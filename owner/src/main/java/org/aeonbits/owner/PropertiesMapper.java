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
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.DefaultValues;
import org.aeonbits.owner.Config.Group;
import org.aeonbits.owner.Config.Key;

/**
 * Maps methods to properties keys and defaultValues. Maps a class to default property values.
 *
 * @author Luigi R. Viggiano
 */
final class PropertiesMapper {

    static final String KEY_SEPARATOR = ".";

    /** Don't let anyone instantiate this class */
    private PropertiesMapper() {
    }

    static String key(Method method) {
        Key key = method.getAnnotation(Key.class);

        Class<?> declaringClass = method.getDeclaringClass();
        Group group = declaringClass.getAnnotation(Group.class);

        String suffix = (key == null) ? method.getName() : key.value();

        if (group == null)
            return suffix;

        String prefix = group.value();

        if (!prefix.endsWith(KEY_SEPARATOR))
            prefix += KEY_SEPARATOR;

        return prefix + suffix;
    }

    static Object defaultValue(Method method) {
        DefaultValue defaultValue = method.getAnnotation(DefaultValue.class);

        if (defaultValue != null) {
            return defaultValue.value();
        }

        DefaultValues defaultValues = method.getAnnotation(DefaultValues.class);

        if (defaultValues != null) {

            if (!method.getReturnType().isAssignableFrom(List.class)) {
                throw new RuntimeException(""); //TODO
            }

            return defaultValues.value();
        }

        return null;
    }

    static void defaults(OwnerProperties properties, Class<? extends Config> clazz) {
        Method[] methods = clazz.getMethods();

        Map<String, Object> defaults = new HashMap<String, Object>();

        for (Method method : methods) {
            String key = key(method);
            Object value = defaultValue(method);

            if (value != null) {
                defaults.put(key, value);

            }
        }

        properties.putAll(defaults);
    }
}
