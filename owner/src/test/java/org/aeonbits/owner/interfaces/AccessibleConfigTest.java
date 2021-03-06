/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.interfaces;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.LoadersManagerForTest;
import org.aeonbits.owner.OwnerProperties;
import org.aeonbits.owner.PropertiesManagerForTest;
import org.aeonbits.owner.VariablesExpanderForTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Luigi R. Viggiano
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessibleConfigTest {
    @Mock
    private ScheduledExecutorService scheduler;
    private LoadersManagerForTest loaders = new LoadersManagerForTest();

    private final VariablesExpanderForTest expander = new VariablesExpanderForTest(new OwnerProperties());

    public static interface AccessibleConfig extends Config, Accessible {
        @DefaultValue("Bohemian Rapsody - Queen")
        String favoriteSong();

        @Key("salutation.text")
        @DefaultValue("Good Morning")
        String salutation();
    }

    @Test
    public void testListPrintStream() throws IOException {
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        PropertiesManagerForTest manager = new PropertiesManagerForTest(AccessibleConfig.class, new OwnerProperties(),
                scheduler, expander, loaders);
        manager.load().list(new PrintStream(expected, true));

        AccessibleConfig config = ConfigFactory.create(AccessibleConfig.class);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        config.list(new PrintStream(result, true));

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testListPrintWriter() throws IOException {
        StringWriter expected = new StringWriter();
        PropertiesManagerForTest manager = new PropertiesManagerForTest(AccessibleConfig.class, new OwnerProperties(),
                scheduler, expander, loaders);
        manager.load().list(new PrintWriter(expected, true));

        AccessibleConfig config = ConfigFactory.create(AccessibleConfig.class);
        StringWriter result = new StringWriter();
        config.list(new PrintWriter(result, true));

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testGetProperty() throws IOException {
        AccessibleConfig cfg = ConfigFactory.create(AccessibleConfig.class);
        assertEquals("Good Morning", cfg.getProperty("salutation.text"));
    }

    @Test
    public void testGetPropertyThatDoesNotExists() throws IOException {
        AccessibleConfig cfg = ConfigFactory.create(AccessibleConfig.class);
        assertNull(cfg.getProperty("foo.bar"));
    }

    @Test
    public void testGetPropertyWithDefault() throws IOException {
        AccessibleConfig cfg = ConfigFactory.create(AccessibleConfig.class);

        Object value = cfg.getProperty("salutation.text");

        assertEquals("Good Morning", value);
    }

    @Test
    public void testGetPropertyWithDefaultThatDoesNotExists() throws IOException {
        AccessibleConfig cfg = ConfigFactory.create(AccessibleConfig.class);

        Object value = cfg.getProperty("salutation.text.nonexistent", "Hello");

        assertEquals("Hello", value);
    }

    @Test
    public void testStringPropertyNames() throws Throwable {
        AccessibleConfig cfg = ConfigFactory.create(AccessibleConfig.class);
        Set<String> propNames = cfg.propertyNames();
        assertThat(propNames.size(), is(3));
        assertThat(propNames, containsInAnyOrder("favoriteSong", "salutation", "salutation.text"));
    }

}
