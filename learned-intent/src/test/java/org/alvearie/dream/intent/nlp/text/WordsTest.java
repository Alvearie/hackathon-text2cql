/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import org.junit.Test;

public class WordsTest {

    @Test
    public void testWords() throws IOException {
        Words words = new Words("a", "b", "c");
        assertTrue("Words didn't contain expected word", words.contains("a"));
        assertFalse("Words contained unexpected word", words.contains("d"));
    }

    @Test
    public void testWordsFromFile() throws IOException {
        Words wordsFromFile = new Words(new File("src/test/resources/WordsTest.txt"));
        assertTrue("Words didn't contain expected word", wordsFromFile.contains("a"));
        assertFalse("Words contain commented outword", wordsFromFile.contains("b"));
        assertTrue("Words didn't contain expected word", wordsFromFile.contains("c"));
        assertTrue("Words didn't contain expected word", wordsFromFile.contains("d"));
        assertFalse("Words contained unexpected word", wordsFromFile.contains("e"));
        assertFalse("Words contained unexpected word", wordsFromFile.contains(" "));
    }

    @Test(expected = NoSuchFileException.class)
    public void testWordsFromNonExistentFile() throws IOException {
        Words wordsFromFile = new Words(new File("FileThatDoesNotExists.txt"));
    }
}
