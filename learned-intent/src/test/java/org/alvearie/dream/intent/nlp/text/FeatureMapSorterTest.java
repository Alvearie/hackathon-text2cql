/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.junit.Test;


public class FeatureMapSorterTest {

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.text.FeatureMapSorter#sort(java.util.Map)}.
     */
    @Test
    public void testSortAllDifferent() {
        NGram a = NGram.getNGram("a");
        NGram b = NGram.getNGram("b");
        NGram c = NGram.getNGram("c");
        NGram d = NGram.getNGram("d");
        Map<Feature, Double> features = new HashMap<>();
        features.put(a, 1.0);
        features.put(b, 2.0);
        features.put(c, 3.0);
        features.put(d, 4.0);
        SortedMap<Feature, Double> sortedFeatures = FeatureMapSorter.sort(features);
        List<Feature> keys = new ArrayList<>(sortedFeatures.keySet());

        // Even though they had been initially put into a hashmap after sorting them desc. by value they will come out in order
        assertEquals(d, keys.get(0));
        assertEquals(c, keys.get(1));
        assertEquals(b, keys.get(2));
        assertEquals(a, keys.get(3));
    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.text.FeatureMapSorter#sort(java.util.Map)}.
     */
    @Test
    public void testSortSomeCollisions() {
        NGram a = NGram.getNGram("a");
        NGram b = NGram.getNGram("b");
        NGram c = NGram.getNGram("c");
        NGram d = NGram.getNGram("d");
        Map<Feature, Double> features = new HashMap<>();
        features.put(a, 0.0);
        features.put(b, 0.0);
        features.put(c, 0.0);
        features.put(d, 0.0);
        SortedMap<Feature, Double> sortedFeatures = FeatureMapSorter.sort(features);
        List<Feature> keys = new ArrayList<>(sortedFeatures.keySet());

        // Even though they had been initially put into a hashmap after sorting they will come out alphabetically
        assertEquals(a, keys.get(0));
        assertEquals(b, keys.get(1));
        assertEquals(c, keys.get(2));
        assertEquals(d, keys.get(3));
    }

}
