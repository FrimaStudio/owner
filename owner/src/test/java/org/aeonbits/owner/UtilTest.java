/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import static org.aeonbits.owner.Util.unreachableButCompilerNeedsThis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.aeonbits.owner.Util.SystemProvider;
import org.aeonbits.owner.util.Collections;
import org.junit.Test;

/**
 * This class contains tests for the {@link Util} class as well utility methods used for test classes.
 *
 * @author Luigi R. Viggiano
 */
public class UtilTest {

    public static SystemProvider setSystem(Object system) {
        SystemProvider save = Util.system;
        Util.system = (SystemProvider) system;
        return save;
    }

    public static OwnerProperties getSystemProperties() {
        return Util.system().getProperties();
    }

    public SystemProvider system() {
        return Util.system();
    }

    @Test
    public void testReverse() {
        Integer[] i = { 1, 2, 3, 4, 5 };
        Integer[] result = Util.reverse(i);
        assertTrue(Arrays.equals(new Integer[] { 1, 2, 3, 4, 5 }, i));
        assertTrue(Arrays.equals(new Integer[] { 5, 4, 3, 2, 1 }, result));
    }

    @Test
    public void testIgnore() {
        Object result = ignore();
        assertNull(result);
    }

    @Test
    public void testUnreachable() {
        try {
            unreachableButCompilerNeedsThis();
        } catch (AssertionError err) {
            assertEquals("this code should never be reached", err.getMessage());
        }
    }

    public static void delete(File target) {
        target.delete();
    }

    public static void debug(String format, Object... args) {
        if (Boolean.getBoolean("debug"))
            System.out.printf(format, args);
    }

    public static <T> T ignore() {
        return Util.ignore();
    }

    public static File fileFromURL(String spec) throws MalformedURLException {
        return Util.fileFromURL(spec);
    }

    public static Object getSystemProperty(String key) {
        return Util.system().getProperty(key);
    }

    public static Object getenv(String home) {
        return Util.system().getenv().get(home);
    }

    public static OwnerProperties getenv() {
        return Util.system().getenv();
    }

    public static interface MyCloneable extends Cloneable {
        // for some stupid reason java.lang.Cloneable doesn't define this method...
        public Object clone() throws CloneNotSupportedException;
    }

    @SuppressWarnings("unchecked")
    public static <T extends MyCloneable> T[] newArray(int size, T cloneable) throws CloneNotSupportedException {
        Object array = Array.newInstance(cloneable.getClass(), size);
        Array.set(array, 0, cloneable);
        for (int i = 1; i < size; i++)
            Array.set(array, i, cloneable.clone());
        return (T[]) array;
    }

    public static boolean eq(Object o1, Object o2) {
        return Util.eq(o1, o2);
    }

    @Test
    public void testExpandUserHomeOnUnix() {
        SystemProvider save = UtilTest.setSystem(new SystemProviderForTest(new OwnerProperties(Collections.map(
                "user.home", "/home/john")), new OwnerProperties()));

        try {
            assertEquals("/home/john", Util.expandUserHome("~"));
            assertEquals("/home/john/foo/bar/", Util.expandUserHome("~/foo/bar/"));
            assertEquals("file:/home/john/foo/bar/", Util.expandUserHome("file:~/foo/bar/"));
            assertEquals("jar:file:/home/john/foo/bar/", Util.expandUserHome("jar:file:~/foo/bar/"));

            assertEquals("/home/john\\foo\\bar\\", Util.expandUserHome("~\\foo\\bar\\"));
            assertEquals("file:/home/john\\foo\\bar\\", Util.expandUserHome("file:~\\foo\\bar\\"));
            assertEquals("jar:file:/home/john\\foo\\bar\\", Util.expandUserHome("jar:file:~\\foo\\bar\\"));
        } finally {
            UtilTest.setSystem(save);
        }
    }

    @Test
    public void testExpandUserHomeOnWindows() {
        SystemProvider save = UtilTest.setSystem(new SystemProviderForTest(new OwnerProperties(Collections.map(
                "user.home", "C:\\Users\\John")), new OwnerProperties()));

        try {
            assertEquals("C:\\Users\\John", Util.expandUserHome("~"));
            assertEquals("C:\\Users\\John/foo/bar/", Util.expandUserHome("~/foo/bar/"));
            assertEquals("file:C:\\Users\\John/foo/bar/", Util.expandUserHome("file:~/foo/bar/"));
            assertEquals("jar:file:C:\\Users\\John/foo/bar/", Util.expandUserHome("jar:file:~/foo/bar/"));

            assertEquals("C:\\Users\\John\\foo\\bar\\", Util.expandUserHome("~\\foo\\bar\\"));
            assertEquals("file:C:\\Users\\John\\foo\\bar\\", Util.expandUserHome("file:~\\foo\\bar\\"));
            assertEquals("jar:file:C:\\Users\\John\\foo\\bar\\", Util.expandUserHome("jar:file:~\\foo\\bar\\"));
        } finally {
            UtilTest.setSystem(save);
        }
    }

}
