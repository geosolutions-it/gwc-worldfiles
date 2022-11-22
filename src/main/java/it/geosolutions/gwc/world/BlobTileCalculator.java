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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FilenameUtils;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;

/** {@link TileCalculator} for the blob storages (S3, Azure and so on) * */
public class BlobTileCalculator implements TileCalculator {

    public int getMaximumDepth() {
        return 6;
    }

    @Override
    public GridSet getGridset(File cacheChild, GridSetBroker broker) {
        // gridset here is the directory name, but it could be URL-encoded when downloding
        // files on local file system
        String name = cacheChild.getName();
        GridSet gridSet = broker.get(name);
        if (gridSet == null)
            try {
                gridSet = broker.get(URLDecoder.decode(name, StandardCharsets.UTF_8.name()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        return gridSet;
    }

    /** The file is always in z/x/y.extension form */
    public long[] getCoordinates(File tileFile, GridSet gridSet) {
        try {
            int z = Integer.valueOf(tileFile.getParentFile().getParentFile().getName());
            long x = Long.valueOf(tileFile.getParentFile().getName());
            long y = Long.valueOf(FilenameUtils.getBaseName(tileFile.getName()));

            return new long[] {x, y, z};
        } catch (Exception e) {
            return null;
        }
    }
}
