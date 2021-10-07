/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors.smile;

import static org.junit.Assert.assertEquals;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.NGrammer;
import org.alvearie.dream.intent.nlp.text.Words;
import org.junit.Test;

public class SmileDocumentNormalizerTest {

    /**
     *
     */
    @Test
    public void normalizeWithStemming() {
        SmileDocumentNormalizer normalizer = new SmileDocumentNormalizer(new Words("year"), new Words(), new Words(), false, 1, true, false, false, false);
        Document document = new Document("The red beautiful dog ran over the hill.");
        normalizer.processText(document);
        System.out.println(document);
        assertEquals("red beauti dog ran hill", document.getText());
    }

    /**
     *
     */
    @Test
    public void normalizeWithLemmatization() {
        SmileDocumentNormalizer normalizer = new SmileDocumentNormalizer(new Words(), new Words(), new Words(), false, 1, false, true, false, false);
        Document document = new Document("The red beautiful dog ran over the hill.");
        normalizer.processText(document);
        System.out.println(document);
        assertEquals("red beautiful dog run hill", document.getText());
    }

    /**
     *
     */
    @Test
    public void normalizeWithConfig() {
        SmileDocumentNormalizer normalizer = new SmileDocumentNormalizer(new Words("abcde"), new Words("ab"), new Words(), false, 4, true, false, false, false);
        normalizer.setStem(false);
        Document document = new Document("ab abc abcd abcde abcdef");
        normalizer.processText(document);
        System.out.println(document);
        // abc gets kicked out b/c it's less than 4 chars
        // abcd makes it in b/c it's 4 chars
        // abcde gets kicked out b/c it's a stop word
        // abcdef makes it in it's a regular word
        // ab makes it in b/c it's an allowed word
        assertEquals("ab abcd abcdef", document.getText());
    }

    /**
     * This test ensures that hyphenated words are kept around and left hyphenated.
     */
    @Test
    public void normalizeWithLemmatizationHyphenateWords() {
        SmileDocumentNormalizer normalizer = new SmileDocumentNormalizer(new Words(), new Words(), new Words(), false, 1, false, true, false, false);
        // We want to make sure to process hyphenated tokens
        Document document = new Document("The red-beautiful dog, ran over >= 5th hill!");
        normalizer.processText(document);
        System.out.println(document);
        assertEquals("red beautiful dog run hill", document.getText());

        document = new Document("CD20-positive disease");
        normalizer.processText(document);
        System.out.println(document);
        assertEquals("cd20 positive disease", document.getText());
    }

    /**
     * Test that stop words are removed from the text during normalization
     */
    @Test
    public void normalizeWithStopWords() {
        SmileDocumentNormalizer normalizer = new SmileDocumentNormalizer(new Words("a", "the", "of"), new Words(), new Words(), true, 1, false, false, false, false);
        // We want to make sure to preserve hyphenated tokens
        Document document = new Document("The dog is a ball of energy.");
        normalizer.processText(document);
        System.out.println(document);
        assertEquals("dog " + NGrammer.BREAK + " ball energy", document.getText());
    }

    /**
     * Test that accented letters are normalized.
     */
    @Test
    public void normalizeAccentedLetters() {
        SmileDocumentNormalizer normalizer = new SmileDocumentNormalizer(new Words("a", "the", "of"), new Words(), new Words(), true, 1, false, false, false, false);
        Document documentA = new Document("ÀÂàâÃãáÁàÀãäÄÃâÂ");
        String expectedA = "aaaaaaaaaaaaaaaa";
        normalizer.processText(documentA);
        assertEquals(expectedA, documentA.getText());

        Document documentE = new Document("ÈÉÊËèéêëéÉèÈëËêÊ");
        String expectedE = "eeeeeeeeeeeeeeee";
        normalizer.processText(documentE);
        assertEquals(expectedE, documentE.getText());

        Document documentI = new Document("ÏÎïîíÍìÌïÏîÎ");
        String expectedI = "iiiiiiiiiiii";
        normalizer.processText(documentI);
        assertEquals(expectedI, documentI.getText());

        Document documentO = new Document("ÔôÕõóÓòÒõöÖÕôÔ");
        String expectedO = "oooooooooooooo";
        normalizer.processText(documentO);
        assertEquals(expectedO, documentO.getText());

        Document documentU = new Document("ÛÙûùúÚùÙüÜûÛ");
        String expectedU = "uuuuuuuuuuuu";
        normalizer.processText(documentU);
        assertEquals(expectedU, documentU.getText());

        Document documentOther = new Document("ÇççÇýÝñÿÑ");
        String expectedOther = "ccccyynyn";
        normalizer.processText(documentOther);
        assertEquals(expectedOther, documentOther.getText());
    }

    /**
     * Test that break words and control characters (. and ;) are used to split the sentence appropriately.
     */
    @Test
    public void normalizeWithBreakWords() {
        SmileDocumentNormalizer normalizer = new SmileDocumentNormalizer(new Words(), new Words("some", "and"), new Words("are"), true, 1, false, false, false, false);
        // We want to make sure to preserve hyphenated tokens
        Document document = new Document("Dogs are white. Dogs are black; Some dogs are brown (not green) and tan.");
        normalizer.processText(document);
        System.out.println(document);
        assertEquals("dogs " + NGrammer.BREAK + " white " + NGrammer.BREAK + " dogs " + NGrammer.BREAK + " black " + NGrammer.BREAK + " some dogs " + NGrammer.BREAK + " brown " + NGrammer.BREAK + " green " + NGrammer.BREAK + " and tan", document.getText());
    }
}
