/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;

/**
 * Maps methods to properties keys and defaultValues. Maps a class to default property values.
 *
 * @author Luigi R. Viggiano
 */
final class PropertiesMapper {

    /** Don't let anyone instantiate this class */
    private PropertiesMapper() {
    }

    static String key(Method method) {
        Key key = method.getAnnotation(Key.class);
        return (key == null) ? method.getName() : key.value();
    }

    static String defaultValue(Method method) {
        DefaultValue defaultValue = method.getAnnotation(DefaultValue.class);
        return defaultValue != null ? defaultValue.value() : null;
    }

    static void defaults(Map<String, Object> properties, Class<? extends Config> clazz) {
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            String key = key(method);
            Object value = defaultValue(method);

            if (value != null) {
                insertRecursive(key, properties, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void insertRecursive(String key, Map<String, Object> container, Object value) {
        if (key.contains(".")) {
            String base = key.substring(0, key.indexOf("."));
            String remainder = key.substring(base.length() + 1, key.length() - 1);

            Map<String, Object> current = (Map<String, Object>) container.get(base);

            if (current == null) {
                current = new HashMap<String, Object>();
                container.put(base, current);
            }

            insertRecursive(remainder, current, value);
        } else {
            container.put(key, value);
        }
    }
}
