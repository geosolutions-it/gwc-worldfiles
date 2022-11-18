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

public interface TileCalculator {

    /**
     * How much to drill down below the directory containing the gridset
     *
     * @return
     */
    int getMaximumDepth();

    /**
     * The gridset identifier included in the top level cache directory name
     * @param cacheChild
     * @return
     */
    String getGridsetId(File cacheChild);

    /**
     * GWC internal XYZ coordinates for a given file (TMS coordinates)
     */
    long[] getCoordinates(File tileFile);
}
