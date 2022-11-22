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

import static org.junit.Assert.*;

import java.io.File;
import org.geowebcache.config.DefaultGridsets;
import org.geowebcache.grid.GridSet;
import org.junit.Test;
import org.opengis.referencing.FactoryException;

public class WorldFileWriterTest {

    @Test
    public void testWorldFileContent() throws FactoryException {
        GridSet gridSet = new DefaultGridsets(true, true).worldEpsg4326();
        TileCalculator tileCalculator = new GWCTileCalculator();
        WorldFileWriter writer = new WorldFileWriter(gridSet, tileCalculator, false, false);
        String contents = writer.getWorldFile(new File("EPSG_4326_00/00_00/00_00.png"));
        String expected =
                "0.703125\n" //
                        + "0\n" //
                        + "0\n" //
                        + "-0.703125\n" //
                        + "-180.0\n" //
                        + "90.0\n";
        assertEquals(expected, contents);
    }

    @Test
    public void testWorldFileContent512() throws FactoryException {
        GridSet gridSet = new DefaultGridsets(true, true).worldEpsg4326x2();
        TileCalculator tileCalculator = new GWCTileCalculator();
        WorldFileWriter writer = new WorldFileWriter(gridSet, tileCalculator, false, false);
        String contents = writer.getWorldFile(new File("EPSG_4326_00/00_00/00_00.png"));
        String expected =
                "0.3515625\n" //
                        + "0\n" //
                        + "0\n" //
                        + "-0.3515625\n" //
                        + "-180.0\n" //
                        + "90.0\n";
        assertEquals(expected, contents);
    }
}
