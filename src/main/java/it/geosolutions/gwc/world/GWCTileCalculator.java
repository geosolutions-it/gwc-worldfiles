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

import org.apache.commons.io.FilenameUtils;

import java.io.File;

/** {@link TileCalculator} for the GWC built-in tile layout */
public class GWCTileCalculator implements TileCalculator {

    public int getMaximumDepth() {
        return 2;
    }

    /**
     * Assumes the directory either contains a gridset name in the form "EPSG_xxxx_..." or that the
     * first token in the string is the gridset name (e.g. webbmercator), both followed by the zoom
     * level and an eventual parameter hash
     */
    public String getGridsetId(File cacheChild) {
        String[] parts = cacheChild.getName().split("_");
        if (parts.length > 1 && "EPSG".equals(parts[0])) return "EPSG:" + parts[1];
        return parts[0];
    }

    /**
     * The file is always in "x_y.extension" form, and the zoom level is two levels up (the
     * directory in the middle is used to spread the files)
     */
    public long[] getCoordinates(File tileFile) {
        String[] parts = FilenameUtils.getBaseName(tileFile.getName()).split("_");
        if (parts.length != 2) return null;
        long x = Long.valueOf(parts[0]);
        long y = Long.valueOf(parts[1]);
        long z;
        String[] parentParts = tileFile.getParentFile().getParentFile().getName().split("_");
        if (parentParts.length > 2 && "EPSG".equals(parentParts[0]))
            z = Long.valueOf(parentParts[2]);
        else if (parentParts.length == 2 && !"EPSG".equals(parentParts[0]))
            z = Long.valueOf(parentParts[1]);
        else return null;

        return new long[] {x, y, z};
    }
}
