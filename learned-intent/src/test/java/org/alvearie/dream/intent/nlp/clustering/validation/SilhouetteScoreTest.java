/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.clustering.validation;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.clustering.Clusterer;
import org.alvearie.dream.intent.nlp.clustering.clusterers.DocumentClusteringPipeline;
import org.alvearie.dream.intent.nlp.clustering.clusterers.SmileKMeansClusterer;
import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.junit.Test;


public class SilhouetteScoreTest {

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.clustering.validation.SilhouetteScore#calculate(java.util.List)}.
     * <p>
     * In this test we use our typical example where we know the optimal k is 3:
     * <li>Cluster 1: the age criteria
     * <li>Cluster 2: the ANC criteria
     * <li>Cluster 3: The creatinine criteria
     * <p>
     * We then do multiple clustering runs for various ks and we calculate the silhouette score for each k. The score for 3
     * should be the maximum (yet less than or equal to 1), since 3 is the optimal number of clusters.
     */
    @Test
    public void testGetScore() {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("Absolute neutrophil count >= 1500/µL");
        Document document3 = new Document("Absolute neutrophil count >= 1600/µL");
        Document document4 = new Document("Creatinine within the normal institutional limits");
        Document document5 = new Document("Creatinine within normal institutional limits");
        Document document6 = new Document("Creatinine is within normal institutional limits");
        Corpus corpus = new Corpus(document1, document2, document3, document4, document5, document6);

        Configuration configuration = new Configuration();
        configuration.setMinimumTokenFrequency(1);

        Clusterer clusterer2 = new DocumentClusteringPipeline(new SmileKMeansClusterer(2), configuration);
        Clusterer clusterer3 = new DocumentClusteringPipeline(new SmileKMeansClusterer(3), configuration);
        Clusterer clusterer4 = new DocumentClusteringPipeline(new SmileKMeansClusterer(4), configuration);
        Clusterer clusterer5 = new DocumentClusteringPipeline(new SmileKMeansClusterer(5), configuration);

        List<Cluster> clusters2 = clusterer2.cluster(corpus);
        List<Cluster> clusters3 = clusterer3.cluster(corpus);
        List<Cluster> clusters4 = clusterer4.cluster(corpus);
        List<Cluster> clusters5 = clusterer5.cluster(corpus);

        System.out.println("Clusters 2:");
        clusters2.forEach(System.out::println);

        System.out.println("Clusters 3:");
        clusters3.forEach(System.out::println);

        System.out.println("Clusters 4:");
        clusters4.forEach(System.out::println);

        System.out.println("Clusters 5:");
        clusters5.forEach(System.out::println);

        SilhouetteScore silhouetteScorer = new SilhouetteScore(corpus.getFeatureSpace());
        double score2 = silhouetteScorer.calculate(clusters2);
        double score3 = silhouetteScorer.calculate(clusters3);
        double score4 = silhouetteScorer.calculate(clusters4);
        double score5 = silhouetteScorer.calculate(clusters5);

        System.out.println("Silhouette score for k = 2: " + score2);
        System.out.println("Silhouette score for k = 3: " + score3);
        System.out.println("Silhouette score for k = 4: " + score4);
        System.out.println("Silhouette score for k = 5: " + score5);

        assertTrue(score3 > score2);
        assertTrue(score3 > score4);
        assertTrue(score3 > score5);
        assertTrue(score3 <= 1);
    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.clustering.validation.SilhouetteScore#calculate(java.util.List)}.
     * <p>
     * At one point there was a bug in SmileKMeans that was preventing the clusters from being set their right centroids.
     * This bug manifested itself when calculating the score of a clustering results with a high-enough K because a NaN
     * centroid would be randomly assigned to a good non-empyy cluster that should have had a non-NaN centroid. This test
     * ensures the bug is not present.
     */
    @Test
    public void testGetScoreHighK() {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("Absolute neutrophil count >= 1500/µL");
        Document document3 = new Document("Absolute neutrophil count >= 1600/µL");
        Document document4 = new Document("Creatinine within the normal institutional limits");
        Document document5 = new Document("Creatinine within normal institutional limits");
        Document document6 = new Document("Creatinine is within normal institutional limits");
        Corpus corpus = new Corpus(document1, document2, document3, document4, document5, document6);

        Configuration configuration = new Configuration();
        configuration.setMinimumTokenFrequency(1);

        Clusterer clusterer100 = new DocumentClusteringPipeline(new SmileKMeansClusterer(100), configuration);

        List<Cluster> clusters100 = clusterer100.cluster(corpus);

        System.out.println("Clusters 100:");
        clusters100.forEach(System.out::println);

        SilhouetteScore silhouetteScorer = new SilhouetteScore(corpus.getFeatureSpace());
        double score100 = silhouetteScorer.calculate(clusters100);

        System.out.println("Silhouette score for k = 100: " + score100);
    }

}
