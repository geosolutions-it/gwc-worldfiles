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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Arrays;
import org.geowebcache.config.DefaultGridsets;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;
import org.junit.Test;

public class BlobTileCalculatorTest {

    BlobTileCalculator tileCalculator = new BlobTileCalculator();
    GridSetBroker broker = new GridSetBroker(Arrays.asList(new DefaultGridsets(true, true)));
    GridSet gridSet = new DefaultGridsets(true, true).worldEpsg3857();

    @Test
    public void testGridsetId() {
        assertEquals(
                "EPSG:900913",
                tileCalculator.getGridset(new File("/tmp/EPSG%3A900913"), broker).getName());
        assertEquals(
                "EPSG:900913",
                tileCalculator.getGridset(new File("EPSG:900913"), broker).getName());
        assertEquals(
                "WebMercatorQuad",
                tileCalculator.getGridset(new File("WebMercatorQuad"), broker).getName());
    }

    @Test
    public void testCoordinates() {
        // y axis flip
        assertArrayEquals(
                new long[] {1, 2, 3},
                tileCalculator.getCoordinates(new File("/EPSG%3A900913/png/3/1/2.png"), gridSet));
    }

    @Test
    public void testInvalidFileName() {
        assertNull(tileCalculator.getCoordinates(new File("/EPSG_900913/3/4/5/aa6.png"), null));
    }

    @Test
    public void testInvalidParentName() {
        assertNull(tileCalculator.getCoordinates(new File("/EPSG_900913/3/abc/2.png"), null));
        assertNull(tileCalculator.getCoordinates(new File("/EPSG_900913/abc/3/2.png"), null));
    }

    @Test
    public void testInvalidStructure() {
        assertNull(tileCalculator.getCoordinates(new File("/EPSG_900913/01_02.png"), null));
    }
}
