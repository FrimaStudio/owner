/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.aeonbits.owner.loaders.Loader;

/**
 * Default implementation for {@link Factory}.
 *
 * @author Luigi R. Viggiano
 */
class DefaultFactory implements Factory {

    private final ScheduledExecutorService scheduler;
    private OwnerProperties props;
    final LoadersManager loadersManager;

    DefaultFactory(ScheduledExecutorService scheduler, OwnerProperties props) {
        this.scheduler = scheduler;
        this.props = props;
        this.loadersManager = new LoadersManager();
    }

    @SuppressWarnings("unchecked")
    public <T extends Config> T create(Class<? extends T> clazz, Map<String, Object>... imports) {
        Class<?>[] interfaces = new Class<?>[] { clazz };
        VariablesExpander expander = new VariablesExpander(props);
        PropertiesManager manager = new PropertiesManager(clazz, new OwnerProperties(), scheduler, expander,
                loadersManager, imports);
        PropertiesInvocationHandler handler = new PropertiesInvocationHandler(manager);
        T proxy = (T) newProxyInstance(clazz.getClassLoader(), interfaces, handler);
        handler.setProxy(proxy);
        return proxy;
    }

    public Object setProperty(String key, Object value) {
        checkKey(key);
        return props.put(key, value);
    }

    private void checkKey(String key) {
        if (key == null)
            throw new IllegalArgumentException("key can't be null");
        if (key.isEmpty())
            throw new IllegalArgumentException("key can't be empty");
    }

    public OwnerProperties getProperties() {
        return props;
    }

    public void setProperties(OwnerProperties properties) {
        if (properties == null)
            props = new OwnerProperties();
        else
            props = properties;
    }

    public void registerLoader(Loader loader) {
        loadersManager.registerLoader(loader);
    }

    public Object getProperty(String key) {
        checkKey(key);
        return props.get(key);
    }

    public Object clearProperty(String key) {
        checkKey(key);
        return props.remove(key);
    }

}
