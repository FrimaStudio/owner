/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.event;

import static java.util.Collections.unmodifiableList;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;

/**
 * A semantic event which indicates that a reload occurred.
 *
 * @author Luigi R. Viggiano
 * @see ReloadListener
 * @since 1.0.4
 */
public class ReloadEvent extends Event {

    private final List<PropertyChangeEvent> events;
    private final Map<String, Object> oldProperties;
    private final Map<String, Object> newProperties;

    /**
     * Constructs a prototypical Event.
     *
     * @param source        The object on which the Event initially occurred.
     * @param events        The {@link PropertyChangeEvent change events} regarding which properties have been modified
     *                      during the reload.
     * @param oldProperties the properties before the reload.
     * @param newProperties the properties after the reload.
     * @throws IllegalArgumentException if source is null.
     */
    public ReloadEvent(Object source, List<PropertyChangeEvent> events, Map<String, Object> oldProperties,
            Map<String, Object> newProperties) {
        super(source);
        this.events = unmodifiableList(events);

        //TODO: Make read only
        this.oldProperties = oldProperties;
        this.newProperties = newProperties;
    }

    /**
     * Returns The {@link PropertyChangeEvent change events} regarding which properties have been modified during the
     * reload.
     *
     * @return The {@link PropertyChangeEvent change events} regarding which properties have been modified during the
     *         reload.
     */
    public List<PropertyChangeEvent> getEvents() {
        return events;
    }

    /**
     * Returns the properties before the reload.
     *
     * @return the properties before the reload.
     */
    public Map<String, Object> getOldProperties() {
        return oldProperties;
    }

    /**
     * Returns the properties after the reload.
     *
     * @return the properties after the reload.
     */
    public Map<String, Object> getNewProperties() {
        return newProperties;
    }

}
