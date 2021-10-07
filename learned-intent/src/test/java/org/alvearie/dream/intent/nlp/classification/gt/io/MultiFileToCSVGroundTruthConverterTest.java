/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.classification.gt.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

public class MultiFileToCSVGroundTruthConverterTest {

    private GroundTruthWriter joiner;

    /**
     * Test method for
     * {@link org.alvearie.dream.intent.nlp.classification.gt.io.MultiFileToCSVGroundTruthConverter#split(java.io.File, java.io.File)}.
     *
     * @throws IOException
     */
    @Test
    public void testJoin() throws IOException {
        Path csvGTDirectory = Files.createTempDirectory(null);
        joiner = new MultiFileToCSVGroundTruthConverter(new File("src/test/resources/test-multi-file-ground-truth-directory-2"));
        File csvGT = joiner.write(csvGTDirectory.toFile());
        List<String> actual = Files.readAllLines(csvGT.toPath());
        // The expected file is sorted by intention then by document text alphabetically in ascending order
        List<String> expected = Files.readAllLines(new File("src/test/resources/test-csv-ground-truth/csvGTFromMultiFileDirectory2.csv").toPath());
        assertEquals(expected, actual);
        csvGT.deleteOnExit();
    }

}
