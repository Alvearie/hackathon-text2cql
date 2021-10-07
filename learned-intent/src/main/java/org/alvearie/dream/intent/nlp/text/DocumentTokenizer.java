/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

/**
 * A {@link DocumentTokenizer} is used to split {@link Document}s into its comprising tokens.
 *
 */
public interface DocumentTokenizer {

    /**
     * Tokenize the given document.
     * <p>
     * After running this method {@link Document#getTokens()} can be used to retrieve the tokens.
     *
     * @param document the document to tokenize in place
     */
    public void tokenize(Document document);
}
