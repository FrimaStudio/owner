/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.event;

import static org.aeonbits.owner.UtilTest.fileFromURL;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.net.MalformedURLException;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;
import org.aeonbits.owner.TestConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Luigi R. Viggiano
 */
@RunWith(MockitoJUnitRunner.class)
public class EventListenerOnReloadTest implements TestConstants {
    private static final String SPEC = "file:" + RESOURCES_DIR + "/EventListenerOnReloadTest.properties";
    private File target;
    @Mock
    private TransactionalPropertyChangeListener propertyChangeListener;
    @Mock
    private TransactionalReloadListener reloadListener;
    private MyConfig cfg;

    @Before
    public void before() throws MalformedURLException {
        target = fileFromURL(SPEC);
        target.delete();
        cfg = ConfigFactory.create(MyConfig.class);
        cfg.addPropertyChangeListener(propertyChangeListener);
        cfg.addReloadListener(reloadListener);
    }

    @After
    public void after() {
        target.delete();
    }

    @Sources(SPEC)
    interface MyConfig extends Mutable, Reloadable {
        @DefaultValue("5")
        Integer someInteger();

        @DefaultValue("foobar")
        String someString();

        @DefaultValue("3.14")
        Double someDouble();

        String nullsByDefault();
    }

    @Test
    public void testPropertyChangeListenerOnReloadWhenNoChangesHaveBeenMade() throws Throwable {
        cfg.reload();
        verifyZeroInteractions(propertyChangeListener);
    }
}
