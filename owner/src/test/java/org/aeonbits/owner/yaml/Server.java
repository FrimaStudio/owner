/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.yaml;

/**
 * @author Fred Deschenes
 */
public class Server {
    public String host;
    public int port;

    @Override
    public String toString() {
        return String.format("[Server host=%s, port=%s]", host, port);
    }
}
