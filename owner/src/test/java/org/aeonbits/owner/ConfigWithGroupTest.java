/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import static org.junit.Assert.assertEquals;

import org.aeonbits.owner.Config.Group;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Fred Deschenes
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigWithGroupTest {

    private static final String DEFAULT_VALUE = "The default value";
    private static final String GROUP = "some.group";

    @Group(GROUP)
    public static interface SampleConfig extends Accessible {
        @Key("with.key")
        @DefaultValue(DEFAULT_VALUE)
        String withKey();

        @DefaultValue(DEFAULT_VALUE)
        String withoutKey();
    }

    private SampleConfig sut = ConfigFactory.create(SampleConfig.class);

    @Test
    public void withKey_getProperty() {
        String val = (String) sut.getProperty(GROUP + ".with.key");

        assertEquals(DEFAULT_VALUE, val);
    }

    @Test
    public void withoutKey_getProperty() {
        String val = (String) sut.getProperty(GROUP + ".withoutKey");

        assertEquals(DEFAULT_VALUE, val);
    }

    @Test
    public void withKey() {
        String val = sut.withKey();

        assertEquals(DEFAULT_VALUE, val);
    }

    @Test
    public void withoutKey() {
        String val = sut.withoutKey();

        assertEquals(DEFAULT_VALUE, val);
    }
}
