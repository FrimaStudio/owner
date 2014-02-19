/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.aeonbits.owner.Config.HotReloadType.SYNC;
import static org.aeonbits.owner.Config.LoadType.FIRST;
import static org.aeonbits.owner.Util.ignore;
import static org.aeonbits.owner.Util.reverse;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Marker interface that must be implemented by all Config sub-interfaces.
 * <p/>
 * Sub-interfaces may also extend {@link Accessible} to allow some debugging facility, or {@link Reloadable} to allow the
 * user to programmatically reload properties.
 *
 * @author Luigi R. Viggiano
 */
public interface Config extends Serializable {

    /**
     * Specifies the policy for loading the properties files. By default the first available properties file specified
     * by {@link Sources} will be loaded, see {@link LoadType#FIRST}. User can also specify that the load policy is
     * {@link LoadType#MERGE} to have the properties files merged: properties are loaded in order from the first file to
     * the last, if there are conflicts in properties names the earlier files loaded prevail.
     *
     * @since 1.0.2
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    @Documented
    @interface LoadPolicy {
        LoadType value() default FIRST;
    }

    /**
     * Specifies the source from which to load the properties file. It has to be specified in a URL string format.
     * Allowed protocols are the ones allowed by {@link java.net.URL} plus
     * <tt>classpath:path/to/resource.properties</tt>
     *
     * @since 1.0.2
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    @Documented
    @interface Sources {
        String[] value();
    }

    /**
     * Default value to be used if no property is found. No quoting (other than normal Java string quoting) is done.
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @Documented
    @interface DefaultValue {
        String value();
    }

    /**
     * The key used for lookup for the property.  If not present, the key will be generated based on the unqualified
     * method name.
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @Documented
    @interface Key {
        String value();
    }

    /**
     * Specifies the policy type to use to load the {@link org.aeonbits.owner.Config.Sources} files for properties.
     *
     * @since 1.0.2
     */
    enum LoadType {

        /**
         * The first available of the specified sources will be loaded.
         */
        FIRST {
            @Override
            OwnerProperties load(List<URL> urls, LoadersManager loaders) {
                OwnerProperties result = new OwnerProperties();
                for (URL url : urls)
                    try {
                        loaders.load(result, url);
                        break;
                    } catch (IOException ex) {
                        // happens when a file specified in the sources is not found or cannot be read.
                        ignore();
                    }
                return result;
            }
        },

        /**
         * All the specified sources will be loaded and merged. If the same property key is
         * specified from more than one source, the one specified first will prevail.
         */
        MERGE {
            @Override
            OwnerProperties load(List<URL> urls, LoadersManager loaders) {
                OwnerProperties result = new OwnerProperties();
                for (URL url : reverse(urls))
                    try {
                        loaders.load(result, url);
                    } catch (IOException ex) {
                        // happens when a file specified in the sources is not found or cannot be read.
                        ignore();
                    }
                return result;
            }
        };

        abstract OwnerProperties load(List<URL> urls, LoadersManager loaders);
    }

    /**
     * Specify that the class implements hot reloading of properties from filesystem baked {@link Sources} (hot
     * reloading can't be applied to all types of URLs).
     * <p/>
     * It is possible to specify an interval to indicate how frequently the library shall check the files for
     * modifications and perform the reload.
     * <p/>
     * Examples:
     * <pre>
     *      &#64;HotReload    // will check for file changes every 5 seconds.
     *      &#64;Sources("file:foo/bar/baz.properties")
     *      interface MyConfig extends Config { ... }
     *
     *      &#64;HotReload(2)    // will check for file changes every 2 seconds.
     *      &#64;Sources("file:foo/bar/baz.properties")
     *      interface MyConfig extends Config { ... }
     *
     *      &#64;HotReload(500, unit = TimeUnit.MILLISECONDS);  // will check for file changes every 500 milliseconds.
     *      &#64;Sources("file:foo/bar/baz.properties")
     *      interface MyConfig extends Config { ... }
     *
     *      &#64;HotReload(type=HotReloadType.ASYNC);  // will use ASYNC reload type: will span a separate thread
     *                                                 // that will check for the file change every 5 seconds (default).
     *      &#64;Sources("file:foo/bar/baz.properties")
     *      interface MyConfig extends Config { ... }
     *
     *      &#64;HotReload(2, type=HotReloadType.ASYNC);  // will use ASYNC reload type and will check every 2 seconds.
     *      &#64;Sources("file:foo/bar/baz.properties")
     *      interface MyConfig extends Config { ... }
     * </pre>
     *
     * <p>
     * To intercept the {@link org.aeonbits.owner.event.ReloadEvent} see {@link Reloadable#addReloadListener(org.aeonbits.owner.event.ReloadListener)}.
     *
     * @since 1.0.4
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    @Documented
    @interface HotReload {
        /**
         * The interval, expressed in seconds (by default), to perform checks on the filesystem to identify modified
         * files and eventually perform the reloading of the properties. By default is 5 seconds.
         */
        long value() default 5;

        /**
         * The time unit for the interval. By default it is {@link TimeUnit#SECONDS}.
         * <p/>
         * Date resolution vary from filesystem to filesystem.<br/>
         * For instance, for Ext3, ReiserFS and HSF+ the date resolution is of 1 second.<br/>
         * For FAT32 the date resolution for the last modified time is 2 seconds. <br/>
         * For Ext4 the date resolution is in nanoseconds.
         * <p/>
         * So, it is a good idea to express the time unit in seconds or more, since higher time resolution
         * will probably not be supported by the underlying filesystem.
         */
        TimeUnit unit() default SECONDS;

        /**
         * The type of HotReload to use. It can be:
         *
         * <p>
         * {@link HotReloadType#SYNC Synchronous}: the configuration file is checked when a method is invoked on the
         * config object. So if the config object is not used for some time, the configuration doesn't get reloaded,
         * until its next usage. i.e. until next method invocation.
         * <p>
         * {@link HotReloadType#ASYNC}: the configuration file is checked by a background thread despite the fact that
         * the config object is used or not.
         */
        HotReloadType type() default SYNC;
    }

    /**
     * Allows to specify which type of HotReload should be applied.
     */
    enum HotReloadType {
        /**
         * The hot reload will happen when one of the methods is invoked on the <tt>Config</tt> class.
         */
        SYNC,

        /**
         * The hot reload will happen in background at the specified interval.
         */
        ASYNC
    }

    /**
     * Specifies to disable some of the features supported by the API.
     * This may be useful in case the user prefers to implement by his own, or just has troubles with something that
     * is unwanted.
     * Features that can be disabled are specified in the enum {@link DisableableFeature}.
     *
     * @since 1.0.4
     */
    @Retention(RUNTIME)
    @Target({ METHOD, TYPE })
    @Documented
    @interface DisableFeature {
        DisableableFeature[] value();
    }

    /**
     * This enum contains the features that can be disabled using the annotation {@link DisableFeature}.
     *
     * @since 1.0.4
     */
    enum DisableableFeature {
        VARIABLE_EXPANSION, PARAMETER_FORMATTING
    }

    /**
     * Specifies simple <tt>{@link String}</tt> as separator to tokenize properties values specified as a
     * single string value, into elements for vectors and collections.
     * The value specified is used as per {@link String#split(String, int)} with int=-1, every element is also
     * trimmed out from spaces using {@link String#trim()}.
     *
     * Notice that {@link TokenizerClass} and {@link Separator} do conflict with each-other when they are both specified
     * together on the same level:
     * <ul>
     *     <li>
     *     You cannot specify {@link TokenizerClass} and {@link Separator} both together on the same method
     *     </li>
     *     <li>
     *     You cannot specify {@link TokenizerClass} and {@link Separator} both together on the same class
     *     </li>
     * </ul>
     * in the two above cases an {@link UnsupportedOperationException} will be thrown when the corresponding conversion
     * is executed.
     *
     * @since 1.0.4
     */
    @Retention(RUNTIME)
    @Target({ METHOD, TYPE })
    @Documented
    @interface Separator {
        /**
         * @return the value specified is used as per {@link java.lang.String#split(String, int)} with int=-1
         */
        String value();
    }

    /**
     * Specifies a <tt>{@link Tokenizer}</tt> class to allow the user to define a custom logic to split
     * the property value into tokens to be used as single elements for vectors and collections.
     *
     * Notice that {@link TokenizerClass} and {@link Separator} do conflict with each-other when they are both specified
     * together on the same level:
     * <ul>
     *     <li>
     *     You cannot specify {@link TokenizerClass} and {@link Separator} both together on the same method
     *     </li>
     *     <li>
     *     You cannot specify {@link TokenizerClass} and {@link Separator} both together on the same class
     *     </li>
     * </ul>
     * in the two above cases an {@link UnsupportedOperationException} will be thrown when the corresponding conversion
     * is executed.
     *
     * @since 1.0.4
     */
    @Retention(RUNTIME)
    @Target({ METHOD, TYPE })
    @Documented
    @interface TokenizerClass {
        Class<? extends Tokenizer> value();
    }

    /**
     * Specifies a <tt>{@link Converter}</tt> class to allow the user to define a custom conversion logic for the
     * type returned by the method. If the method returns a collection, the Converter is used to convert a single
     * element.
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @Documented
    @interface ConverterClass {
        Class<? extends Converter<?>> value();
    }

}
