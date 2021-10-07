/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text;

/**
 * A {@link Document} decorator that also includes a prediction and some confidence.
 *
 */
public class DocumentWithPrediction extends Document {

    /**
     *
     */
    private static final long serialVersionUID = 338828054233678219L;

    private String prediction;
    private double confidence;

    /**
     * Creates a {@link Document} that includes a prediction.
     *
     * @param id the document ID
     * @param text the document text
     * @param prediction the document's prediction
     */
    public DocumentWithPrediction(String id, String text, String prediction) {
        this(id, text, prediction, Double.NaN);
    }

    /**
     * Creates a {@link Document} that includes a prediction.
     *
     * @param id the document ID
     * @param text the document text
     * @param prediction the document's prediction
     * @param confidence the prediction's confidence
     */
    public DocumentWithPrediction(String id, String text, String prediction, double confidence) {
        super(id, text);
        this.prediction = prediction;
        this.confidence = confidence;
    }

    /**
     * @return the prediction
     */
    public String getPrediction() {
        return prediction;
    }

    /**
     * @param prediction the predicition to set
     */
    public void setClassification(String prediction) {
        this.prediction = prediction;
    }

    /**
     * @return the confidence
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * @param confidence the confidence to set
     */
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.Document#toString()
     */
    @Override
    public String toString() {
        return String.format("%s%nPrediction: %s (%s)", super.toString(), getPrediction(), getConfidence());
    }

}
