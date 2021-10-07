/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text;

/**
 * A {@link DocumentVectorizer} is used to create a vector from a {@link Document}.
 * <p>
 * Some vectorization algorithms (like TF-IDF) require the whole corpus feature space, hence this interface requires
 * that a the {@link Document} implements the {@link Vectorizable} interface which provides access to the Corpus-level
 * feature space, and additionally because of that the interface offers a method that works on a {@link Corpus} as well.
 *
 /*
  * (C) Copyright IBM Corp. 2021, 2021
  *
  * SPDX-License-Identifier: Apache-2.0
  */ */
public interface DocumentVectorizer {

    /**
     * Generate the vector for the given {@link Document}.
     * <p>
     * After running this method {@link Document#getVector()} can be used to retrieve the vector.
     *
     * @param document the document to generate the vector in place for
     */
    public void vectorize(Document document);

    /**
     * Generate the vectors for the given {@link Corpus}.
     * <p>
     * After running this method {@link Document#getVector()} can be used to retrieve the vectors.
     *
     * @param corpus the corpus to generate the vectors in place for
     */
    public default void vectorize(Corpus corpus) {
        corpus.forEach(this::vectorize);
    }

}
