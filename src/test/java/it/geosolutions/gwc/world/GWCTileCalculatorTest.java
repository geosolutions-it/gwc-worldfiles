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

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class GWCTileCalculatorTest {

    GWCTileCalculator tileCalculator = new GWCTileCalculator();

    @Test
    public void testGridsetId() {
        assertEquals("EPSG:900913", tileCalculator.getGridsetId(new File("/tmp/EPSG_900913_03")));
        assertEquals("EPSG:900913", tileCalculator.getGridsetId(new File("EPSG_900913_03_foobar")));
        assertEquals("EPSG:4326", tileCalculator.getGridsetId(new File("EPSG_4326_02")));
        assertEquals(
                "WebMercatorQuad", tileCalculator.getGridsetId(new File("WebMercatorQuad_02")));
    }

    @Test
    public void testCoordinates() {
        assertArrayEquals(
                new long[] {1, 2, 3},
                tileCalculator.getCoordinates(new File("/EPSG_900913_03/foo_bar/01_02.png")));
    }

    @Test
    public void testInvalidFileName() {
        assertNull(tileCalculator.getCoordinates(new File("/EPSG_900913_03/foo_bar/0102.png")));
    }

    @Test
    public void testInvalidParentName() {
        assertNull(tileCalculator.getCoordinates(new File("/EPSG_900913/foo_bar/01_02.png")));
    }

    @Test
    public void testInvalidStructure() {
        assertNull(tileCalculator.getCoordinates(new File("/EPSG_900913/01_02.png")));
    }

}


