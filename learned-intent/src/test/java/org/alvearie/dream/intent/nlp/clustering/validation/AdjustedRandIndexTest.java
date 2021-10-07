/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering.validation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.text.Document;
import org.junit.Test;


public class AdjustedRandIndexTest {

    private static final double MARGIN_OF_ERROR = 0.01;

    private AdjustedRandIndex ari;

    /**
     *
     */
    public AdjustedRandIndexTest() {
        ari = new AdjustedRandIndex();
    }

    /**
     * Test method for
     * {@link org.alvearie.dream.intent.nlp.clustering.validation.AdjustedRandIndex#calculate(java.util.List, java.util.List) *
     * <p>
     * This test case simulates the explanation provided in the following
     * <a href="https://scikit-learn.org/stable/modules/clustering.html#clustering-performance-evaluation"> scikit-learn
     * documentation</a>
     */
    @Test
    public void testGetScore() {
        Document document1 = new Document("1");
        Document document2 = new Document("2");
        Document document3 = new Document("3");
        Document document4 = new Document("4");
        Document document5 = new Document("5");
        Document document6 = new Document("6");

        Cluster expectedCluster1 = new Cluster("1", document1, document2, document3);
        Cluster expectedCluster2 = new Cluster("2", document4, document5, document6);
        List<Cluster> expectedClusters = Arrays.asList(expectedCluster1, expectedCluster2);

        Cluster actualCluster1 = new Cluster("1", document1, document2);
        Cluster actualCluster2 = new Cluster("2", document3, document4);
        Cluster actualCluster3 = new Cluster("3", document5, document6);
        List<Cluster> actualClusters = Arrays.asList(actualCluster1, actualCluster2, actualCluster3);

        // The ARI score for the following arrays should be 0.24:
        // actual = [0, 0, 0, 1, 1, 1]
        // expected = [0, 0, 1, 1, 2, 2]
        double ariScore = ari.calculate(expectedClusters, actualClusters);
        assertEquals(0.24, ariScore, MARGIN_OF_ERROR);

        // If the expected and actual clusters are exactly the same down to even the cluster ID we get a perfect score
        actualCluster1 = new Cluster("1", document1, document2, document3);
        actualCluster2 = new Cluster("2", document4, document5, document6);
        actualClusters = Arrays.asList(actualCluster1, actualCluster2);
        ariScore = ari.calculate(expectedClusters, actualClusters);
        assertEquals(1.0, ariScore, MARGIN_OF_ERROR);

        // But ARI doesn't care about cluster IDs so even if the Clusters get assigned the ID reversed we get a perfect score
        actualCluster1 = new Cluster("2", document1, document2, document3);
        actualCluster2 = new Cluster("1", document4, document5, document6);
        actualClusters = Arrays.asList(actualCluster1, actualCluster2);
        ariScore = ari.calculate(expectedClusters, actualClusters);
        assertEquals(1.0, ariScore, MARGIN_OF_ERROR);

        // We even get a perfect score if we use completely different IDs
        actualCluster1 = new Cluster("ID-1", document1, document2, document3);
        actualCluster2 = new Cluster("ID-2", document4, document5, document6);
        actualClusters = Arrays.asList(actualCluster1, actualCluster2);
        ariScore = ari.calculate(expectedClusters, actualClusters);
        assertEquals(1.0, ariScore, MARGIN_OF_ERROR);
    }

}
