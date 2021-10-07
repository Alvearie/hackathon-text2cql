/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors;

import static org.junit.Assert.assertEquals;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.FeatureVector;
import org.alvearie.dream.intent.nlp.text.NGram;
import org.junit.Test;


public class ThresholdVectorizerTest {

    @Test
    public void vectorizeSingleDocument() {
        Document document = new Document("");
        FeatureVector vector = new FeatureVector();
        vector.addFeature(NGram.getNGram("A"), 0.0);
        vector.addFeature(NGram.getNGram("B"), 0.1);
        vector.addFeature(NGram.getNGram("C"), 0.2);
        vector.addFeature(NGram.getNGram("D"), 0.9);
        vector.addFeature(NGram.getNGram("E"), 1.0);
        document.setVector("", vector);
        ThresholdVectorizer vectorizer = new ThresholdVectorizer(0.1);
        vectorizer.vectorize(document);
        FeatureVector thresholdVector = document.getVector();
        Double aValue = thresholdVector.getValue(NGram.getNGram("A"));
        assertEquals(0.0, aValue, 0.0);
        Double bValue = thresholdVector.getValue(NGram.getNGram("B"));
        assertEquals(0.0, aValue, 0.0);
        Double cValue = thresholdVector.getValue(NGram.getNGram("C"));
        assertEquals(1.0, cValue.doubleValue(), 0.0);
        Double dValue = thresholdVector.getValue(NGram.getNGram("D"));
        assertEquals(1.0, dValue.doubleValue(), 0.0);
        Double eValue = thresholdVector.getValue(NGram.getNGram("E"));
        assertEquals(1.0, eValue.doubleValue(), 0.0);

    }
}
