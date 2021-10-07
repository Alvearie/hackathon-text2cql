/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alvearie.dream.intent.nlp.text.Document;

/**
 * A {@link Classifier} is an NLP classifier for text documents.
 *
 */
public interface Classifier {

    /**
     * Trains this classifier using the given training data.
     * <p>
     * The training data is a map of categories with the corresponding documents that belong to that category.
     *
     * @param trainingData the training data
     */
    public void train(Map<String, Collection<Document>> trainingData);

    /**
     * Classify the given document using this classifier.
     *
     * @param document the document to classify
     * @return a sorted list of classifications, where the top (i=0) classification is the most likely one
     */
    public List<Classification> classify(Document document);

    /**
     * Serialize this classifier to the target file for future use.
     *
     * @param file persist this classifier to the given location for future use.
     * @throws IOException if an IO error occurs when saving the file
     */
    public void save(File file) throws IOException;

    /**
     * @return indicates whether the current classifier has already been trained.
     */
    public boolean isTrained();
}
