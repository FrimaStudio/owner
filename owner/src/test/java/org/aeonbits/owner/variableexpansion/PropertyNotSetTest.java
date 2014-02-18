/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.variableexpansion;

import static org.junit.Assert.assertEquals;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.OwnerProperties;
import org.aeonbits.owner.util.Collections;
import org.junit.Test;

/**
 * Tests properties resolution.
 *
 * @author Luigi R. Viggiano
 */
public class PropertyNotSetTest {

    public interface MyConfig extends Config {
        @DefaultValue("Hello ${world}.")
        String foo();
    }

    @Test
    public void testPropertyNotSet() {
        MyConfig cfg = ConfigFactory.create(MyConfig.class);
        Object value = cfg.foo();
        assertEquals("Hello .", value);
    }

    @Test
    public void testPropertySet() {
        OwnerProperties ctx = new OwnerProperties(Collections.map("world", "Earth"));
        MyConfig cfg = ConfigFactory.create(MyConfig.class, ctx);
        assertEquals("Hello Earth.", cfg.foo());
    }

    @Test
    public void testPropertyChanged() {
        OwnerProperties ctx = new OwnerProperties(Collections.map("world", "Earth"));
        MyConfig cfg = ConfigFactory.create(MyConfig.class, ctx);

        ctx.put("world", "Mars");
        assertEquals("Hello Earth.", cfg.foo());
    }
}
