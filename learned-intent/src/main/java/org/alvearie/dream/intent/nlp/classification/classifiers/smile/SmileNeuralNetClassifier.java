/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.classifiers.smile;

import java.io.Serializable;

import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.text.Configuration;

import smile.classification.NeuralNetwork;
import smile.classification.NeuralNetwork.ActivationFunction;
import smile.classification.NeuralNetwork.ErrorFunction;

/**
 * A {@link Classifier} implementation that uses the SMILE libraries.
 * 
 */
public class SmileNeuralNetClassifier extends SmileClassifier implements Serializable {
    private static final long serialVersionUID = -5343941263634204914L;

    private int hiddenLayers[];
    private NeuralNetwork neuralNet;

    /**
     * Create a new Smile Neural Net classifier using the default configuration
     *
     * @param hiddenLayers an array of number of nodes per hidden layer
     */
    public SmileNeuralNetClassifier(int hiddenLayers[]) {
        super();
        this.hiddenLayers = hiddenLayers;
    }

    /**
     * Create a new Smile Neural Net classifier using the provided configuration
     *
     * @param configuration
     * @param hiddenLayers an array of number of nodes per hidden layer
     */
    public SmileNeuralNetClassifier(Configuration configuration, int hiddenLayers[]) {
        super(configuration);
        this.hiddenLayers = hiddenLayers;
    }

    @Override
    void learn(double[][] docMatrix, int numberOfClassifications, int[] classificationArray) {
        if (hiddenLayers == null) {
            hiddenLayers = new int[] { featureSpace.size() * 2 / 3, featureSpace.size() * 2 / 3 };
        }
        int layerSizes[] = new int[hiddenLayers.length + 2];
        layerSizes[0] = featureSpace.size();
        for (int i = 0; i < hiddenLayers.length; i++) {
            layerSizes[i + 1] = hiddenLayers[i];
        }
        layerSizes[layerSizes.length - 1] = classes.size();

        neuralNet = new NeuralNetwork(ErrorFunction.CROSS_ENTROPY, ActivationFunction.SOFTMAX, layerSizes);
        neuralNet.learn(docMatrix, classificationArray);
    }

    @Override
    int predict(double[] vector, double[] scores) {
        return neuralNet.predict(vector, scores);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.classification.Classifier#isTrained()
     */
    @Override
    public boolean isTrained() {
        return neuralNet != null;
    }
}
