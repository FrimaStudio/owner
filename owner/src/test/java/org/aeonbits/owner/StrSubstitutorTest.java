/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * @author Luigi R. Viggiano
 */
public class StrSubstitutorTest {

    @Test
    public void shouldReturnNullWhenNullIsProvided() {
        OwnerProperties props = new OwnerProperties();
        StrSubstitutor substitutor = new StrSubstitutor(props);
        assertNull(substitutor.replace(null));
    }

    @Test
    public void shouldReplaceVariables() {
        OwnerProperties values = new OwnerProperties();
        values.put("animal", "quick brown fox");
        values.put("target", "lazy dog");
        String templateString = "The ${animal} jumped over the ${target}.";
        StrSubstitutor sub = new StrSubstitutor(values);
        String resolvedString = sub.replace(templateString);
        assertEquals("The quick brown fox jumped over the lazy dog.", resolvedString);
    }

    @Test
    public void shouldReplaceVariablesHavingBackslashes() {
        OwnerProperties values = new OwnerProperties();
        values.put("animal", "quick\\brown\\fox");
        values.put("target", "lazy\\dog");
        String templateString = "The\\${animal}\\jumped\\over\\the\\${target}.";
        StrSubstitutor sub = new StrSubstitutor(values);
        String resolvedString = sub.replace(templateString);
        assertEquals("The\\quick\\brown\\fox\\jumped\\over\\the\\lazy\\dog.", resolvedString);
    }

    @Test
    public void shouldReplaceVariablesWithBackSlashesAndShouldWorkWithRecursion() {
        OwnerProperties values = new OwnerProperties();
        values.put("color", "bro\\wn");
        values.put("animal", "qui\\ck\\${color}\\fo\\x");
        values.put("target.attribute", "la\\zy");
        values.put("target.animal", "do\\g");
        values.put("target", "${target.attribute}\\${target.animal}");
        values.put("template", "The ${animal} jum\\ped over the ${target}.");
        values.put("wrapper", "\\foo\\${template}\\bar\\");
        values.put("wrapper2", "\\baz\\${wrapper}\\qux\\");
        StrSubstitutor sub = new StrSubstitutor(values);
        String resolvedString = sub.replace("${wrapper2}");
        assertEquals("\\baz\\\\foo\\The qui\\ck\\bro\\wn\\fo\\x jum\\ped over the la\\zy\\do\\g.\\bar\\\\qux\\",
                resolvedString);
    }

    @Test
    public void testRecoursiveResolution() {
        OwnerProperties values = new OwnerProperties();
        values.put("color", "brown");
        values.put("animal", "quick ${color} fox");
        values.put("target.attribute", "lazy");
        values.put("target.animal", "dog");
        values.put("target", "${target.attribute} ${target.animal}");
        values.put("template", "The ${animal} jumped over the ${target}.");
        String templateString = "${template}";
        StrSubstitutor sub = new StrSubstitutor(values);
        String resolvedString = sub.replace(templateString);
        assertEquals("The quick brown fox jumped over the lazy dog.", resolvedString);
    }

    @Test
    public void testMissingPropertyIsReplacedWithEmptyString() {
        OwnerProperties values = new OwnerProperties() {
            {
                put("foo", "fooValue");
                put("baz", "bazValue");
            }
        };
        String template = "Test: ${foo} ${bar} ${baz} :Test";
        String expected = "Test: fooValue  bazValue :Test";
        String result = new StrSubstitutor(values).replace(template);
        assertEquals(expected, result);
    }

}
