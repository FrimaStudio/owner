/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.aeonbits.owner.Config.Sources;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Luigi R. Viggiano
 */
public class ConfigFactoryTest implements TestConstants {

    @Sources("file:${mypath}/myconfig.properties")
    interface MyConfig extends Config {
        @DefaultValue("defaultValue")
        String someValue();

        @DefaultValue("${setMe}")
        String someOtherValue();
    }

    @Before
    public void before() throws IOException {
        ConfigFactory.setProperties(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPropertyNullKey() {
        ConfigFactory.setProperty(null, "foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPropertyEmptyKey() {
        ConfigFactory.setProperty("", "foobar");
    }

    @Test
    public void testSetPropertyTwice() {
        assertNull(ConfigFactory.setProperty("mypath", RESOURCES_DIR));
        assertEquals(RESOURCES_DIR, ConfigFactory.setProperty("mypath", RESOURCES_DIR + "-2"));
        assertEquals(RESOURCES_DIR + "-2", ConfigFactory.getProperty("mypath"));
    }

    @Test
    public void testSetPropertiesNullObject() {
        ConfigFactory.setProperties(null);

        MyConfig cfg = ConfigFactory.create(MyConfig.class);

        assertEquals("defaultValue", cfg.someValue());
    }

    @Test
    public void testGetProperty() {
        ConfigFactory.setProperty("mypath", RESOURCES_DIR);
        assertEquals(RESOURCES_DIR, ConfigFactory.getProperty("mypath"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyNullKey() {
        ConfigFactory.getProperty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertiesEmptyKey() {
        ConfigFactory.getProperty("");
    }

    @Test
    public void testGetClearProperty() {
        ConfigFactory.setProperty("mypath", RESOURCES_DIR);
        assertEquals(RESOURCES_DIR, ConfigFactory.clearProperty("mypath"));
        assertNull(ConfigFactory.getProperty("mypath"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClearPropertyNullKey() {
        ConfigFactory.clearProperty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClearPropertyEmptyKey() {
        ConfigFactory.clearProperty("");
    }

    @After
    public void after() {
        ConfigFactory.setProperties(null); // clean up things.
    }
}
