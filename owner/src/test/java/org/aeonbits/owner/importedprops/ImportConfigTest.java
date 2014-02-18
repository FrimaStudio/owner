/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.importedprops;

import static org.aeonbits.owner.UtilTest.fileFromURL;
import static org.aeonbits.owner.UtilTest.save;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.OwnerProperties;
import org.aeonbits.owner.TestConstants;
import org.junit.Test;

/**
 * @author Luigi R. Viggiano
 */
public class ImportConfigTest implements TestConstants {
    private static final String SPEC = "file:" + RESOURCES_DIR + "/ImportConfig.properties";

    @Sources(SPEC)
    public static interface ImportConfig extends Config {

        @DefaultValue("apple")
        String foo();

        @DefaultValue("pear")
        String bar();

        @DefaultValue("orange")
        String baz();

    }

    @Test
    public void testImport() {
        OwnerProperties props = new OwnerProperties();
        props.put("foo", "pineapple");
        props.put("bar", "lime");
        ImportConfig cfg = ConfigFactory.create(ImportConfig.class, props); // props imported!
        assertEquals("pineapple", cfg.foo());
        assertEquals("lime", cfg.bar());
        assertEquals("orange", cfg.baz());
    }

    @Test
    public void testImportOrder() {
        OwnerProperties p1 = new OwnerProperties();
        p1.put("foo", "pineapple");
        p1.put("bar", "lime");

        OwnerProperties p2 = new OwnerProperties();
        p2.put("bar", "grapefruit");
        p2.put("baz", "blackberry");

        ImportConfig cfg = ConfigFactory.create(ImportConfig.class, p1, p2); // props imported!

        assertEquals("pineapple", cfg.foo());
        assertEquals("lime", cfg.bar()); // p1 prevails, so this is lime and not grapefruit
        assertEquals("blackberry", cfg.baz());
    }

    @Test
    public void testThatImportedPropertiesHaveHigherPriorityThanPropertiesLoadedBySources() throws IOException {
        File target = fileFromURL(SPEC);

        save(target, new OwnerProperties() {
            {
                put("foo", "strawberries");
            }
        });

        try {
            OwnerProperties props = new OwnerProperties();
            props.put("foo", "pineapple");
            props.put("bar", "lime");
            ImportConfig cfg = ConfigFactory.create(ImportConfig.class, props); // props imported!
            assertEquals("pineapple", cfg.foo());
            assertEquals("lime", cfg.bar());
            assertEquals("orange", cfg.baz());
        } finally {
            target.delete();
        }
    }

    interface ImportedPropertiesHaveHigherPriority extends Config {
        Integer minAge();
    }

    @Test
    public void testImportedPropertiesShouldOverrideSources() {
        ImportedPropertiesHaveHigherPriority cfg = ConfigFactory.create(ImportedPropertiesHaveHigherPriority.class);
        assertEquals(Integer.valueOf(18), cfg.minAge());

        ImportedPropertiesHaveHigherPriority cfg2 = ConfigFactory.create(ImportedPropertiesHaveHigherPriority.class,
                new OwnerProperties() {
                    {
                        put("minAge", "21");
                    }
                },

                new OwnerProperties() {
                    {
                        put("minAge", "22");
                    }
                }

        );

        assertEquals(Integer.valueOf(21), cfg2.minAge());
    }

}
