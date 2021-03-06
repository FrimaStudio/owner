/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.yaml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fred Deschenes
 */
public class YAMLTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Sources({ "classpath:org/aeonbits/owner/yaml/${env}.yaml", "classpath:org/aeonbits/owner/yaml/default.yaml" })
    public interface MyConfig extends Config {
        @Key("foo.bar")
        public int someInt(); //1234

        @Key("foo.baz")
        public String someString(); //"something"

        @Key("servers")
        @ConverterClass(ServerConverter.class)
        public List<Server> servers();

        @Key("expansion.toExpand")
        public String expansionTest(); //"Lorem ipsum dolor sit amet"

        @Key("this.key.doesnt.exist")
        @DefaultValue("1234")
        public int defaultValue();
    }

    private static final String ENVIRONMENT = "live";

    @Before
    public void setup() {
        ConfigFactory.setProperty("env", ENVIRONMENT);
    }

    @Test
    public void testYamlConfigLoading() {
        MyConfig config = ConfigFactory.create(MyConfig.class);

        assertEquals(1234, config.someInt());
        assertEquals("something", config.someString());

        List<Server> servers = config.servers();

        assertNotNull(servers);
        assertEquals(2, servers.size());

        Server first = servers.get(0);
        Server second = servers.get(1);

        assertEquals("firstserver.com", first.host);
        assertEquals(1234, first.port);

        assertEquals("secondserver.com", second.host);
        assertEquals(4321, second.port);

        assertEquals("Lorem ipsum dolor sit amet", config.expansionTest());
        assertEquals(1234, config.defaultValue());
    }
}
