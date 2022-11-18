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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class WorldFileWriter {

    GridSet gridSet;
    TileCalculator tileCalculator;

    boolean overwrite;

    public WorldFileWriter(GridSet gridSet, TileCalculator tileCalculator, boolean overwrite) {
        this.gridSet = gridSet;
        this.tileCalculator = tileCalculator;
        this.overwrite = overwrite;
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
        String extension = FilenameUtils.getExtension(file.getName());
        if (extension.equalsIgnoreCase("png")) return "pnw";
        else if (extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("jpg"))
            return "jgw";
        else if (extension.equalsIgnoreCase("tiff") || extension.equalsIgnoreCase("tif"))
            return "tfw";
        return "wld";
    }
}
