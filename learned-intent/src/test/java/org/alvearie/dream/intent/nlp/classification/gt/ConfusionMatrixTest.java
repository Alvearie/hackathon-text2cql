/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class ConfusionMatrixTest {

	/**
	 * Test method for {@link ConfusionMatrix#register(String, String, int)}
	 */
	@Test
	public void testRegister() {
		// Test the accuracy scores for the given confusion matrix
		//
		//    Expected
		//    A  B  C  D
		// A  5  0  0  0
		// B  2  1  1  2
		// C  2  4  8  2
		// D  1  2  1  0
		//
		ConfusionMatrix confusionMatrix = new ConfusionMatrix("A", "B", "C", "D");
		confusionMatrix.register("A", "A", 5);
		confusionMatrix.register("A", "B", 2);
		confusionMatrix.register("A", "C", 2);
		confusionMatrix.register("A", "D", 1);

		confusionMatrix.register("B", "A", 0);
		confusionMatrix.register("B", "B", 1);
		confusionMatrix.register("B", "C", 4);
		confusionMatrix.register("B", "D", 2);

		confusionMatrix.register("C", "A", 0);
		confusionMatrix.register("C", "B", 1);
		confusionMatrix.register("C", "C", 8);
		confusionMatrix.register("C", "D", 1);

		confusionMatrix.register("D", "A", 0);
		confusionMatrix.register("D", "B", 2);
		confusionMatrix.register("D", "C", 2);
		confusionMatrix.register("D", "D", 0);

		CategoryAccuracy aScores = confusionMatrix.getMatrix().get("A");
		System.out.println("\nA Scores: \n" + aScores);
		assertEquals(0.839, aScores.getAccuracy(), 0.01);
		assertEquals(1, aScores.getPrecision(), 0.01);
		assertEquals(0.5, aScores.getRecall(), 0.01);
		assertEquals(0.667, aScores.getF1(), 0.01 );

		CategoryAccuracy bScores = confusionMatrix.getMatrix().get("B");
		System.out.println("\nB Scores: \n" + bScores);
		assertEquals(0.645, bScores.getAccuracy(), 0.01);
		assertEquals(0.166, bScores.getPrecision(), 0.01);
		assertEquals(0.142, bScores.getRecall(), 0.01);
		assertEquals(0.154, bScores.getF1(), 0.01 );

		// TODO: Finish manual calculation to ensure methods work correctly
//		CategoryAccuracy cScores = confusionMatrix.getMatrix().get("C");
//		System.out.println("\nC Scores: \n" + aScores);
//		assertEquals(1, aScores.getPrecision(), 0.01);
//		assertEquals(0.5, aScores.getRecall(), 0.01);
//		assertEquals(0.839, aScores.getAccuracy(), 0.01);
//		assertEquals(0.667, aScores.getF1(), 0.01 );
//
//		CategoryAccuracy dScores = confusionMatrix.getMatrix().get("D");
//		System.out.println("\nD Scores: \n" + aScores);
//		assertEquals(1, aScores.getPrecision(), 0.01);
//		assertEquals(0.5, aScores.getRecall(), 0.01);
//		assertEquals(0.839, aScores.getAccuracy(), 0.01);
//		assertEquals(0.667, aScores.getF1(), 0.01 );

	}

}
