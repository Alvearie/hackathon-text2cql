/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.classifiers.smile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.alvearie.dream.intent.nlp.Experiment;
import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.text.Configuration;

import smile.classification.Maxent;

/**
 * A {@link Classifier} implementation that uses the SMILE libraries.
 * 
 */
public class SmileMaxEntClassifier extends SmileClassifier implements Serializable {

    private static final long serialVersionUID = -3626505308802449933L;
    private double lambda;
    private int maxIterations;
    private double tolerance;
    private Maxent maxEntModel;

    /**
     * Create a new Max Entropy classifier using the default configuration
     */
    public SmileMaxEntClassifier() {
        this(Configuration.getDefault());
    }

    /**
     * Create a new Max Entropy classifier using the provided configuration
     *
     * @param configuration
     */
    public SmileMaxEntClassifier(Configuration configuration) {
        super(configuration);
        this.lambda = configuration.getLambda();
        this.maxIterations = configuration.getMaxTrainingIterations();
        this.tolerance = configuration.getTrainingTolerance();
    }

    @Override
    void learn(double[][] docMatrix, int numberOfClassifications, int[] classificationArray) {
        final int[][] intMatrix = new int[docMatrix.length][];
        for (int i = 0; i < intMatrix.length; ++i) {
            intMatrix[i] = convertToMaxEntVector(docMatrix[i]);
        }
        Experiment.save("MaxEntIntMatrix.csv", Arrays.stream(intMatrix).map(String::valueOf).collect(Collectors.joining("\n")));

        maxEntModel = new Maxent(featureSpace.size(), intMatrix, classificationArray, lambda, tolerance, maxIterations);
    }

    @Override
    int predict(double[] vector, double[] scores) {
        final int[] intVector = convertToMaxEntVector(vector);
        return maxEntModel.predict(intVector, scores);
    }

    /**
     * This method converts a vector of doubles indicating weight for each feature at the given index, to a vector of the
     * indexes of features present for this input. The output vector is ordered by the highest value to the lowest value.
     * For example, [0,0,1,0,0,0.1] --> [2,5] and [0.1,0.2,0.3,0,0.4] -> [4,2,1,0]
     *
     * @param v the vector to convert
     * @return the vector of indexes to features present
     */
    private static final int[] convertToMaxEntVector(double[] v) {
        TreeMap<Double, List<Integer>> sorted = new TreeMap<>(Collections.reverseOrder());
        for (int i = 0; i < v.length; i++) {
            if (v[i] > 0) {
                // Make sure features with the same post-TF-IDF value are all saved
                List<Integer> values = sorted.computeIfAbsent(v[i], f -> new ArrayList<Integer>());
                values.add(i);
            }
        }
        int[] maxEntVector = new int[sorted.values().stream().mapToInt(x -> x.size()).sum()];
        int i = 0;
        for (List<Integer> values : sorted.values()) {
            for (int j = 0; j < values.size(); j++) {
                maxEntVector[i++] = values.get(j);
            }
        }
        return maxEntVector;
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.classification.Classifier#isTrained()
     */
    @Override
    public boolean isTrained() {
        return maxEntModel != null;
    }
}
