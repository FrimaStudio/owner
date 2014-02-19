/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */
package org.aeonbits.owner;

import static org.aeonbits.owner.Util.propertiesToMap;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <p>
 * Container for loaded properties. This is only a simple wrapper around an {@link HashMap} that allows pretty
 * much any type of configuration (from a '.properties' file to JSON, XML and YAML) that can be stored as a HashMap<String, ? extends Object>
 * to be stored and accessed.
 * </p>
 * <p>
 * Known issues : 
 *      - The {@link #containsKey(String)} method currently isn't recursive (dotted properties might not be found)
 * </p>
 * 
 * @author Fred Deschenes
 */
public class OwnerProperties extends HashMap<String, Object> {
    private static final long serialVersionUID = -8284376889570756605L;

    /**
     * Separator for dotted (ex: "my.property") property keys
     */
    public final static String KEY_SEPARATOR = ".";

    /**
     * @see HashMap#HashMap()
     */
    public OwnerProperties() {
        super();
    }

    /**
     * @see HashMap#HashMap(int)
     */
    public OwnerProperties(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @see HashMap#HashMap(int, float)
     */
    public OwnerProperties(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * @see HashMap#HashMap(Map)
     */
    public OwnerProperties(Map<? extends String, ? extends Object> m) {
        super(m);
    }

    /**
     * Creates a new OwnerProperties instance and initializes it with the values stored in the specified Properties object
     * @param props     The properties to initialize the map with
     */
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

    /**
     * Searches the properties for the specified key.
     * @param key       The key to search for
     * @return  The found value, null otherwise
     */
    public Object get(String key) {
        if (containsKey(key))
            return super.get(key);

        return resolve(key, this);
    }

    /**
     * @deprecated
     * @see OwnerProperties#containsKey(String)
     */
    @Override
    @Deprecated
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            return containsKey((String) key);
        }

        return false;
    }

    /**
     * WARNING: This currently is not recursive, so asking for a 'dotted' key (ex: "my.config.key") will not work in certain conditions.
     * @param key       The key to search for
     * @see Map#containsKey(Object)
     */
    public boolean containsKey(String key) {
        //TODO: Fix recursive keys (without breaking resolve and merge methods)
        //We could use 'keySetRecursive().contains', but that'd be pretty slow
        return super.containsKey(key);
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

    /**
     * @see Map#putAll(Map)
     */
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

    /**
     * @see     Map#keySet()
     */
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

        return new StringBuilder(prefix).append(KEY_SEPARATOR).append(suffix).toString();
    }

    /**
     * Dumps the backing Map object to the specified PrintStream
     * @param out       The stream to print to
     * @throws IOException
     */
    public void list(PrintStream out) throws IOException {
        //TODO: Better formatting?

        try {
            byte[] data = super.toString().getBytes("UTF-8");

            out.write(data);
        } catch (UnsupportedEncodingException e) {
            //Should never happen...
        }
    }

    /**
     * Dumps the backing Map object to the specified PrintWriter
     * @param out       The writer to write to
     */
    public void list(PrintWriter out) {
        //TODO: Better formatting
        out.write(super.toString());
    }
}
