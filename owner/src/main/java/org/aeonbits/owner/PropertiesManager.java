/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import org.aeonbits.owner.event.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.synchronizedList;
import static org.aeonbits.owner.Config.LoadType.FIRST;
import static org.aeonbits.owner.PropertiesMapper.defaults;
import static org.aeonbits.owner.Util.*;

/**
 * Loads properties and manages access to properties handling concurrency.
 *
 * @author Luigi R. Viggiano
 */
class PropertiesManager implements Reloadable, Accessible {
    private static final long serialVersionUID = -7351207206455952345L;

    private final Class<? extends Config> clazz;
    private final OwnerProperties[] imports;
    private final OwnerProperties properties;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReadLock readLock = lock.readLock();
    private final WriteLock writeLock = lock.writeLock();

    private final LoadType loadType;
    private final List<URL> urls;
    private final HotReloadLogic hotReloadLogic;

    private volatile boolean loading = false;

    final List<ReloadListener> reloadListeners = synchronizedList(new LinkedList<ReloadListener>());

    private Object proxy;
    private final LoadersManager loaders;

    @SuppressWarnings("serial")
    final List<PropertyChangeListener> propertyChangeListeners = synchronizedList(new LinkedList<PropertyChangeListener>() {
        @Override
        public boolean remove(Object o) {
            Iterator<PropertyChangeListener> iterator = iterator();
            while (iterator.hasNext()) {
                Object item = iterator.next();
                if (item.equals(o)) {
                    iterator.remove();
                    return true;
                }
            }
            return false;
        }
    });

    @Retention(RUNTIME)
    @Target(METHOD)
    @interface Delegate {
    }

    PropertiesManager(Class<? extends Config> clazz, OwnerProperties properties, ScheduledExecutorService scheduler,
            VariablesExpander expander, LoadersManager loaders, OwnerProperties... imports) {
        this.clazz = clazz;
        this.properties = properties;
        this.loaders = loaders;
        this.imports = imports;

        ConfigURLFactory urlFactory = new ConfigURLFactory(clazz.getClassLoader(), expander);
        urls = toURLs(getAnnotationCheckInterfaces(clazz, Sources.class), urlFactory);

        LoadPolicy loadPolicy = getAnnotationCheckInterfaces(clazz, LoadPolicy.class);
        loadType = (loadPolicy != null) ? loadPolicy.value() : FIRST;

        HotReload hotReload = getAnnotationCheckInterfaces(clazz, HotReload.class);
        if (hotReload != null) {
            hotReloadLogic = new HotReloadLogic(hotReload, urls, this);

            if (hotReloadLogic.isAsync())
                scheduler.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        hotReloadLogic.checkAndReload();
                    }
                }, hotReload.value(), hotReload.value(), hotReload.unit());
        } else {
            hotReloadLogic = null;
        }
    }

    private List<URL> toURLs(Sources sources, ConfigURLFactory urlFactory) {
        String[] specs = specs(sources, urlFactory);
        ArrayList<URL> result = new ArrayList<URL>();
        for (String spec : specs) {
            try {
                Collection<URL> urls = urlFactory.newURL(spec);
                if (urls != null)
                    result.addAll(urls);
            } catch (MalformedURLException e) {
                throw unsupported(e, "Can't convert '%s' to a valid URL", spec);
            }
        }
        return result;
    }

    private String[] specs(Sources sources, ConfigURLFactory urlFactory) {
        if (sources != null)
            return sources.value();
        return defaultSpecs(urlFactory);
    }

    private String[] defaultSpecs(ConfigURLFactory urlFactory) {
        String prefix = urlFactory.toClasspathURLSpec(clazz.getName());
        return loaders.defaultSpecs(prefix);
    }

    OwnerProperties load() {
        writeLock.lock();
        try {
            return load(properties);
        } finally {
            writeLock.unlock();
        }
    }

    private OwnerProperties load(OwnerProperties props) {
        try {
            loading = true;
            defaults(props, clazz);
            OwnerProperties loadedFromFile = doLoad();
            merge(props, loadedFromFile);
            merge(props, reverse(imports));
            return props;
        } finally {
            loading = false;
        }
    }

    @Delegate
    public void reload() {
        writeLock.lock();
        try {
            OwnerProperties loaded = load(new OwnerProperties());
            List<PropertyChangeEvent> events = fireBeforePropertyChangeEvents(keys(properties, loaded), properties,
                    loaded);
            ReloadEvent reloadEvent = fireBeforeReloadEvent(events, properties, loaded);
            applyPropertyChangeEvents(events);
            firePropertyChangeEvents(events);
            fireReloadEvent(reloadEvent);
        } catch (RollbackBatchException e) {
            ignore();
        } finally {
            writeLock.unlock();
        }
    }

    private <T extends Map<String, Object>> Set<String> keys(T... maps) {
        Set<String> keys = new HashSet<String>();
        for (Map<String, ?> map : maps)
            keys.addAll(map.keySet());
        return keys;
    }

    private void applyPropertyChangeEvents(List<PropertyChangeEvent> events) {
        for (PropertyChangeEvent event : events)
            performSetProperty(event.getPropertyName(), event.getNewValue());
    }

    private void fireReloadEvent(ReloadEvent reloadEvent) {
        for (ReloadListener listener : reloadListeners)
            listener.reloadPerformed(reloadEvent);
    }

    private ReloadEvent fireBeforeReloadEvent(List<PropertyChangeEvent> events, OwnerProperties oldProperties,
            OwnerProperties newProperties) throws RollbackBatchException {
        ReloadEvent reloadEvent = new ReloadEvent(proxy, events, oldProperties, newProperties);
        for (ReloadListener listener : reloadListeners)
            if (listener instanceof TransactionalReloadListener)
                ((TransactionalReloadListener) listener).beforeReload(reloadEvent);
        return reloadEvent;
    }

    @Delegate
    public void addReloadListener(ReloadListener listener) {
        if (listener != null)
            reloadListeners.add(listener);
    }

    @Delegate
    public void removeReloadListener(ReloadListener listener) {
        if (listener != null)
            reloadListeners.remove(listener);
    }

    @Delegate
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null)
            propertyChangeListeners.add(listener);
    }

    @Delegate
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null)
            propertyChangeListeners.remove(listener);
    }

    @Delegate
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        if (propertyName == null || listener == null)
            return;

        final boolean transactional = listener instanceof TransactionalPropertyChangeListener;
        propertyChangeListeners.add(new PropertyChangeListenerWrapper(propertyName, listener, transactional));
    }

    private static class PropertyChangeListenerWrapper implements TransactionalPropertyChangeListener, Serializable {
        private static final long serialVersionUID = 4989860386684360313L;

        private final String propertyName;
        private final PropertyChangeListener listener;
        private final boolean transactional;

        public PropertyChangeListenerWrapper(String propertyName, PropertyChangeListener listener, boolean transactional) {
            this.propertyName = propertyName;
            this.listener = listener;
            this.transactional = transactional;

        }

        public void beforePropertyChange(PropertyChangeEvent event) throws RollbackOperationException,
                RollbackBatchException {
            if (transactional && propertyNameMatches(event))
                ((TransactionalPropertyChangeListener) listener).beforePropertyChange(event);
        }

        private boolean propertyNameMatches(PropertyChangeEvent event) {
            return propertyName.equals(event.getPropertyName());
        }

        public void propertyChange(PropertyChangeEvent event) {
            if (propertyNameMatches(event))
                listener.propertyChange(event);
        }

        @Override
        public boolean equals(Object obj) {
            return listener.equals(obj);
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    OwnerProperties doLoad() {
        return loadType.load(urls, loaders);
    }

    private static <T extends Map<String, Object>> void merge(OwnerProperties results, T... inputs) {
        for (Map<String, Object> input : inputs)
            results.putAll(input);
    }

    @Delegate
    public Object getProperty(String key) {
        readLock.lock();
        try {
            return properties.get(key);
        } finally {
            readLock.unlock();
        }
    }

    void syncReloadCheck() {
        if (hotReloadLogic != null && hotReloadLogic.isSync())
            hotReloadLogic.checkAndReload();
    }

    @Delegate
    public Object getProperty(String key, Object defaultValue) {
        readLock.lock();
        try {
            if (properties.containsKey(key)) {
                return properties.get(key);
            }

            return defaultValue;
        } finally {
            readLock.unlock();
        }
    }

    @Delegate
    public Set<String> propertyNames() {
        readLock.lock();
        try {
            LinkedHashSet<String> result = new LinkedHashSet<String>(properties.keySetRecursive());
            return result;
        } finally {
            readLock.unlock();
        }
    }

    @Delegate
    public void list(PrintStream out) throws IOException {
        readLock.lock();
        try {
            properties.list(out);
        } finally {
            readLock.unlock();
        }
    }

    @Delegate
    public void list(PrintWriter out) {
        readLock.lock();
        try {
            properties.list(out);
        } finally {
            readLock.unlock();
        }
    }

    @Delegate
    public Object setProperty(String key, Object newValue) {
        writeLock.lock();
        try {
            Object oldValue = properties.get(key);
            try {
                if (eq(oldValue, newValue))
                    return oldValue;

                PropertyChangeEvent event = new PropertyChangeEvent(proxy, key, oldValue, newValue);
                fireBeforePropertyChange(event);
                Object result = performSetProperty(key, newValue);
                firePropertyChange(event);
                return result;
            } catch (RollbackException e) {
                return oldValue;
            }
        } finally {
            writeLock.unlock();
        }
    }

    private Object performSetProperty(String key, Object value) {
        //FIXME: See MANTICORE-3844 in JIRA
        // return (value == null) ? performRemoveProperty(key) : asString(properties.put(key, value));
        return asString(properties.put(key, value));
    }

    @Delegate
    public Object removeProperty(String key) {
        writeLock.lock();
        try {
            Object oldValue = properties.get(key);
            Object newValue = null;
            PropertyChangeEvent event = new PropertyChangeEvent(proxy, key, oldValue, newValue);
            fireBeforePropertyChange(event);
            String result = performRemoveProperty(key);
            firePropertyChange(event);
            return result;
        } catch (RollbackException e) {
            return properties.get(key);
        } finally {
            writeLock.unlock();
        }
    }

    private String performRemoveProperty(String key) {
        return asString(properties.remove(key));
    }

    @Delegate
    public void clear() {
        writeLock.lock();
        try {
            List<PropertyChangeEvent> events = fireBeforePropertyChangeEvents(keys(properties), properties,
                    new HashMap<String, Object>());
            applyPropertyChangeEvents(events);
            firePropertyChangeEvents(events);
        } catch (RollbackBatchException e) {
            ignore();
        } finally {
            writeLock.unlock();
        }
    }

    void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    @Delegate
    @Override
    public String toString() {
        readLock.lock();
        try {
            return properties.toString();
        } finally {
            readLock.unlock();
        }
    }

    boolean isLoading() {
        return loading;
    }

    private List<PropertyChangeEvent> fireBeforePropertyChangeEvents(Set<String> keys, Map<String, Object> oldValues,
            Map<String, Object> newValues) throws RollbackBatchException {
        List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        for (String key : keys) {
            Object oldValue = oldValues.get(key);
            Object newValue = newValues.get(key);
            if (!eq(oldValue, newValue)) {
                PropertyChangeEvent event = new PropertyChangeEvent(proxy, key, oldValue, newValue);
                try {
                    fireBeforePropertyChange(event);
                    events.add(event);
                } catch (RollbackOperationException e) {
                    ignore();
                }
            }
        }
        return events;
    }

    private void firePropertyChangeEvents(List<PropertyChangeEvent> events) {
        for (PropertyChangeEvent event : events)
            firePropertyChange(event);
    }

    private void fireBeforePropertyChange(PropertyChangeEvent event) throws RollbackBatchException,
            RollbackOperationException {
        for (PropertyChangeListener listener : propertyChangeListeners)
            if (listener instanceof TransactionalPropertyChangeListener)
                ((TransactionalPropertyChangeListener) listener).beforePropertyChange(event);
    }

    private void firePropertyChange(PropertyChangeEvent event) {
        for (PropertyChangeListener listener : propertyChangeListeners)
            listener.propertyChange(event);
    }

    @Delegate
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Proxy))
            return false;
        InvocationHandler handler = Proxy.getInvocationHandler(obj);
        if (!(handler instanceof PropertiesInvocationHandler))
            return false;
        PropertiesInvocationHandler propsInvocationHandler = (PropertiesInvocationHandler) handler;
        PropertiesManager that = propsInvocationHandler.propertiesManager;
        return this.equals(that);
    }

    private boolean equals(PropertiesManager that) {
        if (!this.isAssignationCompatibleWith(that))
            return false;
        this.readLock.lock();
        try {
            that.readLock.lock();
            try {
                return this.properties.equals(that.properties);
            } finally {
                that.readLock.unlock();
            }
        } finally {
            this.readLock.unlock();
        }
    }

    private boolean isAssignationCompatibleWith(PropertiesManager that) {
        return this.clazz.isAssignableFrom(that.clazz) || that.clazz.isAssignableFrom(this.clazz);
    }

    @Delegate
    @Override
    public int hashCode() {
        readLock.lock();
        try {
            return properties.hashCode();
        } finally {
            readLock.unlock();
        }
    }

}
