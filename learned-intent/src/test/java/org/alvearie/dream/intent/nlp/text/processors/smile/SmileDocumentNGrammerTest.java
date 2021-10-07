/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors.smile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTextTransformer;
import org.alvearie.dream.intent.nlp.text.DocumentTokenizer;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.FeatureSpace;
import org.alvearie.dream.intent.nlp.text.NGram;
import org.alvearie.dream.intent.nlp.text.NGrammer;
import org.alvearie.dream.intent.nlp.text.Words;
import org.junit.Test;


public class SmileDocumentNGrammerTest {

    private NGrammer ngrammer;
    private DocumentTokenizer tokenizer;
    private DocumentTextTransformer normalizer;

    /**
     *
     */
    public SmileDocumentNGrammerTest() {
        normalizer = new SmileDocumentNormalizer();
        tokenizer = new SmileDocumentTokenizer();
        ngrammer = new SmileDocumentNGrammer(1, 2);
    }

    /**
     *
     */
    @Test
    public void ngramSingleDocument() {
        Document document = new Document("The big red dog ran over the hill.");
        tokenizer.tokenize(document);
        ngrammer.ngram(document);
        System.out.println(document);
        Set<NGram> ngrams = new HashSet<>();
        ngrams.add(NGram.getNGram("The"));
        ngrams.add(NGram.getNGram("big"));
        ngrams.add(NGram.getNGram("red"));
        ngrams.add(NGram.getNGram("dog"));
        ngrams.add(NGram.getNGram("ran"));
        ngrams.add(NGram.getNGram("hill"));
        ngrams.add(NGram.getNGram("over"));
        ngrams.add(NGram.getNGram("the"));
        ngrams.add(NGram.getNGram("The", "big"));
        ngrams.add(NGram.getNGram("big", "red"));
        ngrams.add(NGram.getNGram("red", "dog"));
        ngrams.add(NGram.getNGram("dog", "ran"));
        ngrams.add(NGram.getNGram("the", "hill"));
        ngrams.add(NGram.getNGram("over", "the"));
        ngrams.add(NGram.getNGram("ran", "over"));
        System.out.println(document.toVerboseString());
        Set<NGram> actualNGrams = document.getNGrams();
        assertEquals(ngrams, actualNGrams);

        // The smile n-grammer will also set the n-gram count for the Document
        // Each n-gram in the complete n-gram set will occur exactly once since these are only the n-grams from the document
        for (NGram nGram : actualNGrams) {
            assertEquals(1, document.getNGramCount(nGram));
        }
    }

    /**
     * Test that when a feature space is set for the document prior to running ngram, the feature space is respected for the
     * resulting ngrams.
     */
    @Test
    public void ngramSingleDocumentAgainstExistingFeatureSpace() {
        Document document = new Document("The big red dog ran over the hill.");
        // Generate ngrams, omitting the word "the" (both cases), and verify the document results in only ngrams from this
        // subsetted feature space
        FeatureSpace ngrams = new FeatureSpace();
        ngrams.add(NGram.getNGram("big"));
        ngrams.add(NGram.getNGram("red"));
        ngrams.add(NGram.getNGram("dog"));
        ngrams.add(NGram.getNGram("ran"));
        ngrams.add(NGram.getNGram("over"));
        ngrams.add(NGram.getNGram("hill"));
        ngrams.add(NGram.getNGram("big", "red"));
        ngrams.add(NGram.getNGram("red", "dog"));
        ngrams.add(NGram.getNGram("dog", "ran"));
        ngrams.add(NGram.getNGram("ran", "over"));
        document.setFeatureSpace(ngrams);
        tokenizer.tokenize(document);
        ngrammer.ngram(document);
        Set<NGram> actualNGrams = document.getNGrams();

        assertEquals(new HashSet<>(ngrams), actualNGrams);

        // The smile n-grammer will also set the n-gram count for the Document
        // Each n-gram in the complete n-gram set will occur exactly once since these are only the n-grams from the document
        for (NGram nGram : actualNGrams) {
            assertEquals(1, document.getNGramCount(nGram));
        }
    }

    /**
     *
     */
    @Test
    public void ngramMultipleDocuments() {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("Ages 18 to 100 year");
        Document document3 = new Document("Absolute neutrophil count >= 1500/µL");
        Document document4 = new Document("Absolute neutrophil count >= 1600/µL");
        Corpus corpus = new Corpus(document1, document2, document3, document4);
        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        ngrammer.ngram(corpus);
        corpus.forEach(d -> System.out.println(d.toVerboseString()));

        // The N-Grams will be created from the entire corpus, not just from a single document
        Set<NGram> ngrams = new HashSet<>();
        ngrams.add(NGram.getNGram("neutrophil"));
        ngrams.add(NGram.getNGram("absolut"));
        ngrams.add(NGram.getNGram("count"));
        ngrams.add(NGram.getNGram("year"));
        ngrams.add(NGram.getNGram("ag"));
        ngrams.add(NGram.getNGram("l"));
        ngrams.add(NGram.getNGram("absolut", "neutrophil"));
        ngrams.add(NGram.getNGram("neutrophil", "count"));

        // Both the Corpus and the Documents will have the n-gram feature space
        assertEquals(ngrams, new HashSet<Feature>(corpus.getFeatureSpace()));
        assertEquals(ngrams, new HashSet<Feature>(document1.getFeatureSpace()));
        assertEquals(ngrams, new HashSet<Feature>(document2.getFeatureSpace()));
        assertEquals(ngrams, new HashSet<Feature>(document3.getFeatureSpace()));
        assertEquals(ngrams, new HashSet<Feature>(document4.getFeatureSpace()));

        // But each individual document will have its list of specific n-grams it contributed
        System.out.println(document1.toVerboseString());
        ngrams = new HashSet<>();
        ngrams.add(NGram.getNGram("ag"));
        ngrams.add(NGram.getNGram("year"));
        assertEquals(ngrams, new HashSet<Feature>(document1.getNGrams()));
        assertEquals(ngrams, new HashSet<Feature>(document2.getNGrams()));

        // This n-grammer will also set the n-gram count for the Document
        // Each n-gram in the complete n-gram set will occur exactly once since these are only the n-grams from the document
        assertEquals(1, document1.getNGramCount(NGram.getNGram("ag")));
        assertEquals(0, document1.getNGramCount(NGram.getNGram("count")));
        assertEquals(1, document2.getNGramCount(NGram.getNGram("ag")));
        assertEquals(0, document2.getNGramCount(NGram.getNGram("count")));

        System.out.println(document3.toVerboseString());
        ngrams = new HashSet<>();
        ngrams.add(NGram.getNGram("neutrophil"));
        ngrams.add(NGram.getNGram("absolut"));
        ngrams.add(NGram.getNGram("count"));
        ngrams.add(NGram.getNGram("l"));
        ngrams.add(NGram.getNGram("absolut", "neutrophil"));
        ngrams.add(NGram.getNGram("neutrophil", "count"));
        assertEquals(ngrams, new HashSet<Feature>(document3.getNGrams()));
        assertEquals(ngrams, new HashSet<Feature>(document4.getNGrams()));

        // We test the n-gram count for these documents too
        for (NGram nGram : ngrams) {
            assertEquals(1, document3.getNGramCount(nGram));
            assertEquals(1, document4.getNGramCount(nGram));
        }
        assertEquals(0, document3.getNGramCount(NGram.getNGram("ag")));
        assertEquals(0, document4.getNGramCount(NGram.getNGram("ag")));

        // Finally we test te Corpus level n-gram count, we know all n-grams occurs twice in the corpus
        ngrams.add(NGram.getNGram("ag"));
        for (NGram nGram : ngrams) {
            assertEquals(2, corpus.getNGramCount(nGram));
        }
    }

    /**
     * Same test as ngramMultipleDocumentWithConfig(), but this will not split the sentence on break boundaries.
     */
    @Test
    public void ngramMultipleDocumentsWithConfigNoBreakWords() {
        NGrammer highFreqNgrammer = new SmileDocumentNGrammer(1, 2, 3, 7);
        Document document1 = new Document("the wheels of the bus");
        Document document2 = new Document("go round and round");
        Document document3 = new Document("round and round");
        Document document4 = new Document("round and round");
        Document document5 = new Document("the wheels of the bus");
        Document document6 = new Document("go round and round");
        Document document7 = new Document("all through town");
        Corpus corpus = new Corpus(document1, document2, document3, document4, document5, document6, document7);
        SmileDocumentNormalizer normalizerNoBreakWords = new SmileDocumentNormalizer(new Words("year"), new Words(), new Words(), false, 1, true, false, false, false);
        corpus.forEach(normalizerNoBreakWords::processText);
        corpus.forEach(tokenizer::tokenize);
        highFreqNgrammer.ngram(corpus);
        corpus.forEach(System.out::println);
        // The unfiltered n-grams produced are:
        // [(round [8]), (wheel [2]), (bu [2]), (town [1]), (round, round [4]), (wheel, bu [2])]
        // But since the frequencies are 3 and 7 we end up with just:
        // [(round, round [4])]
        Set<NGram> ngrams = new HashSet<>();
        NGram roundAndRoundNGram = NGram.getNGram("round", "round");
        ngrams.add(roundAndRoundNGram);
        assertEquals(ngrams, new HashSet<Feature>(corpus.getFeatureSpace()));
        assertEquals(4, corpus.getNGramCount(roundAndRoundNGram));

        // After testing that the corpus level ngrams counts are set we check the same is true at the document level
        assertTrue(document1.getNGrams().isEmpty());
        assertEquals(ngrams, document2.getNGrams());
        assertEquals(1, document2.getNGramCount(roundAndRoundNGram));
        assertEquals(ngrams, document3.getNGrams());
        assertEquals(1, document3.getNGramCount(roundAndRoundNGram));
        assertEquals(ngrams, document4.getNGrams());
        assertEquals(1, document3.getNGramCount(roundAndRoundNGram));
        assertTrue(document5.getNGrams().isEmpty());
        assertEquals(ngrams, document6.getNGrams());
        assertEquals(1, document6.getNGramCount(roundAndRoundNGram));
        assertTrue(document7.getNGrams().isEmpty());
    }

    /**
     *
     */
    @Test
    public void ngramMultipleDocumentsWithConfig() {
        NGrammer highFreqNgrammer = new SmileDocumentNGrammer(1, 2, 3, 7);
        Document document1 = new Document("the wheels of the bus");
        Document document2 = new Document("go round round");
        Document document3 = new Document("round round");
        Document document4 = new Document("round round");
        Document document5 = new Document("the wheels of the bus");
        Document document6 = new Document("go round round");
        Document document7 = new Document("all through town");
        Corpus corpus = new Corpus(document1, document2, document3, document4, document5, document6, document7);
        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        highFreqNgrammer.ngram(corpus);
        corpus.forEach(System.out::println);
        // The unfiltered n-grams produced are:
        // [(round [8]), (wheel [2]), (bu [2]), (town [1]), (round, round [4]), (wheel, bu [2])]
        // But since the frequencies are 3 and 7 we end up with just:
        // [(round, round [4])]
        Set<NGram> ngrams = new HashSet<>();
        NGram roundAndRoundNGram = NGram.getNGram("round", "round");
        ngrams.add(roundAndRoundNGram);
        assertEquals(ngrams, new HashSet<Feature>(corpus.getFeatureSpace()));
        assertEquals(4, corpus.getNGramCount(roundAndRoundNGram));

        // After testing that the corpus level ngrams counts are set we check the same is true at the document level
        assertTrue(document1.getNGrams().isEmpty());
        assertEquals(ngrams, document2.getNGrams());
        assertEquals(1, document2.getNGramCount(roundAndRoundNGram));
        assertEquals(ngrams, document3.getNGrams());
        assertEquals(1, document3.getNGramCount(roundAndRoundNGram));
        assertEquals(ngrams, document4.getNGrams());
        assertEquals(1, document3.getNGramCount(roundAndRoundNGram));
        assertTrue(document5.getNGrams().isEmpty());
        assertEquals(ngrams, document6.getNGrams());
        assertEquals(1, document6.getNGramCount(roundAndRoundNGram));
        assertTrue(document7.getNGrams().isEmpty());
    }

    /**
     *
     */
    @Test(expected = IllegalStateException.class)
    public void ngramUntokenized() {
        Document document = new Document("The big red dog ran over the hill.");
        ngrammer.ngram(document);
    }

    /**
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void ngramBadMinMax() {
        Document document = new Document("The big red dog ran over the hill.");
        NGrammer badNgrammer = new SmileDocumentNGrammer(2, 1);
        badNgrammer.ngram(document);
    }
}
