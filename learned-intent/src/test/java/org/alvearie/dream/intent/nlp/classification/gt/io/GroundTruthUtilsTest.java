/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alvearie.dream.intent.nlp.text.Document;
import org.junit.Test;

/**
 */
public class GroundTruthUtilsTest {

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.classification.gt.io.GroundTruthUtils#findDuplicateCategories(java.util.Map)}.
     */
    @Test
    public void testFindDuplicateCategories() {
        Map<String, Collection<Document>> gt = new HashMap<>();
        gt.put("comedy", Arrays.asList(new Document("Dumb and dumber"), new Document("The office"), new Document("Forrest Gump")));
        gt.put("drama", Arrays.asList(new Document("The godfather"), new Document("Forrest Gump")));
        Map<Document, Collection<String>> duplicates = GroundTruthUtils.findDuplicateCategories(gt);
        assertEquals(1, duplicates.size());
        Collection<String> categories = duplicates.get(new Document("Forrest Gump"));
        assertTrue(categories.contains("comedy"));
        assertTrue(categories.contains("drama"));
    }

    /**
     * Test method for
     * {@link org.alvearie.dream.intent.nlp.classification.gt.io.GroundTruthUtils#substract(java.util.Map, java.util.Map)}.
     */
    @Test
    public void testSubstract() {
        Map<String, Collection<Document>> gt1 = new HashMap<>();
        gt1.put("comedy", new ArrayList<>(Arrays.asList(new Document("Dumb and dumber"), new Document("The office"), new Document("Forrest Gump"))));
        gt1.put("drama", new ArrayList<>(Arrays.asList(new Document("The godfather"), new Document("Forrest Gump"))));

        Map<String, Collection<Document>> gt2 = new HashMap<>();
        gt2.put("comedy", Arrays.asList(new Document("Dumb and dumber"), new Document("The office"), new Document("Forrest Gump")));

        GroundTruthUtils.substract(gt1, gt2);

        // We subtracted the whole comedy category so it should be empty now
        assertTrue(gt1.get("comedy").isEmpty());
        // And drama used to have 2 movies including one Forrest gump which was also in comedy, so drama should only have 1 now
        Collection<Document> drama = gt1.get("drama");
        assertEquals(1, drama.size());
        assertTrue(drama.contains(new Document("The godfather")));
    }
}
