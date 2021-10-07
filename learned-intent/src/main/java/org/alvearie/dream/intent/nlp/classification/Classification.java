/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification;

/**
 * This class is used to save the result of a criteria classification.
 *
 */
public class Classification implements Comparable<Classification> {

    private Double probability;
    private String category;
    private String text;

    /**
     * Creates a new {@link Classification} object for the given text, with the given category and its corresponding
     * probability.
     *
     * @param category the category
     * @param probability the probability that this catergory matches the criterion
     * @param text the text that was classified
     */
    public Classification(String category, Double probability, String text) {
        this.probability = probability;
        this.category = category;
        this.text = text;
    }

    /**
     * @return the criterion
     */
    public String getText() {
        return text;
    }

    /**
     * @return the probability
     */
    public Double getProbability() {
        return probability;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Classification o) {
        int rc = o.getProbability().compareTo(getProbability());
        if (rc == 0) {
            rc = getCategory().compareTo(o.getCategory());
        }
        return rc;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (getProbability() != null) {
            return getCategory() + " (" + getProbability() + ")";
        }
        return getCategory();
    }
}
