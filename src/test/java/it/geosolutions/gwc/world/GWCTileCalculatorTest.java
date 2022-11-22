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
import java.util.Arrays;
import org.geowebcache.config.DefaultGridsets;
import org.geowebcache.grid.GridSetBroker;
import org.junit.Test;

public class GWCTileCalculatorTest {

    GWCTileCalculator calculator = new GWCTileCalculator();
    GridSetBroker broker = new GridSetBroker(Arrays.asList(new DefaultGridsets(true, true)));

    @Test
    public void testGridsetId() {
        assertEquals(
                "EPSG:900913",
                calculator.getGridset(new File("/tmp/EPSG_900913_03"), broker).getName());
        assertEquals(
                "EPSG:900913",
                calculator.getGridset(new File("EPSG_900913_03_foobar"), broker).getName());
        assertEquals(
                "EPSG:4326", calculator.getGridset(new File("EPSG_4326_02"), broker).getName());
        assertEquals(
                "WebMercatorQuad",
                calculator.getGridset(new File("WebMercatorQuad_02"), broker).getName());
    }

    @Test
    public void testCoordinates() {
        assertArrayEquals(
                new long[] {1, 2, 3},
                calculator.getCoordinates(new File("/EPSG_900913_03/foo_bar/01_02.png"), null));
    }

    @Test
    public void testInvalidFileName() {
        assertNull(calculator.getCoordinates(new File("/EPSG_900913_03/foo_bar/0102.png"), null));
    }

    @Test
    public void testInvalidParentName() {
        assertNull(calculator.getCoordinates(new File("/EPSG_900913/foo_bar/01_02.png"), null));
    }

    @Test
    public void testInvalidStructure() {
        assertNull(calculator.getCoordinates(new File("/EPSG_900913/01_02.png"), null));
    }
}
