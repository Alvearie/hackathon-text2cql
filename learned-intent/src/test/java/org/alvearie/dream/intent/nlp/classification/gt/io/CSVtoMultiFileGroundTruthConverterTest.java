/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alvearie.dream.intent.nlp.text.Document;
import org.junit.Test;

import com.google.common.io.Files;

/**
 */
public class CSVtoMultiFileGroundTruthConverterTest {

    private GroundTruthWriter splitter;
    private GroundTruthReader reader;

    /**
     * Test method for
     * {@link org.alvearie.dream.intent.nlp.classification.gt.io.CSVtoMultiFileGroundTruthConverter#split(java.io.File, java.io.File)}.
     *
     * @throws IOException
     */
    @Test
    public void testSplit() throws IOException {
        File tempMultiGT = Files.createTempDir();
        splitter = new CSVtoMultiFileGroundTruthConverter(new File("src/test/resources/test-csv-ground-truth/testCriteriaClassification.csv"));
        File actualGTDir = splitter.write(tempMultiGT);
        reader = new MultiFileGroundTruthReader(actualGTDir);
        Map<String, Collection<Document>> gt = reader.read();

        assertEquals(3, gt.size());

        List<Document> diabetes = new ArrayList<>(gt.get("no-diabetes"));
        assertEquals("Clinical history of Type I or Type II diabetes.", diabetes.get(0).getText());
        assertEquals("Subjects who have a history of documented autoimmune disease, even if not clinically severe or never treated with systemic steroids or immunosuppressive agents, are not candidates for this clinical trial, except for autoimmune hypothyroidism and well-controlled Type 1 diabetes mellitus (Hgb A1C <= 6.5%).", diabetes.get(1).getText());
        assertEquals("Subjects with controlled Type I diabetes mellitus on a stable dose of insulin regimen may be eligible for this trial", diabetes.get(2).getText());

        List<Document> legalCapacity = new ArrayList<>(gt.get("legal-capacity"));
        assertEquals("The subject has legal incapacity or limited legal capacity.", legalCapacity.get(0).getText());

        List<Document> creatinine = new ArrayList<>(gt.get("creatinine"));
        assertEquals("Creatinine < 1.5 x institutional ULN", creatinine.get(0).getText());
        assertEquals("Creatinine <= 1.0 mg/dL", creatinine.get(1).getText());
        assertEquals("Creatinine <= 2.0 mg/dL", creatinine.get(2).getText());
        tempMultiGT.deleteOnExit();
    }
}
