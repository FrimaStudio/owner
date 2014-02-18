/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.event;

import java.util.EventObject;

/**
 * The root event class for all OWNER events.
 *
 * @author Luigi R. Viggiano
 * @since 1.0.4
 */
public class Event extends EventObject {
    private static final long serialVersionUID = 8286431323436674645L;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public Event(Object source) {
        super(source);
    }

}
