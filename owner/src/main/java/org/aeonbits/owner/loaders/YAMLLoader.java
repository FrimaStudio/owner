/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.loaders;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.aeonbits.owner.OwnerProperties;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Fred Deschenes
 */
public class YAMLLoader implements Loader {
    public final static String YAML_EXTENSION = ".yaml";

    public boolean accept(URL url) {
        //'.yaml' is the suggested extension, but some people still use '.yml'
        return url.getFile().matches(".*\\.ya?ml$");
    }

    @SuppressWarnings("unchecked")
    public void load(OwnerProperties result, InputStream input) {
        Yaml yaml = new Yaml();

        Object loaded = yaml.load(input);

        if (loaded instanceof Map<?, ?>) {
            result.putAll((Map<String, Object>) loaded);
        }
    }

    public String defaultSpecFor(String urlPrefix) {
        return urlPrefix + YAML_EXTENSION;
    }
}
