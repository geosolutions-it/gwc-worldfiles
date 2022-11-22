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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.geowebcache.config.ConfigurationResourceProvider;

/** Simplified resource provider for usage in this command line tool */
class SingleFileResourceProvider implements ConfigurationResourceProvider {

    File file;

    public SingleFileResourceProvider(File file) {
        this.file = file;
    }

    public InputStream in() throws IOException {
        return new FileInputStream(file);
    }

    public OutputStream out() throws IOException {
        return null;
    }

    public void backup() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getId() {
        throw new UnsupportedOperationException();
    }

    public String getLocation() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setTemplate(String s) {
        throw new UnsupportedOperationException();
    }

    public boolean hasInput() {
        return true;
    }

    public boolean hasOutput() {
        return false;
    }
}
