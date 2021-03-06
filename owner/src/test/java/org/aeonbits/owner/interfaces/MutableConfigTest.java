/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Luigi R. Viggiano
 */
public class MutableConfigTest {

    private MutableConfig cfg;

    interface MutableConfig extends Config, Mutable {
        @DefaultValue("18")
        public Integer minAge();

        public Integer maxAge();
    }

    @Before
    public void before() {
        cfg = ConfigFactory.create(MutableConfig.class);
    }

    @Test
    public void testSetProperty() {
        assertEquals(Integer.valueOf(18), cfg.minAge());
        String oldValue = (String) cfg.setProperty("minAge", "21");
        assertEquals("18", oldValue);
        assertEquals(Integer.valueOf(21), cfg.minAge());
    }

    @Test
    public void testSetPropertyThatWasNull() {
        assertNull(cfg.maxAge());
        String oldValue = (String) cfg.setProperty("maxAge", "999");
        assertNull(oldValue);
        assertEquals(Integer.valueOf(999), cfg.maxAge());
    }

    @Test
    public void testSetPropertyWithNull() {
        assertEquals(Integer.valueOf(18), cfg.minAge());
        String oldValue = (String) cfg.setProperty("minAge", null);
        assertEquals("18", oldValue);
        assertNull(cfg.minAge());
    }

    @Test
    public void testRemoveProperty() {
        assertEquals(Integer.valueOf(18), cfg.minAge());
        String oldValue = (String) cfg.removeProperty("minAge");
        assertEquals("18", oldValue);
        assertNull(cfg.minAge());
    }

    @Test
    public void testClear() {
        assertEquals(Integer.valueOf(18), cfg.minAge());
        cfg.clear();
        assertNull(cfg.minAge());
    }
}
