/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.reload;

import static org.aeonbits.owner.UtilTest.fileFromURL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.MalformedURLException;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.OwnerProperties;
import org.aeonbits.owner.Reloadable;
import org.aeonbits.owner.TestConstants;
import org.aeonbits.owner.event.ReloadEvent;
import org.aeonbits.owner.event.ReloadListener;
import org.aeonbits.owner.util.Collections;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Luigi R. Viggiano
 */
@RunWith(MockitoJUnitRunner.class)
public class ReloadTest implements TestConstants {
    private static final String SPEC = "file:" + RESOURCES_DIR + "/ReloadableConfig.properties";
    private static File target;
    @Mock
    ReloadListener listener;

    @BeforeClass
    public static void beforeClass() throws MalformedURLException {
        target = fileFromURL(SPEC);
    }

    @Sources(SPEC)
    public interface ReloadableConfig extends Config, Reloadable {
        Integer minimumAge();
    }

    public interface ReloadImportConfig extends Config, Reloadable {
        Integer minimumAge();
    }

    @Test
    public void testReloadWithImportedProperties() throws Throwable {
        OwnerProperties props = new OwnerProperties(Collections.map("minimumAge", "18"));

        ReloadImportConfig cfg = ConfigFactory.create(ReloadImportConfig.class, props);
        assertEquals(Integer.valueOf(18), cfg.minimumAge());

        props.put("minimumAge", "21"); // changing props doesn't reflect to cfg immediately
        assertEquals(Integer.valueOf(18), cfg.minimumAge());

        cfg.reload(); // the config gets reloaded, so the change in props gets reflected
        assertEquals(Integer.valueOf(21), cfg.minimumAge());
    }

    @After
    public void after() throws Throwable {
        target.delete();
    }

    @Test
    public void testReloadListener() throws Throwable {
        ReloadableConfig cfg = ConfigFactory.create(ReloadableConfig.class);
        cfg.addReloadListener(listener);
        cfg.reload();
        cfg.reload();
        cfg.reload();
        verify(listener, times(3)).reloadPerformed(argThat(isReloadListnerWithSource(cfg)));
    }

    @Test
    public void testReloadListenerRemoved() throws Throwable {
        ReloadableConfig cfg = ConfigFactory.create(ReloadableConfig.class);
        cfg.addReloadListener(listener);
        cfg.reload();
        cfg.reload();
        cfg.removeReloadListener(listener);
        cfg.reload();
        verify(listener, times(2)).reloadPerformed(argThat(isReloadListnerWithSource(cfg)));
    }

    private Matcher<ReloadEvent> isReloadListnerWithSource(final ReloadableConfig cfg) {
        return new BaseMatcher<ReloadEvent>() {
            public boolean matches(Object o) {
                ReloadEvent given = (ReloadEvent) o;
                return given.getSource() == cfg;
            }

            public void describeTo(Description description) {
                description.appendText("does not match");
            }
        };
    }

}
