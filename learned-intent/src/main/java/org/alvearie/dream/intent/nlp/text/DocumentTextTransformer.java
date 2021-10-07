/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

/**
 * A {@link DocumentTextTransformer} processes a {@link Document} such that it is ready before it can be run through
 * specific text analytics.
 * <p>
 * The operations that are performed in each individual processor are up to the implementor and may include, making
 * lower case, expanding contractions, removing stop words, normalizing, etc.
 *
 */
public interface DocumentTextTransformer {

    /**
     * Transform the given document's text.
     * <p>
     * After running this method {@link Document#getText()} can be used to retrieve the tokens.
     *
     * @param document the document to normalize in place
     */
    public void processText(Document document);

}
