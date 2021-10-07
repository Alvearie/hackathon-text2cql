/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.alvearie.dream.intent.nlp.Experiment;
import org.alvearie.dream.intent.nlp.classification.Classification;
import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.classification.gt.io.CSVGroundTruthReader;
import org.alvearie.dream.intent.nlp.classification.gt.io.GroundTruthReader;
import org.alvearie.dream.intent.nlp.text.Document;

/**
 * The {@link GroundTruther} is a class used to test the accuracy of a {@link Classifier}.
 * <p>
 * It uses an existing GT file, and randomly selects a certain percentage of it as trainig vs. test data, it then trains
 * a classifier and calculates accuracy scores.
 *
 */
public class GroundTruther {

    /**
     * Default random seed to use for splitting the training data.
     */
    private static final int DEFAULT_SEED = 23;

    /**
     * We use the same seed in order to get consistent "random" results for the same GT set across multiple runs.
     */
    private int seed = DEFAULT_SEED;

    private static final int TEST_DATA_PERCENTAGE = 10;
    private Classifier classifier;
    private ConfusionMatrix confusionMatrix;
    private InputStream groundTruthInputStream;
    private double minThreshold = 0.0;

    /**
     * Creates a new {@link GroundTruther} over the following ground truth CSV and classifier.
     *
     * @param groundTruthFile the ground truth CSV file. Will be split into test/train data according to
     *        SEED/TEST_DATA_PERCENTAGE.
     * @param classifier the classifier to test with the GT
     * @throws FileNotFoundException
     */
    public GroundTruther(File groundTruthFile, Classifier classifier) throws FileNotFoundException {
        this(new FileInputStream(groundTruthFile), classifier);
    }

    /**
     * Creates a new {@link GroundTruther} over the following ground truth CSV and classifier.
     *
     * @param groundTruthInputStream an input stream of the ground truth CSV file. Will be split into test/train data
     *        according to SEED/TEST_DATA_PERCENTAGE.
     * @param classifier the classifier to test with the GT
     */
    public GroundTruther(InputStream groundTruthInputStream, Classifier classifier) {
        this(groundTruthInputStream, classifier, 0.0);
    }

    /**
     * Creates a new {@link GroundTruther} over the following ground truth CSV and classifier.
     *
     * @param groundTruthInputStream an input stream of the ground truth CSV file. Will be split into test/train data
     *        according to SEED/TEST_DATA_PERCENTAGE.
     * @param classifier the classifier to test with the GT
     * @param minThreshold the minimum classification score to trust when evaluating test set accuracy
     */
    public GroundTruther(InputStream groundTruthInputStream, Classifier classifier, double minThreshold) {
        this.groundTruthInputStream = groundTruthInputStream;
        this.classifier = classifier;
        this.minThreshold = minThreshold;
    }

    /**
     * Calculates the GT accuracy with the given GT. The training data will be split according to the TEST_DATA_PERCENTAGE
     * proportion and current SEED value.
     *
     * @throws IOException - if the given file does not exist or if an I/O error occurs reading from the file or a malformed
     *         or unmappable byte sequence is read
     */
    public void run() throws IOException {
        GroundTruthReader reader = new CSVGroundTruthReader(groundTruthInputStream);
        Map<String, Collection<Document>> trainData = reader.read();
        if (classifier.isTrained()) {
            save("TestData.csv", trainData);
            confusionMatrix = new ConfusionMatrix(trainData.keySet());
            testClassifier(trainData);
        } else {
            Map<String, Collection<Document>> testData = splitTrainingAndTest(trainData);
            save("TrainingData.csv", trainData);
            save("TestData.csv", testData);

            confusionMatrix = new ConfusionMatrix(trainData.keySet());
            classifier.train(trainData);
            testClassifier(testData);
        }
        reportResults(trainData);
    }

    /**
     * Serialize a map of intent to document collections
     *
     * @param fileName
     * @param data
     */
    private static void save(String fileName, Map<String, Collection<Document>> data) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Collection<Document>> entry : data.entrySet()) {
            String intent = entry.getKey();
            sb.append(entry.getValue().stream().map(d -> "\"" + d.getOriginalText() + "\"," + intent).collect(Collectors.joining("\n")));
        }
        Experiment.save(fileName, sb.toString());
    }

    /**
     * Generates a report to the console with the accuracy results
     */
    private void reportResults(Map<String, Collection<Document>> trainData) {
        StringBuilder report = new StringBuilder();
        report.append("\nAccuracy report for classifier implementation: " + classifier.getClass() + "\n");
        Map<String, CategoryAccuracy> scores = confusionMatrix.getMatrix();
        Set<Entry<String, CategoryAccuracy>> scoresEntrySet = scores.entrySet();
        // We collect the CategoryAccuracy objects for each category and set the training size just for reporting purposes
        List<CategoryAccuracy> results = new ArrayList<>();
        for (Entry<String, CategoryAccuracy> scoresEntry : scoresEntrySet) {
            String category = scoresEntry.getKey();
            CategoryAccuracy categoryAccuracy = scoresEntry.getValue();
            if (trainData != null) {
                categoryAccuracy.setTrainingSize(trainData.get(category).size());
            }
            results.add(categoryAccuracy);
        }
        // Finally we sort and print the results
        results.stream()
                .sorted(CategoryAccuracy.getF1Comparator())
                .collect(Collectors.toList())
                .stream()
                .forEach(r -> report.append(r + "\n"));

        report.append("\nOverall F1: " + CategoryAccuracy.toPercentage(confusionMatrix.getOverallF1()) + "\n");
        System.out.println(report.toString());
        if (Experiment.getCurrentExperiment() != null) {
            Experiment.getCurrentExperiment().log("\n\n" + report.toString() + "\n\n");
        }
    }

    /**
     * Runs the classifier on the test data, and generates the confusion matrix
     *
     * @param testData the test data
     */
    private void testClassifier(Map<String, Collection<Document>> testData) {
        StringBuilder output = new StringBuilder();
        output.append("\nRunning test criteria through the classifier...\n");
        Set<Entry<String, Collection<Document>>> testDataEntrySet = testData.entrySet();
        int skipped = 0;
        for (Entry<String, Collection<Document>> testDataEntry : testDataEntrySet) {
            String expectedCategory = testDataEntry.getKey();
            Collection<Document> testDocuments = testDataEntry.getValue();
            for (Document document : testDocuments) {
                List<Classification> results = classifier.classify(document);
                Classification topResult = results.get(0);
                String actualCategory = topResult.getCategory();
                if (topResult.getProbability() <= minThreshold) {
                    skipped++;
                    continue;
                }
                if (!actualCategory.equals(expectedCategory)) {
                    Double expectedProbability = 0.0;
                    Double actualProbability = 0.0;
                    for (Classification result : results) {
                        if (result.getCategory().equals(expectedCategory)) {
                            expectedProbability = result.getProbability();
                        }
                        if (result.getCategory().equals(actualCategory)) {
                            actualProbability = result.getProbability();
                        }
                    }
                    output.append(String.format("Expected '%s'", expectedCategory));
                    if (expectedProbability != null) {
                        output.append(String.format("(%3.2f)", expectedProbability));
                    }
                    output.append(String.format(" but was '%s'", actualCategory));
                    if (actualProbability != null) {
                        output.append(String.format("(%3.2f)", actualProbability));
                    }
                    output.append(String.format(": %s%n", document.getOriginalText()));
                }

                /*
                 * TODO Replace:
                 *      confusionMatrix.register(expectedCategory, actualCategory, text)
                 *  with
                 *      confusionMatrix.register(expectedPrediction, actualPrediction, text)
                 *      Then create an interface Prediction with methods getLabel() and getConfidence(). Make Classification implement Prediction map category/probability,
                 *      and in those Predictions where there is a confidence use it and if not just use the label like now.
                 */
                confusionMatrix.register(expectedCategory, actualCategory, document.getText());
            }
        }
        output.append("Finished testing the classifier (minThreshold=" + minThreshold + ", skipped=" + skipped + ").\n");
        System.out.println(output.toString());
        if (Experiment.getCurrentExperiment() != null) {
            Experiment.getCurrentExperiment().log("\n\n" + output.toString() + "\n\n");
        }
    }

    /**
     * Splits the given ground truth into a training set and a test set.
     * <p>
     * The test data will be removed from the given ground truth which will be the training data, and the method will return
     * the test data, obtained from the original ground truth.
     * <p>
     * {@value #TEST_DATA_PERCENTAGE} percent of the given ground truth data will be turned into test data.
     *
     * @param groundTruth the ground truth to split, from which the test data will be removed in-place
     * @returns the test data
     */
    private Map<String, Collection<Document>> splitTrainingAndTest(Map<String, Collection<Document>> groundTruth) {
        Map<String, Collection<Document>> testData = new HashMap<>();
        Set<Entry<String, Collection<Document>>> groundTruthEntrySet = groundTruth.entrySet();
        for (Entry<String, Collection<Document>> groundTruthEntry : groundTruthEntrySet) {
            Collection<Document> trainingCriteria = groundTruthEntry.getValue();
            List<Document> allCriteria = new ArrayList<>(trainingCriteria);
            if (allCriteria.size() > 1) {
                int numberOfTestCriteria = (int) (allCriteria.size() * (TEST_DATA_PERCENTAGE / 100.0));
                if (numberOfTestCriteria == 0) {
                    // If there is too little data such that we 10% of the data isn't even 1, we grab at least 1
                    numberOfTestCriteria = 1;
                }
                Collections.shuffle(allCriteria, new Random(seed));
                List<Document> testCriteria = allCriteria.subList(0, numberOfTestCriteria);
                trainingCriteria.removeAll(testCriteria);
                testData.put(groundTruthEntry.getKey(), testCriteria);
            }
        }
        return testData;
    }

    /**
     * @return the confusionMatrix
     */
    public ConfusionMatrix getConfusionMatrix() {
        return confusionMatrix;
    }

    /**
     * Change the random seed used for this GroundTruther
     *
     * @param seed
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }
}
