/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors.smile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTokenizer;
import org.junit.Test;


public class SmileDocumentTokenizerTest {

    private DocumentTokenizer tokenizer;

    /**
     *
     */
    public SmileDocumentTokenizerTest() {
        tokenizer = new SmileDocumentTokenizer();
    }

    /**
     *
     */
    @Test
    public void tokenize() {
        Document document = new Document("The red beautiful dog ran over the hill.");
        tokenizer.tokenize(document);
        System.out.println(document);
        List<String> tokens = Arrays.asList("The", "red", "beautiful", "dog", "ran", "over", "the", "hill", ".");
        assertEquals(tokens, document.getTokens());
    }

    /**
     *
     */
    @Test
    public void tokenizeWithContractions() {
        Document document = new Document("The red dog won't or can't run over the hill.");
        tokenizer.tokenize(document);
        System.out.println(document);
        List<String> tokens = Arrays.asList("The", "red", "dog", "will", "not", "or", "can", "not", "run", "over", "the", "hill", ".");
        assertEquals(tokens, document.getTokens());
    }

}
