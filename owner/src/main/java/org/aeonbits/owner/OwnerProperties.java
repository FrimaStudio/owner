/** This file and its contents are confidential and intended solely for the use of Frima Studio or outside parties permitted to view this file and its contents
 * per agreement between Frima Studio and said parties. Unauthorized publication, use, dissemination, forwarding, printing or copying of this file and its
 * contents is strictly prohibited.
 * 
 * Copyright (c) 2014 Frima Studio Inc. All Rights Reserved */
package org.aeonbits.owner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fred Deschenes
 */
public class OwnerProperties extends HashMap<String, Object> {
    private static final long serialVersionUID = -8284376889570756605L;

    public final static String KEY_SEPARATOR = ".";

    public OwnerProperties() {
        super();
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
}
