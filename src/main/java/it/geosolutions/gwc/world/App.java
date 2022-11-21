/*
 *    GeoWebCache world files
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2022, GeoSolutions
 *
 *    This application is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.gwc.world;

import com.google.common.collect.Streams;
import org.apache.commons.io.FilenameUtils;
import org.geowebcache.config.DefaultGridsets;
import org.geowebcache.config.GridSetConfiguration;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.grid.Grid;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.mime.ApplicationMime;
import org.geowebcache.mime.ImageMime;
import org.geowebcache.mime.MimeType;
import org.geowebcache.mime.XMLMime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    private static Printer printer = Printer.STD;
    private static GridSetBroker broker;
    private static TileCalculator calculator = new GWCTileCalculator();

    private static boolean overwrite;

    private static boolean prj;

    private static AtomicLong counter = new AtomicLong(0);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // in case of no arguments, exit and
        if (args.length == 0) {
            printer.print("Tool to generate world file sidecars for tile caches. Usage:\n");
            printer.print(
                    "java -jar gwc-worldfiles.jar [-q] [-j threads] [-prj] [-overwrite] [-layout layout] [-config geowebcache.xml] layer_location\n");
            printer.print("* -q quiet output");
            printer.print(
                    "* -j number of threads to use (defaults to the number of available cores");
            printer.print("* -prj add a projection file (.prj) along with the world file");
            printer.print("* -overwrite activates overwriting existing world files");
            printer.print("* -layout can be gwc (default), xyz, tms, blob");
            printer.print("* -config is the location of the GeoWebCache configuration file");
            printer.print(
                    "* layer_location is the path to the layer folder (normally has gridset specific subfolders as direct children). Must be last command line parameter");
            System.exit(-1);
        }

        File configuration = null;
        File cache;
        int parallelism = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < args.length - 1; i++) {
            String curr = args[i];
            if (curr.equals("-layout")) calculator = getTileCalculator(args[++i]);
            else if (curr.equals("-config")) configuration = new File(args[++i]);
            else if (curr.equals("-j")) parallelism = Integer.parseInt(args[++i]);
            else if (curr.equals("-prj")) prj = true;
            else if (curr.equals("-overwrite")) overwrite = true;
            else if (curr.equals("-q")) printer = Printer.QUIET;
            else {
                printer.err("Unrecognized parameter: " + curr);
                System.exit(-2);
            }
        }
        cache = new File(args[args.length - 1]);

        if (configuration != null && !configuration.isFile()) {
            printer.err("Configuration file does not exist: " + configuration);
            System.exit(-3);
        }

        if (cache == null || !cache.isDirectory()) {
            printer.err("Layer cache root not found, or found but not a directory: " + cache);
            System.exit(-4);
        }

        if (parallelism < 1) {
            printer.err("Parallelism in -j must be at least 1");
            System.exit(-5);
        }

        // build the machinery to compute the world files
        broker = getGridsetBroker(configuration);

        // start the calculation
        printer.print("Computing world files with parallelism: " + parallelism);
        ForkJoinPool customPool = new ForkJoinPool(parallelism);
        customPool.submit(() -> computeWorldFilesAllGridsets(cache)).get();
    }

    private static void computeWorldFilesAllGridsets(File cache) {
        long start = System.currentTimeMillis();
        Arrays.stream(cache.listFiles(f -> f.isDirectory()))
                .parallel()
                .forEach(f -> computeWorldFiles(f));
        printer.print("\nDone!");
        printer.print(
                "Created "
                        + counter
                        + " world files in "
                        + (System.currentTimeMillis() - start) / 1000d
                        + " sec");
    }

    private static void computeWorldFiles(File gridsetDirectory) {
        GridSet gridSet = calculator.getGridset(gridsetDirectory, broker);
        if (gridSet == null) printer.err("Unknown gridset, skipping directory " + gridsetDirectory);

        printer.print("Creating world files in " + gridsetDirectory);

        try {
            WorldFileWriter writer = new WorldFileWriter(gridSet, calculator, overwrite, prj);
            Files.walk(gridsetDirectory.toPath(), calculator.getMaximumDepth())
                    .parallel()
                    .filter(App::isTileFile)
                    .forEach(
                            p -> {
                                if (writer.write(p.toFile())) {
                                    long count = counter.incrementAndGet();
                                    if (count % 5000 == 0)
                                        printer.print("World files generated: " + count);
                                }
                            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isTileFile(Path p) {
        if (!Files.isRegularFile(p)) return false;
        String extension = FilenameUtils.getExtension(p.getFileName().toString());
        return TileExtensions.contains(extension);
    }

    private static TileCalculator getTileCalculator(String layout) {
        if (layout == null || "gwc".equals(layout)) return new GWCTileCalculator();
        if ("xyz".equals(layout)) return new XYZTileCalculator();
        if ("tms".equals(layout)) return new TMSTileCalculator();

        printer.err("Unknonw layout: " + layout);
        System.exit(-2);
        return null;
    }

    private static GridSetBroker getGridsetBroker(File configuration) {
        List<GridSetConfiguration> configurations = new ArrayList<>();
        configurations.add(new DefaultGridsets(true, true));
        if (configuration != null) {
            XMLConfiguration xmlConfiguration =
                    new XMLConfiguration(null, new SingleFileResourceProvider(configuration));
            configurations.add(xmlConfiguration);
        }
        return new GridSetBroker(configurations);
    }
}
