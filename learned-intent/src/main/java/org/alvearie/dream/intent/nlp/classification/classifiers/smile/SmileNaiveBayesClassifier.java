/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.classifiers.smile;

import java.io.Serializable;

import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.text.Configuration;

import smile.classification.NaiveBayes;

/**
 * A {@link Classifier} implementation that uses the SMILE libraries.
 * 
 */
public class SmileNaiveBayesClassifier extends SmileClassifier implements Serializable {

    /**
     * Create a new Smile Naive Bayes Classifier
     */
    public SmileNaiveBayesClassifier() {
        this(Configuration.getDefault());
    }

    /**
     * Create a new Smile Navie Bayes Classifier using the provided configuration
     *
     * @param configuration
     */
    public SmileNaiveBayesClassifier(Configuration configuration) {
        super(configuration);
    }

    private static final long serialVersionUID = -5343941263634204914L;
    private NaiveBayes nbc;

    @Override
    void learn(double[][] docMatrix, int numberOfClassifications, int[] classificationArray) {
        int numberOfFeatures = docMatrix == null || docMatrix[0] == null ? 0 : docMatrix[0].length;
//        nbc = new NaiveBayes(NaiveBayes.Model.GENERAL, numberOfClassifications, numberOfFeatures); // doesn't work with "online training"
        nbc = new NaiveBayes(NaiveBayes.Model.MULTINOMIAL, numberOfClassifications, numberOfFeatures);
//        nbc = new NaiveBayes(NaiveBayes.Model.BERNOULLI, numberOfClassifications, numberOfFeatures); // performs very poorly (~40%) for current ground truth
        nbc.learn(docMatrix, classificationArray);
    }

    @Override
    int predict(double[] vector, double[] scores) {
        return nbc.predict(vector, scores);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.classification.Classifier#isTrained()
     */
    @Override
    public boolean isTrained() {
        return nbc != null;
    }
}
