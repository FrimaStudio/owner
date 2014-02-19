/** This file and its contents are confidential and intended solely for the use of Frima Studio or outside parties permitted to view this file and its contents
 * per agreement between Frima Studio and said parties. Unauthorized publication, use, dissemination, forwarding, printing or copying of this file and its
 * contents is strictly prohibited.
 * 
 * Copyright (c) 2014 Frima Studio Inc. All Rights Reserved */
package org.aeonbits.owner.yaml;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.Converter;

/**
 * @author fdeschenes
 *
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
