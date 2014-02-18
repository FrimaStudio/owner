/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import java.net.URL;

import org.aeonbits.owner.loaders.Loader;

/**
 * @author Luigi R. Viggiano
 */
public class LoadersManagerForTest extends LoadersManager {
    private static final long serialVersionUID = -7865299822856501920L;

    @Override
    public Loader findLoader(URL url) {
        return super.findLoader(url);
    }
}
