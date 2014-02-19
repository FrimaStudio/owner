/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.yaml;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.Converter;

/**
 * @author Fred Deschenes
 */
public class ServerConverter implements Converter<List<Server>> {
    @SuppressWarnings("unchecked")
    public List<Server> convert(Method method, Object value) {
        if (value instanceof List<?>) {
            List<Map<String, Object>> values = (List<Map<String, Object>>) value;
            List<Server> result = new ArrayList<Server>(values.size());

            for (Map<String, Object> rawServer : values) {
                Server temp = new Server();
                temp.host = (String) rawServer.get("host");
                temp.port = (Integer) rawServer.get("port");

                result.add(temp);
            }

            return result;
        }

        return null;
    }
}
