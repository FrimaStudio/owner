/** This file and its contents are confidential and intended solely for the use of Frima Studio or outside parties permitted to view this file and its contents
 * per agreement between Frima Studio and said parties. Unauthorized publication, use, dissemination, forwarding, printing or copying of this file and its
 * contents is strictly prohibited.
 * 
 * Copyright (c) 2014 Frima Studio Inc. All Rights Reserved */
package org.aeonbits.owner.yaml;

/**
 * @author fdeschenes
 *
 */
public class Server {
    public String host;
    public int port;

    @Override
    public String toString() {
        return String.format("[Server host=%s, port=%s]", host, port);
    }
}
