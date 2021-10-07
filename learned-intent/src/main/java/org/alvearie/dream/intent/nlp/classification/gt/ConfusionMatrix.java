/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Represents a confusion matrix for a some text analytics task, such as classification.
 * 
 */
public class ConfusionMatrix {

    private Map<String, CategoryAccuracy> matrix;

    /**
     * Initialize a confusion matrix for the given categories
     *
     * @param categories the categories of the matrix
     */
    public ConfusionMatrix(Set<String> categories) {
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("At least one category is required to create a confusion matrix.");
        }
        matrix = new TreeMap<>();
        for (String category : categories) {
            matrix.put(category, new CategoryAccuracy(category));
        }
    }

    /**
     * Initialize a confusion matrix for the given categories
     *
     * @param categories the categories of the matrix
     */
    public ConfusionMatrix(String... categories) {
        this(new HashSet<String>(Arrays.asList(categories)));
    }

    /**
     * Register a classification's prediction with the expected and actual categories.
     *
     * @param expectedCategory the expected category
     * @param actualCategory the actual category
     * @param text optionally the text that was classified
     */
    public void register(String expectedCategory, String actualCategory, String text) {
        CategoryAccuracy expectedCategoryScores = matrix.get(expectedCategory);
        matrix.put(expectedCategory, expectedCategoryScores);
        CategoryAccuracy actualCategoryScores = matrix.get(actualCategory);
        matrix.put(actualCategory, actualCategoryScores);
        if (expectedCategory.equals(actualCategory)) {
            // If expected and actual match, we add a TP to that category and a TN for all other categories
            expectedCategoryScores.addTruePositive();
            Set<String> allOtherCategories = getCategories();
            for (String otherCategory : allOtherCategories) {
                if (otherCategory.equals(actualCategory)) {
                    continue;
                }
                CategoryAccuracy scores = matrix.get(otherCategory);
                matrix.put(otherCategory, scores);
                scores.addTrueNegative();
            }
        } else {
            // If expected and actual don't match
            // - We add a FN to the expected category since we missed it
            // - We add a FP to the actual category since we prediceted it incorrectly
            // - We add a TN to all other categories, except the expected and actual
//            System.out.printf("Expected '%s' but was '%s': %s%n", expectedCategory, actualCategory, text);

            expectedCategoryScores.addFalseNegative();
            actualCategoryScores.addFalsePositive();
            Set<String> allOtherCategories = getCategories();
            for (String otherCategory : allOtherCategories) {
                if (otherCategory.equals(actualCategory) || otherCategory.equals(expectedCategory)) {
                    continue;
                }
                CategoryAccuracy scores = matrix.get(otherCategory);
                matrix.put(otherCategory, scores);
                scores.addTrueNegative();
            }
        }
    }

    /**
     * Register a classification's prediction with the expected and actual categories, with the given number of hits
     * <p>
     * Likely this method will only used for testing purposes.
     *
     * @param expectedCategory the expected category
     * @param actualCategory the actual category
     * @param count the number of hits with this result
     */
    public void register(String expectedCategory, String actualCategory, int count) {
        for (int i = 0; i < count; i++) {
            register(expectedCategory, actualCategory, null);
        }
    }

    /**
     * Gets the categories known to this confusion matrix
     *
     * @return the categories in a unmodifiable set
     */
    public Set<String> getCategories() {
        return Collections.unmodifiableSet(matrix.keySet());
    }

    /**
     * @return the matrix
     */
    public Map<String, CategoryAccuracy> getMatrix() {
        return matrix;
    }

    /**
     * @return precision across all categories
     */
    public double getOverallPrecision() {
        double truePositives = 0.0;
        double falsePositives = 0.0;
        for (CategoryAccuracy categoryAccuracy : matrix.values()) {
            truePositives += categoryAccuracy.getTruePositives();
            falsePositives += categoryAccuracy.getFalsePositives();
        }
        return truePositives / (truePositives + falsePositives);
    }

    /**
     * @return the recall across all categories
     */
    public double getOverallRecall() {
        double truePositives = 0.0;
        double falseNegatives = 0.0;
        for (CategoryAccuracy categoryAccuracy : matrix.values()) {
            truePositives += categoryAccuracy.getTruePositives();
            falseNegatives += categoryAccuracy.getFalseNegatives();
        }
        return truePositives / (truePositives + falseNegatives);
    }

    /**
     * @return the F1 score across all categories
     */
    public double getOverallF1() {
        double precision = getOverallPrecision();
        double recall = getOverallRecall();
        return (2 * precision * recall) / (precision + recall);
    }
}
