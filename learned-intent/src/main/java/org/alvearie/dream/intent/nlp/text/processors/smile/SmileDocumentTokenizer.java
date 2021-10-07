/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors.smile;

import java.util.Arrays;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTokenizer;

import smile.nlp.tokenizer.SimpleTokenizer;
import smile.nlp.tokenizer.Tokenizer;

/**
 * A {@link DocumentTokenizer} implementation based on Smile.
 *
 */
public class SmileDocumentTokenizer implements DocumentTokenizer {

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.DocumentTokenizer#tokenize(org.alvearie.nlp.text.Document)
     */
    @Override
    public void tokenize(Document document) {
        // The true below is for splitting out word contractions
        Tokenizer tokenizer = new SimpleTokenizer(true);
        String[] tokens = tokenizer.split(document.getText());
        document.setTokens(Arrays.asList(tokens));
    }
}
