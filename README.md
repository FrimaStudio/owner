OWNER
=====

OWNER, an API to ease Java property files usage.

[![Build Status](https://aeonbits.ci.cloudbees.com/job/owner-api/badge/icon)](https://aeonbits.ci.cloudbees.com/job/owner-api/)
[![Build Status](https://travis-ci.org/lviggiano/owner.png?branch=master)](https://travis-ci.org/lviggiano/owner)
[![Coverage Status](https://coveralls.io/repos/lviggiano/owner/badge.png)](https://coveralls.io/r/lviggiano/owner)

[![Built with Maven](http://maven.apache.org/images/logos/maven-feather.png)](http://owner.newinstance.it/maven-site/)
[![Powered by Sonar](http://sheldon.dyndns.tv:9000/images/sonar.png)](http://sheldon.dyndns.tv:9000/dashboard/index/1)

Build pre-requisites
------------
- Maven (tested with 3.1.1, older/newer versions should also work)

Generate Eclipse project
------------
Run 'mvn -npr eclipse:eclipse'.

YAML
------------
This fork was created to implement YAML loading/parsing into OWNER. To do so, we sadly had to let go of some features that we didn't need and didn't have time to port:
- XML configurations are not supported anymore. '.properties' should still work but are not used outside of the unit tests.
- Accessible interface does not have the 'store' method anymore since it's currently impossible to know which format the properties should be saved as. This sadly break a lot of unit tests that relied on properties being able to be saved at runtime :(
- Documentation outdated in some places (Should not really impact users of the library though).

INTRODUCTION
------------

The goal of OWNER API is to minimize the code required to handle
application configuration through Java properties files.

Full documentation available on [project website][website].

BASIC USAGE
-----

The approach used by OWNER APIs, is to define a Java interface
associated to a properties file.

Suppose your properties file is defined
as `ServerConfig.properties`:

```properties
port=80
hostname=foobar.com
maxThreads=100
```

To access this property you need to define a convenient Java
interface in `ServerConfig.java`:

```java
public interface ServerConfig extends Config {
    int port();
    String hostname();
    int maxThreads();
}
```

We'll call this interface the *Properties Mapping Interface* or
just *Mapping Interface* since its goal is to map Properties into
a an easy to use piece of code.

Then, you can use it from inside your code:

```java
public class MyApp {
    public static void main(String[] args) {
        ServerConfig cfg = ConfigFactory.create(ServerConfig.class);
        System.out.println("Server " + cfg.hostname() + ":" + cfg.port() +
                           " will run " + cfg.maxThreads());
    }
}
```

But this is just the tip of the iceberg.

Continue reading here: [Basic usage](http://owner.aeonbits.org/docs/usage/).

DOCUMENTATION
-------------

Make sure to have a look at the documentation on [project website][website]
to learn how flexible and powerful OWNER is, and why you may need it!

  [website]: http://owner.aeonbits.org


LICENSE
-------

OWNER is released under the BSD license.
See [LICENSE][] file included for the details.

  [LICENSE]: https://raw.github.com/lviggiano/owner/master/LICENSE
