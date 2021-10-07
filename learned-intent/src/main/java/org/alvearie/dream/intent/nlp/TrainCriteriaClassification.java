/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.classification.gt.io.CSVGroundTruthReader;
import org.alvearie.dream.intent.nlp.classification.gt.io.GroundTruthReader;
import org.alvearie.dream.intent.nlp.classification.gt.io.GroundTruthUtils;
import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.Words;
import org.alvearie.dream.intent.nlp.utils.FileUtilities;

/**
 * This class will train the criteria classification model using current training data
 *
 *
 */
public class TrainCriteriaClassification {

	public static final String CRITERIA_CLASSIFICATION_GROUND_TRUTH_CSV = "/criteriaClassification.csv";
	public static final String CRITERIA_CLASSIFICATION_GROUND_TRUTH_USER_CSV = "/criteriaClassification_user.csv";
	public static final String CRITERIA_CLASSIFICATION_MODEL = "criteria-classification.model";

	/**
	 * This will train a model for criteria classification. The resulting model will be put in an experiment directory (<<user.home>>/experiments by default). To deliver this as the current model, it must be moved into the src/main/resources folder of policyadvisor-ctm-ingestion.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Experiment experiment = Experiment.createExperiment(TrainCriteriaClassification.class.getSimpleName());
		try {
			experiment.getConfiguration().setStopWords(new Words(FileUtilities.getFileWithPathOrClasspath("classificationStopWords.txt")));
		} catch (IOException e) {
			System.err.println("Error loading stop words.");
		}
		try {
			experiment.getConfiguration().setAllowedWords(new Words(FileUtilities.getFileWithPathOrClasspath("classificationAllowWords.txt")));
		} catch (IOException e) {
			System.err.println("Error loading allow words.");
		}
		try {
			experiment.getConfiguration().setBreakWords(new Words(FileUtilities.getFileWithPathOrClasspath("classificationBreakWords.txt")));
		} catch (IOException e) {
			System.err.println("Error loading break words.");
		}
		GroundTruthReader reader = new CSVGroundTruthReader(TrainCriteriaClassification.class.getResourceAsStream(CRITERIA_CLASSIFICATION_GROUND_TRUTH_CSV));
		Map<String, Collection<Document>> groundTruth = reader.read();
		// Load second GT file and merge
		GroundTruthReader reader2 = new CSVGroundTruthReader(TrainCriteriaClassification.class
				.getResourceAsStream(TrainCriteriaClassification.CRITERIA_CLASSIFICATION_GROUND_TRUTH_USER_CSV));
		Map<String, Collection<Document>> groundTruth2 = reader2.read();
		groundTruth = GroundTruthUtils.add(groundTruth, groundTruth2);
		Classifier classifier = Configuration.getDefault().getClassifierClass().newInstance();
		classifier.train(groundTruth);

		File experimentFile = new File(CRITERIA_CLASSIFICATION_MODEL);
		classifier.save(experimentFile);
		experiment.save();
	}
}
