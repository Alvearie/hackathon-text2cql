/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.classification.classifiers.smile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.alvearie.dream.intent.nlp.Experiment;
import org.alvearie.dream.intent.nlp.classification.Classification;
import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTextTransformer;
import org.alvearie.dream.intent.nlp.text.DocumentTokenizer;
import org.alvearie.dream.intent.nlp.text.DocumentVectorizer;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.FeatureSpace;
import org.alvearie.dream.intent.nlp.text.FeatureVector;
import org.alvearie.dream.intent.nlp.text.NGrammer;
import org.alvearie.dream.intent.nlp.text.Utils;
import org.alvearie.dream.intent.nlp.text.processors.RegexBoWVectorizer;
import org.alvearie.dream.intent.nlp.text.processors.TFIDFVectorizer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentNGrammer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentNormalizer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentTokenizer;
import org.apache.log4j.Logger;

/**
 * A {@link Classifier} implementation that uses the SMILE libraries.
 *
 */
public abstract class SmileClassifier implements Classifier, Serializable {

    private static final Logger LOGGER = Logger.getLogger(SmileClassifier.class.getName());
    private static final long serialVersionUID = -5343941263634204914L;

    protected Configuration configuration;

    /**
     * Create a new Smile-based classifier, using the default configuration
     */
    public SmileClassifier() {
        this(Experiment.getCurrentExperiment() != null ? Experiment.getCurrentExperiment().getConfiguration() : Configuration.getDefault());
    }

    /**
     * Create a new Smile-based classifier using the provided configuration
     *
     * @param configuration
     */
    public SmileClassifier(Configuration configuration) {
        this.configuration = configuration;
        this.tfidfVectorizer = new TFIDFVectorizer(configuration);
    }

    List<String> classes; // List of classes, must keep order

    private DocumentVectorizer tfidfVectorizer;
    final FeatureSpace featureSpace = new FeatureSpace();

    /* (non-Javadoc)
     * @see org.alvearie.nlp.classification.Classifier#classify(org.alvearie.nlp.text.Document)
     */
    @Override
    public List<Classification> classify(Document doc) {
        if (doc.getVector() == null) {
            doc.setFeatureSpace(featureSpace);
            processDocuments(Arrays.asList(doc));
        }
        int prediction = -1; // Default to no prediction
        double scores[] = new double[classes.size()];
        if (!doc.getVector().isEmpty()) { // if features are found, predict the classifications
            double docVector[] = doc.getVector().toDenseVector(featureSpace).toArray();
            Experiment.save("TestDocument.ser", doc);
            Experiment.save("TestDocVector.csv", Arrays.stream(docVector).mapToObj(String::valueOf).collect(Collectors.joining(", ")));
            Experiment.save("Features.csv", featureSpace.stream().map(Feature::getFeature).collect(Collectors.joining("\n")));

            prediction = predict(docVector, scores);
        }
        List<Classification> classifications = new ArrayList<>();
        if (prediction >= 0) {
            for (int i = 0; i < scores.length; i++) {
                classifications.add(new Classification(classes.get(i), scores[i], doc.getOriginalText()));
            }
        } else {
            // No prediction can be made - this can be caused by a document that contains only ngrams never seen before.
            for (int i = 0; i < classes.size(); i++) {
                classifications.add(new Classification(classes.get(i), 0.0, doc.getOriginalText()));
            }
        }
        Collections.sort(classifications);
        return classifications;
    }

    /**
     * Learn a model based on the given data
     *
     * @param docMatrix
     * @param numberOfClassifications
     * @param classificationArray
     */
    abstract void learn(double[][] docMatrix, int numberOfClassifications, int[] classificationArray);

    /**
     * Predict the classification of the vector passed in
     *
     * @param vector a vector of feature values
     * @param scores an array of scores for each possible classification
     * @return
     */
    abstract int predict(double[] vector, double[] scores);

    /**
     * This method will process one document to prepare (ngram/vectorize/etc) it for classification
     *
     * @param document
     */
    public void processDocument(Document document) {
        processDocuments(Arrays.asList(document));
    }

    /**
     * This method will process all documents to prepare (ngram/vectorize/etc) them for classification.
     *
     * @param allDocuments
     */
    public void processDocuments(List<Document> allDocuments) {
        DocumentTextTransformer normalizer = new SmileDocumentNormalizer(configuration);
        Utils.stream(allDocuments).forEach(normalizer::processText);

        DocumentTokenizer tokenizer = new SmileDocumentTokenizer();
        allDocuments.forEach(tokenizer::tokenize);
        Corpus corpus = new Corpus(allDocuments);
        if (!featureSpace.isEmpty()) {
            corpus.setFeatureSpace(featureSpace);
        }

        NGrammer nGrammer = new SmileDocumentNGrammer(configuration);
        nGrammer.ngram(corpus);

        if (featureSpace.isEmpty()) {
            featureSpace.addAll(corpus.getFeatureSpace());
            // TODO Experimenting with other processing techniques. Remove if we end up not using.
            // features.add(NoteFeatureProcessor.NOTE_FEATURE);
            System.err.println("Found " + featureSpace.size() + " unique features");
        }
        new RegexBoWVectorizer().vectorize(corpus);
        // TODO Experimenting with other processing techniques. Remove if we end up not using.
        // new NoteFeatureProcessor().vectorize(allDocuments, features);

        tfidfVectorizer.vectorize(corpus);
        // ThresholdVectorizer thresholdVectorizer = new ThresholdVectorizer(0.1);
        // thresholdVectorizer.vectorize(allDocuments);
    }

    @Override
    public void save(File file) throws IOException {
        LOGGER.info("Saving to: " + file.getAbsolutePath());
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file))) {
            os.writeObject(this);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alvearie.criteria.classification.CriteriaClassifier#train(java.util.Map)
     */
    @Override
    public void train(Map<String, Collection<Document>> trainingData) {
        // Map of document to classificationIndex. Must respect key order!!
        Map<Document, Integer> allDocuments = new LinkedHashMap<>();
        classes = new ArrayList<>(); // ordered list of classification names
        for (Entry<String, Collection<Document>> entry : trainingData.entrySet()) {
            String category = entry.getKey();
            classes.add(category);
            entry.getValue().stream().forEach(d -> allDocuments.put(d, classes.size()-1));
        }

        processDocuments(new ArrayList<>(allDocuments.keySet()));
        // Don't include ground truth entries that map to no features. This causes poorly trained models.
        allDocuments.entrySet().removeIf(e -> e.getKey().getNGrams().isEmpty());
        //
        final int labelArray[] = new int[allDocuments.size()];
        AtomicInteger l = new AtomicInteger(0);
        allDocuments.entrySet().stream().forEach(e -> labelArray[l.getAndIncrement()] = e.getValue());

        List<FeatureVector> vectors = allDocuments.entrySet().stream().map(e -> {
            FeatureVector vector = e.getKey().getVector();
            if (vector == null) {
                throw new IllegalStateException(
                        "One or more documents in the corpus are not vectorized. Documents need to be vectorized in order to be classified.");
            }
            vector = vector.toDenseVector(featureSpace);
            return vector;
        }).collect(Collectors.toList());

        double[][] allDocVectors = FeatureVector.toMatrix(vectors);
        saveTrainingData(trainingData, allDocVectors, labelArray, classes, featureSpace);

        LOGGER.info("Starting to train classification model.");
        learn(allDocVectors, classes.size(), labelArray);
    }

    /**
     * Save all training-related data to the current experiment directory (if applicable)
     *
     * @param trainingData
     * @param allDocVectors
     * @param labelArray
     * @param classes
     * @param featureSpace
     */
    private void saveTrainingData(Map<String, Collection<Document>> trainingData, double[][] allDocVectors, int labelArray[], List<String> classes, FeatureSpace featureSpace) {
        Experiment currentExperiment = Experiment.getCurrentExperiment();
        if (currentExperiment != null) {
            StringBuilder trainingString = new StringBuilder();
            trainingData.entrySet().stream().forEach(e -> e.getValue().stream().forEach(d -> trainingString.append("\"" + d.getOriginalText() + "\"," + e.getKey() + "\n")));
            Experiment.save("TrainingData.csv", trainingString);

            StringBuilder allDocVectorString = new StringBuilder();
            for (double[] row : allDocVectors) {
                allDocVectorString.append(Arrays.stream(row).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + "\n");
            }
            Experiment.save("AllDocVectors.csv", allDocVectorString);
            Experiment.save("LabelArray.csv", Arrays.stream(labelArray).mapToObj(String::valueOf).collect(Collectors.joining("\n")));
            Experiment.save("ClassesMap.csv", classes.stream().collect(Collectors.joining("\n")));
            Experiment.save("Features.csv", featureSpace.stream().map(Feature::getFeature).collect(Collectors.joining("\n")));
        }
    }
}
