/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.classification.gt.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * Validate the CsvGroundTruthReader class.
 *
 */
public class CSVGroundTruthReaderTest {

    @Test
    public void testLineSplit() throws IOException {
        String line1 = "Criterion Text,Intent";
        String splitLine[] = CSVGroundTruthReader.splitCSVLine(line1);
        assertEquals(2, splitLine.length);
        assertEquals("Criterion Text", splitLine[0]);
        assertEquals("Intent", splitLine[1]);
    }

    @Test
    public void testLineSplitLong() throws IOException {
        String line1 = "Criterion Text,Intent,Column 3";
        String splitLine[] = CSVGroundTruthReader.splitCSVLine(line1);
        assertEquals(3, splitLine.length);
        assertEquals("Criterion Text", splitLine[0]);
        assertEquals("Intent", splitLine[1]);
        assertEquals("Column 3", splitLine[2]);
    }

    @Test
    public void testLineSplitWithComma() throws IOException {
        String line1 = "\"Criterion Text, Comma\",Intent";
        String splitLine[] = CSVGroundTruthReader.splitCSVLine(line1);
        assertEquals(2, splitLine.length);
        assertEquals("\"Criterion Text, Comma\"", splitLine[0]);
        assertEquals("Intent", splitLine[1]);
    }

    @Test
    public void testLineSplitEmpty() throws IOException {
        String line1 = "";
        String splitLine[] = CSVGroundTruthReader.splitCSVLine(line1);
        assertEquals(0, splitLine.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadFileDataNoComma() throws IOException {
        try (InputStream is = new FileInputStream(new File("src/test/resources/badClassificationNoCommaGT.csv"))) {
            CSVGroundTruthReader reader = new CSVGroundTruthReader(is);
            reader.read();
        }
    }
}
