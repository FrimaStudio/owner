/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import org.aeonbits.owner.Util.SystemProvider;

/**
 * @author Luigi R. Viggiano
 */
public class SystemProviderForTest implements SystemProvider {

    private final OwnerProperties system;
    private final OwnerProperties env;

    public SystemProviderForTest(OwnerProperties system, OwnerProperties env) {
        this.system = system;
        this.env = env;
    }

    public Object getProperty(String key) {
        return system.get(key);
    }

    public OwnerProperties getenv() {
        return env;
    }

    public OwnerProperties getProperties() {
        return system;
    }
}
