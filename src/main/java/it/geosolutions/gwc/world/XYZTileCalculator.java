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
import org.apache.commons.lang3.StringUtils;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;

import javax.security.auth.callback.CallbackHandler;
import java.io.File;
import java.util.StringJoiner;

/** {@link TileCalculator} for the XYZ tile layout in FileBlobStore */
public class XYZTileCalculator implements TileCalculator {

    public int getMaximumDepth() {
        return 3;
    }

    @Override
    public GridSet getGridset(File cacheChild, GridSetBroker broker) {
        String name = cacheChild.getName();
        // we could have undercore in the gridset name, or it could be a replacement for ":"
        GridSet gridSet = broker.get(name);
        if (gridSet == null)
            gridSet = broker.get(name.replace("_", ":"));
        
        // do we have a parameters sha1 at the end maybe?
        if (gridSet == null && name.contains("_")) {
            name = name.substring(0, name.lastIndexOf("_"));
            gridSet = broker.get(name);
            if (gridSet == null)
                gridSet = broker.get(name.replace("_", ":"));
        }
        
        return gridSet;
    }

    /** The file is always in z/x/y.extension form */
    public long[] getCoordinates(File tileFile, GridSet gridSet) {
        try {
            int z = Integer.valueOf(tileFile.getParentFile().getParentFile().getName());
            long x = Long.valueOf(tileFile.getParentFile().getName());
            long y = Long.valueOf(FilenameUtils.getBaseName(tileFile.getName()));

            long gwcy = gridSet.getGrid(z).getNumTilesHigh() - y - 1;

            return new long[] {x, gwcy, z};
        } catch (Exception e) {
            return null;
        }
    }
}
