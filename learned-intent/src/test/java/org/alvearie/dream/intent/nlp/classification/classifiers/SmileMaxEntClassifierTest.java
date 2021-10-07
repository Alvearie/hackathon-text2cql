/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.classifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alvearie.dream.intent.nlp.classification.Classification;
import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.classification.classifiers.smile.SmileMaxEntClassifier;
import org.alvearie.dream.intent.nlp.classification.gt.io.CSVGroundTruthReader;
import org.alvearie.dream.intent.nlp.classification.gt.io.GroundTruthReader;
import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Document;
import org.junit.Test;

/**
 * Test for {@link SmileMaxEntClassifier}.
 *
 */
public class SmileMaxEntClassifierTest {

    private Classifier classifier;

    /**
     *
     */
    public SmileMaxEntClassifierTest() {
        Configuration configuration = new Configuration();
        configuration.setLemmatize(false);
        configuration.setStem(true);
        configuration.setMinimumTokenFrequency(1);
        configuration.setNGramMaxRange(3);
        configuration.setKeepDigitPlaceholder(true);
        configuration.setRemoveParentheticalText(true);
        configuration.setMinimumTokenLength(2);
        classifier = new SmileMaxEntClassifier(configuration);
    }

    /**
     * @throws IOException
     */
    @Test
    public void testClassify() throws IOException {
        File f = new File("src/test/resources/testCriteriaClassification.csv");
        System.err.println(f.getAbsolutePath());
        GroundTruthReader reader = new CSVGroundTruthReader(new File("src/test/resources/test-csv-ground-truth/testCriteriaClassification.csv"));
        Map<String, Collection<Document>> gt = reader.read();
        classifier.train(gt);
        assertTrue("Classifier failed to train.", classifier.isTrained());

        List<Classification> creatinineClassifications = classifier.classify(new Document("Creatinine <= 5.0 mg/dL"));
        System.out.println(creatinineClassifications);
        Classification creatinineClassification = creatinineClassifications.get(0);
        assertEquals("creatinine", creatinineClassification.getCategory());

        List<Classification> diabetesClassifications = classifier.classify(new Document("History of Type I diabetes"));
        System.out.println(diabetesClassifications);
        Classification diabetesClassification = diabetesClassifications.get(0);
        assertEquals("no-diabetes", diabetesClassification.getCategory());

        List<Classification> junkClassifications = classifier.classify(new Document("diabetes and creatinine and legal"));
        System.out.println(junkClassifications);
        Classification junkClassification = junkClassifications.get(0);
        assertTrue("junk text scored higher than ideal text for creatinine", creatinineClassification.getProbability() > junkClassification.getProbability());
        assertTrue("junk text scored higher than ideal text for diabetes", diabetesClassification.getProbability() > junkClassification.getProbability());
    }
}
