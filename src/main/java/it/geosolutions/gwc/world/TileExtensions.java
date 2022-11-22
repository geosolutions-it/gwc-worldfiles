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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.geowebcache.mime.ApplicationMime;
import org.geowebcache.mime.ImageMime;
import org.geowebcache.mime.MimeType;
import org.geowebcache.mime.XMLMime;

/**
 * Support class collecting all tile file extensions used by GeoWebCache in the various layouts
 * (uses the MimeType class, but in some layouts it uses the MIME internal name, in others the MIME
 * file extension)
 */
class TileExtensions {

    private static Set<String> TILE_EXTENSIONS = collectTileExtensions();

    private static Set<String> collectTileExtensions() {
        Stream<MimeType> mimes = getDeclaredMimeTypes(ImageMime.class);
        mimes = Stream.concat(mimes, getDeclaredMimeTypes(ApplicationMime.class));
        mimes = Stream.concat(mimes, getDeclaredMimeTypes(XMLMime.class));
        List<MimeType> mimeList = mimes.collect(Collectors.toList());

        Set<String> extensions = new HashSet<>();
        mimeList.forEach(m -> extensions.add(m.getFileExtension()));
        mimeList.forEach(m -> extensions.add(m.getInternalName()));

        return extensions;
    }

    private static Stream<MimeType> getDeclaredMimeTypes(Class target) {
        return Arrays.stream(target.getDeclaredFields())
                .filter(
                        f ->
                                Modifier.isStatic(f.getModifiers())
                                        && MimeType.class.isAssignableFrom(f.getType()))
                .map(f -> getMimeType(f));
    }

    private static MimeType getMimeType(Field f) {
        try {
            return (MimeType) f.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean contains(String extension) {
        return TILE_EXTENSIONS.contains(extension);
    }
}
