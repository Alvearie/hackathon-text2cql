/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors;

import java.util.Set;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentVectorizer;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.FeatureVector;
import org.alvearie.dream.intent.nlp.text.NGram;

/**
 * A BoW implementation that requires n-grammed {@link Document}s.
 * <p>
 * This BoW implementation requires that the given {@link Document}s be already n-grammed, such that the
 * {@link Document#getNGrams()} are set.
 *
 */
public class DocumentBoWVectorizer implements DocumentVectorizer {

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.DocumentVectorizer#vectorize(org.alvearie.nlp.text.Document)
     */
    @Override
    public void vectorize(Document document) {
        Set<NGram> documentNGrams = document.getNGrams();
        if (documentNGrams == null) {
            throw new IllegalArgumentException("The given Document does not have its n-grams. Make sure to NGram the Document before calling this vectorizer.");
        }
        FeatureVector vector = new FeatureVector(documentNGrams.size());
        for (Feature feature : documentNGrams) {
            NGram ngram = (NGram) feature;
            int count = document.getNGramCount(ngram);
            vector.addFeature(feature, new Double(count));
        }
        document.setVector("BoW", vector);
    }
}
