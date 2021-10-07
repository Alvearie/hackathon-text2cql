/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors;

import java.util.Map.Entry;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentVectorizer;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.FeatureVector;

/**
 * This class will compare each feature value to a given threshold, and generate a corresponding vector for any features
 * that pass the threshold with 1.0 values.
 *
 */
public class ThresholdVectorizer implements DocumentVectorizer {

    double min = 0.0;

    /**
     * Initialize this vectorizer with a minimum value that must be exceeded in order for the feature to be kept.
     *
     * @param min
     */
    public ThresholdVectorizer(double min) {
        this.min = min;
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.DocumentVectorizer#vectorize(org.alvearie.nlp.text.Document)
     */
    @Override
    public void vectorize(Document document) {
        FeatureVector vector = new FeatureVector(document.getVector().size());
        for (Entry<Feature, Double> entry : document.getVector().getFeaturesAndValues().entrySet()) {
            Double value = entry.getValue();
            if (value > min) {
                vector.addFeature(entry.getKey(), 1.0);
            }
        }
        document.setVector("Threshold", vector);
    }
}
