/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Luigi R. Viggiano
 */
public class ConfigURLFactoryTest {
    @Test
    public void shouldReturnAnURL() throws MalformedURLException {
        ConfigURLFactory h = new ConfigURLFactory(this.getClass().getClassLoader(), new VariablesExpander(
                new OwnerProperties()));
        Collection<URL> urls = h.newURL("classpath:test.properties");
        assertNotNull(urls);
        assertFalse(urls.isEmpty());
    }
}
