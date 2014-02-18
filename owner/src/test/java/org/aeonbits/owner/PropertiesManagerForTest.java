/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.aeonbits.owner.event.ReloadListener;

/**
 * @author Luigi R. Viggiano
 */
public class PropertiesManagerForTest extends PropertiesManager {
    public PropertiesManagerForTest(Class<? extends Config> clazz, OwnerProperties properties,
            ScheduledExecutorService scheduler, VariablesExpander expander, LoadersManager loaders,
            OwnerProperties... imports) {
        super(clazz, properties, scheduler, expander, loaders, imports);
    }

    @Override
    public OwnerProperties load() {
        return super.load();
    }

    public List<ReloadListener> getReloadListeners() {
        return reloadListeners;
    }

    public List<PropertyChangeListener> getPropertyChangeListeners() {
        return propertyChangeListeners;
    }
}
