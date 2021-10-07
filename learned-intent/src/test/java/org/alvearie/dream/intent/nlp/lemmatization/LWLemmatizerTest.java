/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.lemmatization;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.junit.BeforeClass;
import org.junit.Test;


public class LWLemmatizerTest {

    private static LWLemmatizer lemmatizer = null;

    @BeforeClass
    public static void setUp() throws Exception {

        lemmatizer = LWLemmatizer.getInstance();
    }

    @Test
    public void testLemmatizer() throws AnalysisEngineProcessException {
//        assertEquals("the quick brown fox jump over the lazy dog.", lemmatizer.lemmatizeText("The quick brown foxes jumped over the lazy dogs."));
    }

    /**
     * Test that text that can't be lemmatized doesn't get lost via lemmatizer
     *
     * @throws AnalysisEngineProcessException
     */
    @Test
    public void testUnLemmatizable() throws AnalysisEngineProcessException {
//        assertEquals("Her2Positive breast cancer", lemmatizer.lemmatizeText("Her2Positive breast cancers"));
    }
}
