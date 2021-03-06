/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.importedprops;

import static org.junit.Assert.assertEquals;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.OwnerProperties;
import org.aeonbits.owner.SystemProviderForTest;
import org.aeonbits.owner.UtilTest;
import org.aeonbits.owner.util.Collections;
import org.junit.Test;

/**
 * @author Luigi R. Viggiano
 */
public class WithImportedPropertiesTest {

    @Test
    public void testSubstituteWithImports() {
        OwnerProperties propsFromTest = new OwnerProperties();
        propsFromTest.put("external", "propsFromTest");
        WithImportedProperties conf = ConfigFactory.create(WithImportedProperties.class, propsFromTest);
        assertEquals("testing replacement from propsFromTest properties file.", conf.someValue());
    }

    @Test
    public void testSystemProperty() {
        String userHome = System.getProperty("user.home");
        WithImportedProperties conf = ConfigFactory.create(WithImportedProperties.class,
                new OwnerProperties(System.getProperties()));
        assertEquals(userHome, conf.userHome());
    }

    @Test
    public void testSystemEnv() {
        Object save = UtilTest.setSystem(new SystemProviderForTest(new OwnerProperties(Collections.map("user.home",
                "/home/foobar")), new OwnerProperties(Collections.map("HOME", "/home/foobar"))));
        try {
            String envHome = (String) UtilTest.getenv("HOME");
            WithImportedProperties conf = ConfigFactory.create(WithImportedProperties.class, UtilTest.getenv());
            assertEquals(envHome, conf.envHome());
        } finally {
            UtilTest.setSystem(save);
        }
    }

    @Test
    public void testMultipleImports() {
        Object save = UtilTest.setSystem(new SystemProviderForTest(new OwnerProperties(Collections.map("user.home",
                "/home/foobar")), new OwnerProperties(Collections.map("HOME", "/home/foobar"))));
        try {
            OwnerProperties propsFromTest = new OwnerProperties();
            propsFromTest.put("external", "propsFromTest");

            String userHome = (String) UtilTest.getSystemProperty("user.home");
            String envHome = (String) UtilTest.getenv("HOME");
            WithImportedProperties conf = ConfigFactory.create(WithImportedProperties.class, propsFromTest,
                    UtilTest.getSystemProperties(), UtilTest.getenv());
            assertEquals(userHome, conf.userHome());
            assertEquals(envHome, conf.envHome());
            assertEquals("testing replacement from propsFromTest properties file.", conf.someValue());
        } finally {
            UtilTest.setSystem(save);
        }
    }

    @Test
    public void testBackSlash() {
        OwnerProperties propsFromTest = new OwnerProperties();
        propsFromTest.put("external", "propsFromTest");
        String winPath = "C:\\windows\\path";
        propsFromTest.put("value.with.backslash", winPath);

        WithImportedProperties conf = ConfigFactory.create(WithImportedProperties.class, propsFromTest);

        assertEquals(winPath, conf.valueWithBackslash());
    }

    @Test
    public void testPropertyComingFromExternalObject() {
        OwnerProperties propsFromTest = new OwnerProperties();
        propsFromTest.put("external", "propsFromTest");

        WithImportedProperties conf = ConfigFactory.create(WithImportedProperties.class, propsFromTest);

        assertEquals("propsFromTest", conf.external());
    }

    public static interface WithImportedProperties extends Config {
        String someValue();

        @DefaultValue("${user.home}")
        String userHome();

        @DefaultValue("${HOME}")
        String envHome();

        @DefaultValue("${value.with.backslash}")
        String valueWithBackslash();

        String external();
    }
}
