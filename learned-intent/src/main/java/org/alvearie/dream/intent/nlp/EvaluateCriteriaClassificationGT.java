/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp;

import java.lang.reflect.Constructor;

import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.classification.gt.GroundTruther;
import org.alvearie.dream.intent.nlp.text.Configuration;

/**
 * Evaluate the criteria classification ground truth using the GroundTruther test framework.
 *
 *
 */
public class EvaluateCriteriaClassificationGT {

	public static void main(String[] args) throws Exception {
		Experiment experiment = Experiment.createExperiment(EvaluateCriteriaClassificationGT.class.getSimpleName());
		Constructor<? extends Classifier> constructor = experiment.getConfiguration().getClassifierClass().getConstructor(Configuration.class);
		Classifier criteriaClassifier = constructor.newInstance(experiment.getConfiguration());
		GroundTruther groundTruther = new GroundTruther(EvaluateCriteriaClassificationGT.class.getResourceAsStream(TrainCriteriaClassification.CRITERIA_CLASSIFICATION_GROUND_TRUTH_CSV), criteriaClassifier, 0.0);
		groundTruther.run();
		experiment.save();
	}
}
