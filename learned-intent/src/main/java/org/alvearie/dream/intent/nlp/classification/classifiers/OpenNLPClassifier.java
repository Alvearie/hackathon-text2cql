/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.classifiers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.alvearie.dream.intent.nlp.classification.Classification;
import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.text.Document;
import org.apache.log4j.Logger;

import opennlp.tools.doccat.BagOfWordsFeatureGenerator;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizer;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.FeatureGenerator;
import opennlp.tools.doccat.NGramFeatureGenerator;
import opennlp.tools.ml.AbstractTrainer;
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

/**
 * A {@link Classifier} implementation that uses the Apache Open NLP libraries.
 *
 */
public class OpenNLPClassifier implements Classifier {

    /**
     * Inner class to convert our training data to DocumentSamples, for OpenNLP to parse
     *
     */
    private class TrainingDataDocumentSampleStream implements ObjectStream<DocumentSample> {
        List<DocumentSample> trainingSamples = new ArrayList<>();

        public TrainingDataDocumentSampleStream(Map<String, Collection<Document>> trainingData) {
            for (String category : trainingData.keySet()) {
                Collection<Document> documents = trainingData.get(category);
                for (Document document : documents) {
                    trainingSamples.add(new DocumentSample(category, cleanWords(document.getText())));
                }
            }
        }

        @Override
        public DocumentSample read() throws IOException {
            if (trainingSamples.size() == 0) {
                return null;
            }
            return trainingSamples.remove(0);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(OpenNLPClassifier.class.getName());

    /**
     * This method will take a string and convert it to an array of words, omitting useless characters
     *
     * @param s
     * @return
     */
    static String[] cleanWords(String s) {
        List<String> words = new ArrayList<>();
        for (String word : s.replaceAll("[^A-Za-z]", " ").split("\\s")) {
            word = word.toLowerCase();
            word = word.trim();
            if (word.length() > 0) {
                // if (word.length() > 1 && !STOP_WORDS.contains(word)) {
                words.add(word);
                // }
            }
        }
        return words.toArray(new String[words.size()]);
    }

    /**
     * @return the {@link TrainingParameters} for the classifier
     */
    private static TrainingParameters getTrainingParameters() {
        TrainingParameters params = TrainingParameters.defaultParams();
        // Naive Bayes was found to generate better confidence for the
        params.put(AbstractTrainer.ALGORITHM_PARAM, NaiveBayesTrainer.NAIVE_BAYES_VALUE);
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(0));
        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(100));
        return params;
    }

    private DoccatModel model;

    /**
     * Create an OpenNLP-based classifier.
     */
    public OpenNLPClassifier() {
    }

    @Override
    public List<Classification> classify(Document document) {
        if (model == null) {
            throw new IllegalStateException("This classifier has not been trained yet. Use the train() method first.");
        }
        DocumentCategorizer classifier = new DocumentCategorizerME(model);

        String cleanWords[] = cleanWords(document.getText());

        SortedMap<Double, Set<String>> sortedScoreMap = classifier.sortedScoreMap(cleanWords);
        List<Classification> classifications = new ArrayList<>();

        for (Entry<Double, Set<String>> entry : sortedScoreMap.entrySet()) {
            Double score = entry.getKey();
            for (String classification : entry.getValue()) {
                classifications.add(0, new Classification(classification, score, document.getText()));
            }
        }
        return classifications;
    }

    @Override
    public void save(File file) throws IOException {
        try (BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(file))) {
            model.serialize(modelOut);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.alvearie.criteria.classification.CriteriaClassifier#train(java.util.Map)
     */
    @Override
    public void train(Map<String, Collection<Document>> trainingData) {
        if (trainingData == null) {
            throw new NullPointerException("The training data was null.");
        }
        if (trainingData.isEmpty()) {
            throw new IllegalArgumentException("The training data is empty.");
        }

        int minNgramSize = 1;
        int maxNgramSize = 4;

        FeatureGenerator[] featureGenerator;
        try {
            featureGenerator = new FeatureGenerator[] {
                    new BagOfWordsFeatureGenerator(),
                    new NGramFeatureGenerator(minNgramSize, maxNgramSize) };
        } catch (InvalidFormatException e) {
            LOGGER.error("Invalid N-gram format passed to the N-Gram feature generator: ", e);
            return;
        }

        DoccatFactory customFactory = new DoccatFactory(featureGenerator);
        try (ObjectStream<DocumentSample> trainingSetStream = new TrainingDataDocumentSampleStream(trainingData)) {
            model = DocumentCategorizerME.train("en", trainingSetStream, getTrainingParameters(), customFactory);
            model.getFactory().setFeatureGenerators(featureGenerator); // Fixes bug in OpenNLP where feature
                                                                       // generators
                                                                       // get null constructor
        } catch (IOException e) {
            LOGGER.error("Error building the NLP model: ", e);
        }
    }

    @Override
    public boolean isTrained() {
        return model != null;
    }
}
