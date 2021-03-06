/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import java.beans.PropertyChangeListener;

/**
 * <p>Allows a <tt>Config</tt> object to change its property values at runtime.</p> <p/> <p>Example:</p>
 * <p/>
 * <pre>
 *     public interface MyConfig extends Config, Mutable {
 *         &#64;DefaultValue("18")
 *         int minAge();
 *     }
 *
 *     public void example() {
 *         MyConfig cfg = ConfigFactory.create(MyConfig.class);
 *         int before = cfg.minAge();                 // before = 18
 *         int old = cfg.setProperty("minAge", "21"); // old = 18
 *         int after = cfg.minAge();                  // after = 21
 *         int old2 = cfg.removeProperty("minAge");   // old2 = 21
 *         int end = cfg.minAge();                    // end = null
 *     }
 * </pre>
 *
 * @author Luigi R. Viggiano
 * @since 1.0.4
 */
public interface Mutable extends Config {

    /**
     * <p>Sets a given property to the specified value.</p> <p/> <p>Differently than {@link
     * java.util.Properties#setProperty(String, String)}, if <tt>key</tt> is set to <tt>null</tt> then this call is
     * equivalent to {@link #removeProperty(String)}.</p>
     *
     * @param key   the key to be placed into the property list.
     * @param value the value corresponding to <tt>key</tt>, or <tt>null</tt> if the property must be removed.
     * @return the previous value of the specified key, or <code>null</code> if it did not have one.
     * @since 1.0.4
     */
    Object setProperty(String key, Object value);

    /**
     * Removes a given property.
     *
     * @param key the key of the property to remove.
     * @return the previous value of the specified key, or <code>null</code> if it did not have one.
     * @see java.util.Hashtable#remove(Object)
     * @since 1.0.4
     */
    Object removeProperty(String key);

    /**
     * Clears all properties.
     *
     * @since 1.0.4
     */
    void clear();

    /**
     * Adds a {@link PropertyChangeListener} to the Mutable interface.
     *
     * @param listener the listener to be added.
     * @since 1.0.5
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a {@link PropertyChangeListener} from the Mutable interface.
     *
     * @param listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Adds a PropertyChangeListener to the listener list for a specific
     * property.
     * If <code>propertyName</code> or <code>listener</code> is <code>null</code>,
     * no exception is thrown and no action is taken.
     *
     * @param propertyName one of the property names listed above
     * @param listener the property change listener to be added
     */
    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

}
