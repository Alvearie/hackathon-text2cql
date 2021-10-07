/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.classification.gt;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.text.Configuration;
import org.junit.Test;


public class GroundTrutherTest {

    /**
     * Test method for {@link GroundTruther#run()}
     *
     * @throws IOException
     */
    @Test
    public void testRun() throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Configuration configuration = new Configuration();
        File groundTruthDir = new File("src/test/resources/testClassificationGT.csv");
        Constructor<? extends Classifier> constructor = configuration.getClassifierClass().getConstructor(Configuration.class);
        Classifier classifier = constructor.newInstance(configuration);
        GroundTruther groundTruther = new GroundTruther(groundTruthDir, classifier);
        groundTruther.run();
    }
}
