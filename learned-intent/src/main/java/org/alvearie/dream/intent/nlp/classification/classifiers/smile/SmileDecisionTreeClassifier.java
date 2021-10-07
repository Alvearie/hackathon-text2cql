/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.classifiers.smile;

import java.io.Serializable;

import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.text.Configuration;

import smile.classification.DecisionTree;

/**
 * A {@link Classifier} implementation that uses the SMILE libraries.
 * 
 */
public class SmileDecisionTreeClassifier extends SmileClassifier implements Serializable {

    private static final long serialVersionUID = -5343941263634204914L;
    private DecisionTree decisionTree;
    private int maxNodes;

    /**
     * Create a new Smile Decision Tree classifier
     */
    public SmileDecisionTreeClassifier() {
        this(Configuration.getDefault());
    }

    /**
     * Create a new Smile Decision Tree classifier using the provided configuration
     *
     * @param configuration
     */
    public SmileDecisionTreeClassifier(Configuration configuration) {
        this.maxNodes = configuration.getMaxDecisionTreeNodes();
    }

    @Override
    void learn(double[][] docMatrix, int numberOfClassifications, int[] classificationArray) {
        decisionTree = new DecisionTree(docMatrix, classificationArray, maxNodes);
    }

    @Override
    int predict(double[] vector, double[] scores) {
        return decisionTree.predict(vector, scores);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.classification.Classifier#isTrained()
     */
    @Override
    public boolean isTrained() {
        return decisionTree != null;
    }
}
