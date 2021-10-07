/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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


public class TFIDFVectorizerTest {

    /**
     *
     */
    private static final double MARGIN_OF_ERROR = 0.0001;

    private DocumentVectorizer vectorizer;
    private NGrammer ngrammer;
    private DocumentTextTransformer normalizer;
    private DocumentTokenizer tokenizer;
    private DocumentVectorizer bow;

    /**
     *
     */
    public TFIDFVectorizerTest() {
        vectorizer = new TFIDFVectorizer();
        ngrammer = new SmileDocumentNGrammer(1, 2);
        tokenizer = new SmileDocumentTokenizer();
        normalizer = new SmileDocumentNormalizer(new Words("year"), new Words(), new Words(), true, 2, true, false, false, false);
        bow = new DocumentBoWVectorizer();
    }

    /**
     *
     */
    @Test
    public void vectorizeMultipleDocuments() {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("Ages 18 to 100 year");
        Document document3 = new Document("Absolute neutrophil count >= 1500/µL");
        Document document4 = new Document("Absolute neutrophil count >= 1600/µL");
        Corpus corpus = new Corpus(document1, document2, document3, document4);

        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        ngrammer.ngram(corpus);
        corpus.forEach(bow::vectorize);
        corpus.forEach(vectorizer::vectorize);

        corpus.forEach(d -> System.out.println(d.toVerboseString()));

        assertEquals("Incorrect feature space: " + corpus.getFeatureSpace(), 6, corpus.getFeatureSpace().size());

        // This is the matrix that we are expecting which we originally got from the python implementation
        // neutrophil          0.000000  0.000000  0.447214  0.447214
        // absolut             0.000000  0.000000  0.447214  0.447214
        // count               0.000000  0.000000  0.447214  0.447214
        // ag                  1.000000  1.000000  0.000000  0.000000
        // absolut neutrophil  0.000000  0.000000  0.447214  0.447214
        // neutrophil count    0.000000  0.000000  0.447214  0.447214
        corpus.printMatrix();

        // Features from 1st doc: Age: 18 to 100 years
        FeatureVector tfidf1 = document1.getVector();
        assertEquals(0.0, tfidf1.getValue(NGram.getNGram("neutrophil")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf1.getValue(NGram.getNGram("absolut")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf1.getValue(NGram.getNGram("count")), MARGIN_OF_ERROR);
        assertEquals(1.0, tfidf1.getValue(NGram.getNGram("ag")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf1.getValue(NGram.getNGram("absolut", "neutrophil")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf1.getValue(NGram.getNGram("neutrophil", "count")), MARGIN_OF_ERROR);

        // Features from 2nd doc: Ages 18 to 100 year
        FeatureVector tfidf2 = document2.getVector();
        assertEquals(0.0, tfidf2.getValue(NGram.getNGram("neutrophil")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf2.getValue(NGram.getNGram("absolut")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf2.getValue(NGram.getNGram("count")), MARGIN_OF_ERROR);
        assertEquals(1.0, tfidf2.getValue(NGram.getNGram("ag")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf2.getValue(NGram.getNGram("absolut", "neutrophil")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf2.getValue(NGram.getNGram("neutrophil", "count")), MARGIN_OF_ERROR);

        // Features from 3rd doc: Absolute neutrophil count >= 1500/µL
        FeatureVector tfidf3 = document3.getVector();
        assertEquals(0.4472, tfidf3.getValue(NGram.getNGram("neutrophil")), MARGIN_OF_ERROR);
        assertEquals(0.4472, tfidf3.getValue(NGram.getNGram("absolut")), MARGIN_OF_ERROR);
        assertEquals(0.4472, tfidf3.getValue(NGram.getNGram("count")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf3.getValue(NGram.getNGram("ag")), MARGIN_OF_ERROR);
        assertEquals(0.4472, tfidf3.getValue(NGram.getNGram("absolut", "neutrophil")), MARGIN_OF_ERROR);
        assertEquals(0.4472, tfidf3.getValue(NGram.getNGram("neutrophil", "count")), MARGIN_OF_ERROR);

        // Features from 4th doc: Absolute neutrophil count >= 1600/µL
        FeatureVector tfidf4 = document4.getVector();
        assertEquals(0.4472, tfidf4.getValue(NGram.getNGram("neutrophil")), MARGIN_OF_ERROR);
        assertEquals(0.4472, tfidf4.getValue(NGram.getNGram("absolut")), MARGIN_OF_ERROR);
        assertEquals(0.4472, tfidf4.getValue(NGram.getNGram("count")), MARGIN_OF_ERROR);
        assertEquals(0.0, tfidf4.getValue(NGram.getNGram("ag")), MARGIN_OF_ERROR);
        assertEquals(0.4472, tfidf4.getValue(NGram.getNGram("absolut", "neutrophil")), MARGIN_OF_ERROR);
        assertEquals(0.4472, tfidf4.getValue(NGram.getNGram("neutrophil", "count")), MARGIN_OF_ERROR);
    }

    /**
     * The purpose of this test is to ensure we get sensible results for documents that end up without features
     */
    @Test
    public void vectorizeBadDocument() {
        Document document = new Document("a 1000.");
        Corpus corpus = new Corpus(document);
        normalizer.processText(document);
        tokenizer.tokenize(document);
        ngrammer.ngram(corpus);
        bow.vectorize(document);
        vectorizer.vectorize(corpus);
        System.out.println(document);
        FeatureVector vector = document.getVector();
        // The final text after processing is the empty string, given that we will then test it results in the empty vector
        assertEquals("", document.getText());
        assertTrue(vector.isEmpty());
    }

    /**
     * When there is a bad document, that is a document for which after normalization etc. we will end up with no features,
     * that does not affect the vectorization and l2 normalization and it is just handled as a zero vector.
     */
    @Test
    public void vectorizeCorpusWithBadDocument() {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("a 1000.");
        Corpus corpus = new Corpus(document1, document2);

        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        ngrammer.ngram(corpus);
        corpus.forEach(bow::vectorize);
        corpus.forEach(vectorizer::vectorize);
        corpus.forEach(System.out::println);

        FeatureVector vector1 = document1.getVector();
        assertFalse(vector1.isZeroVector());
        FeatureVector vector2 = document2.getVector();
        assertTrue(vector2.isZeroVector());
    }

}
