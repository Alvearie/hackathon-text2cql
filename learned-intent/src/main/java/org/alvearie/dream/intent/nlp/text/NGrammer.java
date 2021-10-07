/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

/**
 * A {@link NGrammer} is used to create the N-Grams for a {@link Document}.
 * <p>
 * Typically, n-gramming is performed at the corpus level, that is for various documents, since that is what will become
 * the feature space for the corpus. However, for convenience purpose this interface includes n-gramming on a single
 * document too.
 *
 */
public interface NGrammer {

    /**
     * Constant used to indicate a word was found that should prevent ngrams from bridging. This is inserted during
     * normalization, but used by ngramming to respect those boundaries.
     */
    public static final String BREAK = "CTMBREAK";

    /**
     * Generate the n-grams for the given document. If the document has a featureSpace already set, the ngrams used will be
     * based off of that featureSpace instead of recalculating it.
     * <p>
     * After running this method {@link Document#getNGrams()} can be used to retrieve the n-grams.
     *
     * @param document the document to n-gram in place
     */
    public void ngram(Document document);

    /**
     * Generate the n-grams for the given {@link Corpus}
     * <p>
     * After running this method {@link Corpus#getFeatureSpace()} can be used to retrieve the n-grams.
     *
     * @param corpus the corpus to n-gram in place
     */
    public void ngram(Corpus corpus);

}
