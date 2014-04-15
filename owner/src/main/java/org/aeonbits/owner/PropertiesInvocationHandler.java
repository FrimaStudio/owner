/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import static org.aeonbits.owner.Config.DisableableFeature.PARAMETER_FORMATTING;
import static org.aeonbits.owner.Config.DisableableFeature.RESULT_CACHING;
import static org.aeonbits.owner.Config.DisableableFeature.VARIABLE_EXPANSION;
import static org.aeonbits.owner.Converters.convert;
import static org.aeonbits.owner.PropertiesMapper.key;
import static org.aeonbits.owner.Util.isFeatureDisabled;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.PropertiesManager.Delegate;
import org.aeonbits.owner.event.ReloadEvent;
import org.aeonbits.owner.event.ReloadListener;

/**
 * This {@link InvocationHandler} receives method calls from the delegate instantiated by {@link ConfigFactory} and maps
 * it to a property value from a property file, or a {@link Config.DefaultValue} specified in method annotation.
 * <p/>
 * The {@link Config.Key} annotation can be used to override default mapping between method names and property names.
 * <p/>
 * Automatic conversion is handled between the property value and the return type expected by the method of the
 * delegate.
 *
 * @author Luigi R. Viggiano
 */
class PropertiesInvocationHandler implements InvocationHandler, Serializable {
    private static final long serialVersionUID = -9178477775317705877L;

    private static final Method[] DELEGATES = findDelegates();
    private final StrSubstitutor substitutor;
    final PropertiesManager propertiesManager;

    private final Map<String, Object> preresolvedProperties = new HashMap<String, Object>();

    PropertiesInvocationHandler(PropertiesManager manager) {
        this.propertiesManager = manager;

        this.propertiesManager.addReloadListener(new ReloadListener() {
            public void reloadPerformed(ReloadEvent event) {
                synchronized (preresolvedProperties) {
                    preresolvedProperties.clear();
                }
            }
        });

        this.substitutor = new StrSubstitutor(manager.load());
    }

    public Object invoke(Object proxy, Method invokedMethod, Object[] args) throws Throwable {
        propertiesManager.syncReloadCheck();
        Method delegate = getDelegateMethod(invokedMethod);
        if (delegate != null)
            return delegate(delegate, args);

        return resolveProperty(invokedMethod, args);
    }

    private Object delegate(Method delegate, Object[] args) throws Throwable {
        try {
            return delegate.invoke(propertiesManager, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Method getDelegateMethod(Method invokedMethod) {
        for (Method delegate : DELEGATES)
            if (equals(invokedMethod, delegate))
                return delegate;
        return null;
    }

    private boolean equals(Method a, Method b) {
        return a.getName().equals(b.getName()) && a.getReturnType().equals(b.getReturnType())
                && Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
    }

    private Object resolveProperty(Method method, Object... args) {
        String key = expandKey(method);

        boolean cachingEnabled = !isFeatureDisabled(method, RESULT_CACHING);

        Object value = null;

        if (args.length == 0 && cachingEnabled) {
            synchronized (preresolvedProperties) {
                value = preresolvedProperties.get(key);
            }

            if (value != null)
                return value;
        }

        value = propertiesManager.getProperty(key);
        if (value == null)
            return null;

        Object result = convert(method, method.getReturnType(), format(method, expandVariables(method, value), args));
        if (result == Converters.NULL)
            return null;

        if (cachingEnabled) {
            synchronized (preresolvedProperties) {
                preresolvedProperties.put(key, result);
            }
        }

        return result;
    }

    private String expandKey(Method method) {
        String key = key(method);
        if (isFeatureDisabled(method, VARIABLE_EXPANSION))
            return key;
        return substitutor.replace(key);
    }

    private Object format(Method method, Object format, Object... args) {
        if (isFeatureDisabled(method, PARAMETER_FORMATTING) || args == null || args.length == 0
                || !(format instanceof String))
            return format;

        return String.format((String) format, args);
    }

    private Object expandVariables(Method method, Object value) {
        if (isFeatureDisabled(method, VARIABLE_EXPANSION))
            return value;

        if (value instanceof String) {
            return substitutor.replace((String) value);
        }

        return value;
    }

    private static Method[] findDelegates() {
        List<Method> result = new LinkedList<Method>();
        Method[] methods = PropertiesManager.class.getMethods();
        for (Method m : methods)
            if (m.getAnnotation(Delegate.class) != null)
                result.add(m);
        return result.toArray(new Method[result.size()]);
    }

    public <T extends Config> void setProxy(T proxy) {
        propertiesManager.setProxy(proxy);
    }

}
