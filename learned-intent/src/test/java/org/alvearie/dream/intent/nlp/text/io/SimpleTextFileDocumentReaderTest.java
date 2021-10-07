/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.alvearie.dream.intent.nlp.text.Document;
import org.junit.Test;


public class SimpleTextFileDocumentReaderTest {

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.text.io.SimpleTextFileDocumentReader#read()}.
     *
     * @throws IOException
     */
    @Test
    public void testRead() throws IOException {
        File file = new File("src/test/resources/reader/testDocumentsFile.txt");
        DocumentReader reader = new SimpleTextFileDocumentReader(file, StandardCharsets.UTF_8);
        List<Document> documents = reader.read();
        assertEquals(4, documents.size());
        assertEquals("Document 1", documents.get(0).getText());
        assertEquals("Document 2.", documents.get(1).getText());
        assertEquals("Document 3.", documents.get(2).getText());
        assertEquals("文件4", documents.get(3).getText());
    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.text.io.SimpleTextFileDocumentReader#read()}.
     *
     * @throws IOException
     */
    @Test(expected = FileNotFoundException.class)
    public void testReadNotExistent() throws IOException {
        File file = new File("src/test/resources/reader/does-not-exist.txt");
        DocumentReader reader = new SimpleTextFileDocumentReader(file, StandardCharsets.UTF_8);
        reader.read();
    }

}
