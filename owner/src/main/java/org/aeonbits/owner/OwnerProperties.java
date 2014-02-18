/** This file and its contents are confidential and intended solely for the use of Frima Studio or outside parties permitted to view this file and its contents
 * per agreement between Frima Studio and said parties. Unauthorized publication, use, dissemination, forwarding, printing or copying of this file and its
 * contents is strictly prohibited.
 * 
 * Copyright (c) 2014 Frima Studio Inc. All Rights Reserved */
package org.aeonbits.owner;

import static org.aeonbits.owner.Util.propertiesToMap;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Fred Deschenes
 */
public class OwnerProperties extends HashMap<String, Object> {
    private static final long serialVersionUID = -8284376889570756605L;

    public final static String KEY_SEPARATOR = ".";

    public OwnerProperties() {
        super();
    }

    public OwnerProperties(int initialCapacity) {
        super(initialCapacity);
    }

    public OwnerProperties(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public OwnerProperties(Map<? extends String, ? extends Object> m) {
        super(m);
    }

    public OwnerProperties(Properties props) {
        this(propertiesToMap(props));
    }

    /**
     * @deprecated
     * @see OwnerProperties#get(String)
     */
    @Override
    @Deprecated
    public Object get(Object key) {
        if (key instanceof String) {
            return get((String) key);
        }

        return null;
    }

    public Object get(String key) {
        if (containsKey(key))
            return super.get(key);

        return resolve(key, this);
    }

    @SuppressWarnings("unchecked")
    private static Object resolve(String key, Map<String, ? extends Object> from) {
        if (from.containsKey(key)) {
            return from.get(key);
        } else if (key.contains(KEY_SEPARATOR)) {
            int firstIndex = key.indexOf(KEY_SEPARATOR);
            String baseKey = key.substring(0, firstIndex);
            Object value = from.get(baseKey);

            if (value instanceof Map<?, ?>) {
                String rest = key.substring(firstIndex + 1);
                return resolve(rest, (Map<String, ? extends Object>) value);
            }

            return null;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map<? extends String, ? extends Object> from) {
        merge((Map<String, Object>) from, this);
    }

    @SuppressWarnings({ "unchecked" })
    private static void merge(Map<String, Object> from, Map<String, Object> into) {
        for (String key : from.keySet()) {
            Object fromValue = from.get(key);

            if (into.containsKey(key)) {
                Object intoValue = into.get(key);

                if (intoValue instanceof Map<?, ?> && fromValue instanceof Map<?, ?>) {
                    merge((Map<String, Object>) fromValue, (Map<String, Object>) intoValue);
                    continue;
                }
            }

            into.put(key, fromValue);
        }
    }

    public Set<String> keySetRecursive() {
        Set<String> keySet = new LinkedHashSet<String>();

        recursiveKeySet(keySet, this, null);

        return keySet;
    }

    @SuppressWarnings("unchecked")
    private static void recursiveKeySet(Set<String> insertInto, Map<String, Object> root, String prefix) {
        for (String key : root.keySet()) {
            String realKey = buildKey(prefix, key);
            insertInto.add(realKey);

            Object value = root.get(key);

            if (value instanceof Map<?, ?>) {
                recursiveKeySet(insertInto, (Map<String, Object>) value, realKey);
            }
        }
    }

    private static String buildKey(String prefix, String suffix) {
        if (prefix == null || prefix.isEmpty()) {
            return suffix;
        }

        return prefix + KEY_SEPARATOR + suffix;
    }
}