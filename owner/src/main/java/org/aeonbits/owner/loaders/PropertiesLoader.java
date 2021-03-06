/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.aeonbits.owner.OwnerProperties;

/**
 * A {@link Loader loader} able to read properties from standard Java properties files.
 *
 * @since 1.0.5
 * @author Luigi R. Viggiano
 */
public class PropertiesLoader implements Loader {

    private static final long serialVersionUID = -1781643040589572341L;

    public boolean accept(URL url) {
        return true;
    }

    public void load(OwnerProperties result, InputStream input) throws IOException {
        Properties props = new Properties();
        props.load(input);

        for (String key : props.stringPropertyNames()) {
            result.put(key, props.get(key));
        }
    }

    public String defaultSpecFor(String urlPrefix) {
        return urlPrefix + ".properties";
    }

}
