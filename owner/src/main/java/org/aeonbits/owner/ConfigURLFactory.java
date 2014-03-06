/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Luigi R. Viggiano
 */
public class ConfigURLFactory {

    /**
     * Interface to allow 'resources:' sources to be loaded using an external path formatting/searching
     */
    public static interface ResourcesPathResolver {
        /**
         * Resolves the correct path to use for a specified path, null if not valid/found
         * @param path  The path to find
         * @return      The found path or null
         */
        public String resolvePath(String path);
    }

    private static ResourcesPathResolver resourcePathResolver;

    /**
     * Sets the {@link ResourcesPathResolver} to use to resolve paths.
     * @param resolver    The getter to set
     */
    public static void setResourcePathGetter(ResourcesPathResolver resolver) {
        synchronized (ConfigURLFactory.class) {
            resourcePathResolver = resolver;
        }
    }

    private static final String CLASSPATH_PROTOCOL = "classpath:";
    private static final String RESOURCES_PROTOCOL = "resources:";

    private final transient ClassLoader classLoader;
    private final VariablesExpander expander;

    ConfigURLFactory(ClassLoader classLoader, VariablesExpander expander) {
        this.classLoader = classLoader;
        this.expander = expander;
    }

    URL newURL(String spec) throws MalformedURLException {
        String expanded = expand(spec);
        URL url;
        if (expanded.startsWith(CLASSPATH_PROTOCOL)) {
            String path = expanded.substring(CLASSPATH_PROTOCOL.length());
            url = classLoader.getResource(path);
            if (url == null)
                return null;
        } else if (expanded.startsWith(RESOURCES_PROTOCOL)) {
            ResourcesPathResolver resolver = resourcePathResolver;
            if (resolver == null) {
                throw Util.unsupported("ConfigURLFactory resourcePathGetter not set.");
            }

            String path = expanded.substring(RESOURCES_PROTOCOL.length());
            String resolvedPath = resolver.resolvePath(path);

            if (resolvedPath != null) {
                url = new URL(resolvedPath);
            } else {
                return null;
            }
        } else {
            url = new URL(expanded);
        }
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), expand(url.getPath()));
    }

    private String expand(String path) {
        return expander.expand(path);
    }

    String toClasspathURLSpec(String name) {
        return CLASSPATH_PROTOCOL + name.replace('.', '/');
    }

}
