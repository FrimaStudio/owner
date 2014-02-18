/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.importedprops;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.PrintStream;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.OwnerProperties;
import org.junit.Test;

/**
 * @author Luigi R. Viggiano
 */
public class SystemPropertiesAndEnvTest {
    interface SystemEnvProperties extends Config {
        @Key("file.separator")
        String fileSeparator();

        @Key("java.home")
        String javaHome();

        @Key("HOME")
        String home();

        @Key("USER")
        String user();

        void list(PrintStream out);
    }

    @Test
    public void testSystemEnvProperties() {

        OwnerProperties systemProps = new OwnerProperties(System.getProperties());
        OwnerProperties env = new OwnerProperties(System.getenv());

        SystemEnvProperties cfg = ConfigFactory.create(SystemEnvProperties.class, systemProps, env);
        assertEquals(File.separator, cfg.fileSeparator());
        assertEquals(System.getProperty("java.home"), cfg.javaHome());
        assertEquals(System.getenv().get("HOME"), cfg.home());
        assertEquals(System.getenv().get("USER"), cfg.user());
    }
}
