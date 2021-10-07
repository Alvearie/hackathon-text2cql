/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.alvearie.dream.intent.nlp.Experiment;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExperimentTest {

    @BeforeClass
    public static void setup() {
        try {
            Experiment.setBaseExperimentDirectoryName(Files.createTempDirectory("").toString() + "/experiments");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create working directory for JUnit");
        }
    }

    /**
     * Make sure a new Experiment can get created and initialized properly
     */
    @Test
    public void testNewExperiment() {
        String experimentName = "TestNew" + System.currentTimeMillis();
        Experiment experiment = Experiment.createExperiment(experimentName);
        assertNotNull("Error creating experiment", experiment);
        assertNotNull(experiment.getConfiguration());
        assertNotNull(experiment.getExperimentName());
        assertNotNull(experiment.getExperimentDirectory());
        assertNotNull(experiment.getExperimentDate());
        //
        assertNotNull(Experiment.getCurrentExperimentName());
    }

    /**
     * Make sure getCurrenExperiment returns the correct experiment
     */
    @Test
    public void testGetCurrentExperiment() {
        String experimentName = "TestGetCurrent" + System.currentTimeMillis();
        Experiment experiment = Experiment.createExperiment(experimentName);
        assertNotNull("Error creating experiment", experiment);
        //
        Experiment currentExperiment = Experiment.getCurrentExperiment(experimentName);
        assertEquals(experiment, currentExperiment);
    }

    /**
     * Make sure getCurrentExperiment returns the correct experiment when no experiment name is provided.
     */
    @Test
    public void testAnonymousExperiment() {
        String experimentName = "TestAnonymous" + System.currentTimeMillis();
        Experiment experiment = Experiment.createExperiment(experimentName);
        assertNotNull("Error creating experiment", experiment);
        experiment.save();
        //
        Experiment currentExperiment = Experiment.getCurrentExperiment();
        assertEquals(experiment, currentExperiment);
    }

    /**
     * Make sure all experiment instances for a given name can be retrieved using getLastExperimentBefore
     *
     * @throws InterruptedException
     */
    @Test
    public void testOldExperiment() throws InterruptedException {
        String experimentName = "TestOld" + System.currentTimeMillis();
        Experiment experiment1 = Experiment.createExperiment(experimentName);
        assertNotNull("Error creating experiment", experiment1);
        experiment1.save();
        Thread.sleep(10);
        //
        Experiment experiment2 = Experiment.createExperiment(experimentName);
        assertNotNull("Error creating experiment", experiment2);
        experiment2.save();
        Thread.sleep(10);
        //
        Experiment experiment3 = Experiment.createExperiment(experimentName);
        assertNotNull("Error creating experiment", experiment3);
        experiment3.save();
        Thread.sleep(10);
        //
        assertEquals(experiment3, Experiment.getLastExperimentBefore(experimentName, null));
        //
        assertEquals(experiment2, Experiment.getLastExperimentBefore(experimentName, experiment3.getExperimentDate()));
        //
        assertEquals(experiment1, Experiment.getLastExperimentBefore(experimentName, experiment2.getExperimentDate()));
    }

    /**
     * Make sure getting an experiment that doesn't exist returns null.
     */
    @Test
    public void testNoExperiment() {
        String experimentName = "TestNoExp" + System.currentTimeMillis();
        Experiment experiment = Experiment.getCurrentExperiment(experimentName);
        assertNull("No experiment should exist yet.", experiment);
    }

    /**
     * Make sure the current experiment name is stored statically for the last call.
     */
    @Test
    public void testCurrentExperimentName() {
        String experiment1Name = "TestCurrentName1" + System.currentTimeMillis();
        Experiment experiment1 = Experiment.createExperiment(experiment1Name);
        assertNotNull("Error creating experiment", experiment1);

        String experiment2Name = "TestCurrentName2" + System.currentTimeMillis();
        Experiment experiment2 = Experiment.createExperiment(experiment2Name);
        assertNotNull("Error creating experiment", experiment2);
        assertEquals(experiment2Name, Experiment.getCurrentExperimentName());

        Experiment experiment3 = Experiment.getCurrentExperiment(experiment1Name);
        assertNotNull("Error getting experiment", experiment3);
        assertEquals(experiment1, experiment3);
        assertEquals(experiment1Name, Experiment.getCurrentExperimentName());
    }

    @Test
    public void testSaveLoad() {
        String experimentName = "TestSaveLoad" + System.currentTimeMillis();
        Experiment experiment = Experiment.createExperiment(experimentName);
        assertNotNull("Error creating experiment", experiment);
        String testString = "Test Data";
        String stringFileName = "TestString.dat";
        Experiment.save(stringFileName, testString);
        assertTrue(new File(experiment.getExperimentDirectory(), stringFileName).exists());
        String loadedString = (String) experiment.load(stringFileName);
        assertNotNull("Error loading data", loadedString);
        assertEquals(testString, loadedString);

        Double testDouble = new Double(3.1415926535);
        String doubleFileName = "TestDouble.dat";
        Experiment.save(doubleFileName, testDouble);
        assertTrue(new File(experiment.getExperimentDirectory(), doubleFileName).exists());
        Double loadedDouble = (Double) experiment.load(doubleFileName);
        assertNotNull("Error loading data", loadedDouble);
        assertEquals(testDouble, loadedDouble);
    }

    @Test
    public void testNoPrior() {
        String experimentName = "TestNoPrior" + System.currentTimeMillis();
        Experiment experiment = Experiment.createExperiment(experimentName);
        assertNotNull("Error creating experiment", experiment);
        experiment.save();
        Experiment curExperiment = Experiment.getCurrentExperiment(experimentName);
        assertNotNull("Error retrieving current experiment", curExperiment);
        Experiment olderExperiment = Experiment.getLastExperimentBefore(experimentName, experiment.getExperimentDate());
        assertNull("No experiment should exist yet.", olderExperiment);
    }
}
