/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt;

import java.text.NumberFormat;
import java.util.Comparator;

/**
 * Represents the confusion matrix for an individual category.
 * 
 */
public class CategoryAccuracy {

    private String category;

    private int truePositives;
    private int falsePositives;
    private int falseNegatives;
    private int trueNegatives;
    private int trainingSize;

    /**
     * Crate a new {@link CategoryAccuracy} object for the given category
     *
     * @param category the category
     */
    public CategoryAccuracy(String category) {
        this.category = category;
    }

    /**
     * Register a TP.
     */
    public void addTruePositive() {
        truePositives++;
    }

    /**
     * Register a FP.
     */
    public void addFalsePositive() {
        falsePositives++;
    }

    /**
     * Register a FN.
     */
    public void addFalseNegative() {
        falseNegatives++;
    }

    /**
     * Register a TN.
     */
    public void addTrueNegative() {
        trueNegatives++;
    }

    /**
     * @return the truePositives
     */
    public int getTruePositives() {
        return truePositives;
    }

    /**
     * @return the falsePositives
     */
    public int getFalsePositives() {
        return falsePositives;
    }

    /**
     * @return the falseNegatives
     */
    public int getFalseNegatives() {
        return falseNegatives;
    }

    /**
     * @return the trueNegatives
     */
    public int getTrueNegatives() {
        return trueNegatives;
    }

    /**
     * @param size the number of entries that were used to train this category
     */
    public void setTrainingSize(int size) {
        trainingSize = size;
    }

    /**
     * @return the number of entries that were used to train this category
     */
    public int getTrainSize() {
        return trainingSize;
    }

    /**
     * @return the number of entries that were used to test this category
     */
    public int getTestSize() {
        return getTruePositives() + getFalseNegatives();
    }

    /**
     * @param value the value to represent as a percentage
     * @return the trueNegatives
     */
    public static String toPercentage(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMinimumFractionDigits(1);
        return defaultFormat.format(value);
    }

    /**
     * Calculates the Accuracy score for this category
     *
     * @return the accuracy
     */
    public double getAccuracy() {
        return (double) (truePositives + trueNegatives) / (truePositives + trueNegatives + falsePositives + falseNegatives);
    }

    /**
     * Calculates the Precision score for this category
     *
     * @return the precision
     */
    public double getPrecision() {
        return (double) truePositives / (truePositives + falsePositives);
    }

    /**
     * Calculates the Recall score for this category
     *
     * @return the recall
     */
    public double getRecall() {
        return (double) truePositives / (truePositives + falseNegatives);
    }

    /**
     * Calculates the F1 score for this category
     *
     * @return the F1
     */
    public double getF1() {
        double precision = getPrecision();
        double recall = getRecall();
        return (2 * precision * recall) / (precision + recall);
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Creates a {@link CategoryAccuracy} {@link Comparator} that sorts by F1 score descending, with NaNs last.
     *
     * @return the comparator
     */
    public static Comparator<CategoryAccuracy> getF1Comparator() {
        // We sort the results form highest F1 to lowest and NaN
        Comparator<CategoryAccuracy> comparator = Comparator.comparing(CategoryAccuracy::getF1, (f11, f12) -> {
            if (Double.isNaN(f11) && Double.isNaN(f12)) {
                return 0;
            }
            if (Double.isNaN(f11)) {
                return -1;
            }
            if (Double.isNaN(f12)) {
                return 1;
            }
            return f11.compareTo(f12);
        }).thenComparing(CategoryAccuracy::getTrainSize).reversed();
        return comparator;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("Category: %-45s", getCategory()));
        buffer.append(String.format("Train Size: %-4s", getTrainSize()));
        buffer.append(String.format("Test Size: %-4s", getTestSize()));
        buffer.append(String.format("Accuracy: %-8s", toPercentage(getAccuracy())));
        buffer.append(String.format("Precision: %-8s", toPercentage(getPrecision())));
        buffer.append(String.format("Recall: %-8s", toPercentage(getRecall())));
        buffer.append(String.format("F1: %s", toPercentage(getF1())));
        return buffer.toString();
    }
}
