/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Set;

/**
 * <p>Allows a <tt>Config</tt> object to access the contents of the properties, providing utility methods to perform
 * consequent operations.</p> <p/> <p>Example:</p> <p/>
 * <pre>
 *     public interface MyConfig extends Config, Accessible {
 *         int someProperty();
 *     }
 *
 *     public void doSomething() {
 *         MyConfig cfg = ConfigFactory.create(MyConfig.class);
 *         cfg.list(System.out);
 *     }
 * </pre>
 * <p>These methods will print the list of properties, see {@link java.util.Properties#list(java.io.PrintStream)} and
 * {@link java.util.Properties#list(java.io.PrintWriter)}.</p>
 *
 * @author Luigi R. Viggiano
 * @since 1.0.4
 */
public interface Accessible extends Config {

    /**
     * Prints this property list out to the specified output stream. This method is useful for debugging.
     *
     * @param out an output stream.
     * @throws IOException 
     * @throws ClassCastException if any key in this property list is not a string.
     * @see java.util.Properties#list(java.io.PrintStream)
     * @since 1.0.4
     */
    void list(PrintStream out) throws IOException;

    /**
     * Prints this property list out to the specified output stream. This method is useful for debugging.
     *
     * @param out an output stream.
     * @throws ClassCastException if any key in this property list is not a string.
     * @see java.util.Properties#list(java.io.PrintWriter)
     * @since 1.0.4
     */
    void list(PrintWriter out);

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * <code>null</code> if the property is not found.
     *
     * @param   key   the property key.
     * @return  the value in this property list with the specified key value.
     * @see     java.util.Properties#getProperty(String)
     * @since 1.0.4
     */
    Object getProperty(String key);

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns the
     * default value argument if the property is not found.
     *
     * @param   key            the property key.
     * @param   defaultValue   a default value.
     * @return  the value in this property list with the specified key value.
     * @see java.util.Properties#getProperty(String, String)
     *
     * @since 1.0.4
     */
    Object getProperty(String key, Object defaultValue);

    /**
     * Returns a set of keys in this property list
     * including distinct keys in the default property list if a key
     * of the same name has not already been found from the main
     * properties list.
     * <p>
     * The returned set is not backed by the <tt>Properties</tt> object.
     * Changes to this <tt>Properties</tt> are not reflected in the set,
     * or vice versa.
     *
     * @return  a set of keys in this property list, including the keys in the
     *          default property list.
     * @throws  ClassCastException if any key in this property list
     *          is not a string.
     * @see     java.util.Properties#defaults
     * @see     java.util.Properties#stringPropertyNames()
     * @see     java.util.Properties#propertyNames()
     * @since   1.0.5
     */
    Set<String> propertyNames();

}