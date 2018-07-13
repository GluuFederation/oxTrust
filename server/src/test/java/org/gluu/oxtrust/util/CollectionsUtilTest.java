package org.gluu.oxtrust.util;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.*;

public class CollectionsUtilTest {

    @Test
    public void testEqualsUnordered() {
        assertTrue(CollectionsUtil.equalsUnordered(Arrays.asList("Mehdi", "AREZKI"), Arrays.asList("Mehdi", "AREZKI")));
        assertTrue(CollectionsUtil.equalsUnordered(Arrays.asList("Mehdi", "AREZKI"), Arrays.asList("AREZKI", "Mehdi")));
        assertFalse(CollectionsUtil.equalsUnordered(Arrays.asList("mehdi", "AREZKI"), Arrays.asList("AREZKI", "Mehdi")));
        assertFalse(CollectionsUtil.equalsUnordered(Arrays.asList("Mehdi", "AREZKI"), Arrays.asList("Mehdi")));

        assertTrue(CollectionsUtil.equalsUnordered(Arrays.asList(1, 2), Arrays.asList(1, 2)));
        assertFalse(CollectionsUtil.equalsUnordered(Arrays.asList(1, 2), Arrays.asList(1, 1)));
    }

    @Test
    public void testFirst() {
        assertTrue(CollectionsUtil.first(Arrays.asList("Mehdi")).isPresent());
        assertEquals("Mehdi", CollectionsUtil.first(Arrays.asList("Mehdi")).get());
        assertEquals("Mehdi", CollectionsUtil.first(Arrays.asList("Mehdi", "AREZKI")).get());

        assertFalse(CollectionsUtil.first(Collections.emptyList()).isPresent());
        assertFalse(CollectionsUtil.first(null).isPresent());
    }
}