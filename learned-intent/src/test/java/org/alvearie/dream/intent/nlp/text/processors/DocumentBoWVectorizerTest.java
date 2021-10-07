/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors;

import static org.junit.Assert.assertEquals;

import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTextTransformer;
import org.alvearie.dream.intent.nlp.text.DocumentTokenizer;
import org.alvearie.dream.intent.nlp.text.DocumentVectorizer;
import org.alvearie.dream.intent.nlp.text.FeatureVector;
import org.alvearie.dream.intent.nlp.text.NGram;
import org.alvearie.dream.intent.nlp.text.NGrammer;
import org.alvearie.dream.intent.nlp.text.Words;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentNGrammer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentNormalizer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentTokenizer;
import org.junit.Test;


public class DocumentBoWVectorizerTest {

    private DocumentVectorizer vectorizer;
    private NGrammer ngrammer;
    private DocumentTextTransformer normalizer;
    private DocumentTokenizer tokenizer;

    /**
     *
     */
    public DocumentBoWVectorizerTest() {
        vectorizer = new DocumentBoWVectorizer();
        ngrammer = new SmileDocumentNGrammer(1, 2);
        tokenizer = new SmileDocumentTokenizer();
        normalizer = new SmileDocumentNormalizer(new Words("year"), new Words(), new Words(), false, 1, true, false, false, false);
    }

    /**
     *
     */
    @Test
    public void vectorizeSingleDocument() {
        Document document = new Document("the red dog ran over the hill, the red dog then ran under a tree.");
        normalizer.processText(document);
        tokenizer.tokenize(document);
        ngrammer.ngram(document);
        vectorizer.vectorize(document);
        System.out.println(document.toVerboseString());
        FeatureVector bow = document.getVector();
        // The final text after processing is the following, given that we will then test the frequencies
        assertEquals("red dog ran hill red dog ran tree", document.getText());
        assertEquals(2.0, bow.getValue(NGram.getNGram("dog")), 0.00001);
        assertEquals(2.0, bow.getValue(NGram.getNGram("ran")), 0.00001);
        assertEquals(1.0, bow.getValue(NGram.getNGram("hill")), 0.00001);
        assertEquals(1.0, bow.getValue(NGram.getNGram("tree")), 0.00001);
        assertEquals(2.0, bow.getValue(NGram.getNGram("red", "dog")), 0.00001);
        assertEquals(2.0, bow.getValue(NGram.getNGram("dog", "ran")), 0.00001);
    }

    /**
     *
     */
    @Test
    public void vectorizeSingleDocumentWithBreakWords() {
        Document document = new Document("the red dog ran over the hill, the red dog then ran under a tree.");
        SmileDocumentNormalizer normalizerWithBreakWords = new SmileDocumentNormalizer();
        normalizerWithBreakWords.processText(document);
        tokenizer.tokenize(document);
        ngrammer.ngram(document);
        vectorizer.vectorize(document);
        System.out.println(document.toVerboseString());
        FeatureVector bow = document.getVector();
        // The final text after processing is the following, given that we will then test the frequencies
        assertEquals("red dog ran " + NGrammer.BREAK + " hill " + NGrammer.BREAK + " red dog " + NGrammer.BREAK + " ran " + NGrammer.BREAK + " tree", document.getText());
        assertEquals(2.0, bow.getValue(NGram.getNGram("dog")), 0.00001);
        assertEquals(2.0, bow.getValue(NGram.getNGram("ran")), 0.00001);
        assertEquals(1.0, bow.getValue(NGram.getNGram("hill")), 0.00001);
        assertEquals(1.0, bow.getValue(NGram.getNGram("tree")), 0.00001);
        assertEquals(2.0, bow.getValue(NGram.getNGram("red", "dog")), 0.00001);
        assertEquals(1.0, bow.getValue(NGram.getNGram("dog", "ran")), 0.00001);
    }

    /**
     *
     */
    @Test
    public void vectorizeMultipleDocuments() {
        Document document1 = new Document("the red dog ran over the hill, the red dog then ran under a tree.");
        Document document2 = new Document("a red cat jumped");
        Corpus corpus = new Corpus(document1, document2);
        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        ngrammer.ngram(corpus);
        corpus.forEach(vectorizer::vectorize);
        corpus.forEach(document -> System.out.println(document.toVerboseString()));

        FeatureVector bow1 = document1.getVector();
        // Features from 1st doc
        assertEquals(2.0, bow1.getValue(NGram.getNGram("red")), 0.00001);
        assertEquals(2.0, bow1.getValue(NGram.getNGram("dog")), 0.00001);
        assertEquals(2.0, bow1.getValue(NGram.getNGram("ran")), 0.00001);
        assertEquals(1.0, bow1.getValue(NGram.getNGram("hill")), 0.00001);
        assertEquals(1.0, bow1.getValue(NGram.getNGram("tree")), 0.00001);
        assertEquals(2.0, bow1.getValue(NGram.getNGram("red", "dog")), 0.00001);
        assertEquals(2.0, bow1.getValue(NGram.getNGram("dog", "ran")), 0.00001);
        // Features from 2nd doc
        assertEquals(0.0, bow1.getValue(NGram.getNGram("cat")), 0.00001);
        assertEquals(0.0, bow1.getValue(NGram.getNGram("jump")), 0.00001);
        assertEquals(0.0, bow1.getValue(NGram.getNGram("cat", "jump")), 0.00001);

        FeatureVector bow2 = document2.getVector();
        // Features from 1st doc
        assertEquals(0.0, bow2.getValue(NGram.getNGram("dog")), 0.00001);
        assertEquals(0.0, bow2.getValue(NGram.getNGram("ran")), 0.00001);
        assertEquals(0.0, bow2.getValue(NGram.getNGram("hill")), 0.00001);
        assertEquals(0.0, bow2.getValue(NGram.getNGram("tree")), 0.00001);
        assertEquals(0.0, bow2.getValue(NGram.getNGram("red", "dog")), 0.00001);
        assertEquals(0.0, bow2.getValue(NGram.getNGram("dog", "ran")), 0.00001);
        // Features from 2nd doc
        assertEquals(1.0, bow2.getValue(NGram.getNGram("red")), 0.00001);
        assertEquals(1.0, bow2.getValue(NGram.getNGram("cat")), 0.00001);
        assertEquals(1.0, bow2.getValue(NGram.getNGram("red", "cat")), 0.00001);
        assertEquals(1.0, bow2.getValue(NGram.getNGram("jump")), 0.00001);
        assertEquals(1.0, bow2.getValue(NGram.getNGram("cat", "jump")), 0.00001);
    }

    /**
     * In this test we check that if we have a token in docN that is a substring of another document's token, we won't count
     * that as a feature in that other document.
     */
    @Test
    public void vectorizeCountWholeWordsOnly() {
        Document document1 = new Document("Cooperate sentence with cooperate sentence cooperate");
        // This document has a token that is a substring of another document
        Document document2 = new Document("Coo");
        Corpus corpus = new Corpus(document1, document2);
        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        ngrammer.ngram(corpus);
        corpus.forEach(vectorizer::vectorize);
        corpus.forEach(document -> System.out.println(document.toVerboseString()));

        // Features from 1st doc
        FeatureVector bow1 = document1.getVector();
        assertEquals(3.0, bow1.getValue(NGram.getNGram("cooper")), 0.00001);
        assertEquals(0.0, bow1.getValue(NGram.getNGram("coo")), 0.00001);
        assertEquals(2.0, bow1.getValue(NGram.getNGram("sentenc")), 0.00001);
        assertEquals(2.0, bow1.getValue(NGram.getNGram("sentenc", "cooper")), 0.00001);
        assertEquals(2.0, bow1.getValue(NGram.getNGram("cooper", "sentenc")), 0.00001);

        // Features from 2nd doc
        FeatureVector bow2 = document2.getVector();
        assertEquals(0.0, bow2.getValue(NGram.getNGram("cooper")), 0.00001);
        assertEquals(1.0, bow2.getValue(NGram.getNGram("coo")), 0.00001);
        assertEquals(0.0, bow2.getValue(NGram.getNGram("sentenc")), 0.00001);
        assertEquals(0.0, bow2.getValue(NGram.getNGram("sentenc", "cooper")), 0.00001);
        assertEquals(0.0, bow2.getValue(NGram.getNGram("cooper", "sentenc")), 0.00001);
    }
}
