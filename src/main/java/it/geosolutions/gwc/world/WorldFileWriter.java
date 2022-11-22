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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.referencing.CRS;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSet;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

class WorldFileWriter {

    static Map<Integer, String> WKTS = new HashMap<>();

    static {
        try (InputStream is = WorldFileWriter.class.getResourceAsStream("/epsg.properties")) {
            Properties props = new Properties();
            props.load(is);
            props.forEach((k, v) -> WKTS.put(Integer.valueOf((String) k), (String) v));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    GridSet gridSet;
    TileCalculator tileCalculator;

    boolean overwrite;

    String wkt;

    public WorldFileWriter(
            GridSet gridSet,
            TileCalculator tileCalculator,
            boolean overwrite,
            boolean writeProjection)
            throws FactoryException {
        this.gridSet = gridSet;
        this.tileCalculator = tileCalculator;
        this.overwrite = overwrite;

        if (writeProjection) {

            int srid = gridSet.getSrs().getNumber();
            if (WKTS.containsKey(srid)) {
                wkt = WKTS.get(srid);
            } else {
                CoordinateReferenceSystem crs = CRS.decode("EPSG:" + srid);
                wkt = crs.toWKT();
            }
        }
    }

    public boolean write(File file) {
        // check a world file is not already there
        File parent = file.getParentFile();
        String baseName = FilenameUtils.getBaseName(file.getName());
        String worldExtension = getWorldExtension(file);
        File world = new File(parent, baseName + "." + worldExtension);
        if (!overwrite && world.exists()) return false;

        String worldFileContents = getWorldFile(file);
        if (worldFileContents == null) return false;

        try {
            FileUtils.writeStringToFile(world, worldFileContents, StandardCharsets.UTF_8);

            if (wkt != null) {
                File prj = new File(parent, baseName + ".prj");
                FileUtils.writeStringToFile(prj, wkt, StandardCharsets.UTF_8);
            }

            return true;
        } catch (IOException e) {
            System.err.println(
                    "Failed to write world file " + world + ". Error: " + e.getMessage());
            return false;
        }
    }

    String getWorldFile(File file) {
        // compute affine transform parameters
        long[] coordinates = tileCalculator.getCoordinates(file, gridSet);
        if (coordinates == null) return null;

        BoundingBox bbox = gridSet.boundsFromIndex(coordinates);
        int tileWidth = gridSet.getTileWidth();
        int tileHeight = gridSet.getTileHeight();

        double scaleX = bbox.getWidth() / tileWidth;
        double scaleY = bbox.getHeight() / tileHeight;
        double offsetX = bbox.getMinX();
        double offsetY = bbox.getMaxY();

        StringBuilder sb = new StringBuilder();
        sb.append(scaleX).append("\n");
        sb.append(0).append("\n");
        sb.append(0).append("\n");
        sb.append(-scaleY).append("\n");
        sb.append(offsetX).append("\n");
        sb.append(offsetY).append("\n");
        return sb.toString();
    }

    String getWorldExtension(File file) {
        // QGIS does not seem to recognize the world file unless it has wld extension
        //        String extension = FilenameUtils.getExtension(file.getName());
        //        if (extension.equalsIgnoreCase("png")) return "pnw";
        //        else if (extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("jpg"))
        //            return "jgw";
        //        else if (extension.equalsIgnoreCase("tiff") || extension.equalsIgnoreCase("tif"))
        //            return "tfw";
        return "wld";
    }
}
