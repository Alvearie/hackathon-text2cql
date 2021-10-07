/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.classifiers.smile;

import java.io.Serializable;

import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.text.Configuration;

import smile.classification.RandomForest;

/**
 * A {@link Classifier} implementation that uses the SMILE libraries.
 * 
 */
public class SmileRandomForestClassifier extends SmileClassifier implements Serializable {

    private static final long serialVersionUID = -5343941263634204914L;

    private int featuresPerTree;
    private int numberOfTrees;
    private RandomForest randomForest;

    /**
     * Create a new Smile Random Forest classifier using the default configuration
     */
    public SmileRandomForestClassifier() {
        this(Configuration.getDefault());
    }

    /**
     * Create a new Smile Random Forest classifier using the provided configuration
     *
     * @param configuration
     */
    public SmileRandomForestClassifier(Configuration configuration) {
        super(configuration);
        featuresPerTree = configuration.getFeaturesPerTree();
        numberOfTrees = configuration.getNumberOfTrees();
    }

    @Override
    void learn(double[][] docMatrix, int numberOfClassifications, int[] classificationArray) {
        if (featuresPerTree > 0) {
            randomForest = new RandomForest(docMatrix, classificationArray, numberOfTrees, featuresPerTree);
        } else {
            randomForest = new RandomForest(docMatrix, classificationArray, numberOfTrees);
        }
    }

    @Override
    int predict(double[] vector, double[] scores) {
        return randomForest.predict(vector, scores);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.classification.Classifier#isTrained()
     */
    @Override
    public boolean isTrained() {
        return randomForest != null;
    }
}
