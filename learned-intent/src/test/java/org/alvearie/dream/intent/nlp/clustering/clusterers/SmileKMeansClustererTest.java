/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.clustering.clusterers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.clustering.Clusterer;
import org.alvearie.dream.intent.nlp.clustering.validation.AdjustedRandIndex;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTextTransformer;
import org.alvearie.dream.intent.nlp.text.DocumentTokenizer;
import org.alvearie.dream.intent.nlp.text.DocumentVectorizer;
import org.alvearie.dream.intent.nlp.text.NGram;
import org.alvearie.dream.intent.nlp.text.NGrammer;
import org.alvearie.dream.intent.nlp.text.processors.DocumentBoWVectorizer;
import org.alvearie.dream.intent.nlp.text.processors.TFIDFVectorizer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentNGrammer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentNormalizer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentTokenizer;
import org.junit.Test;


public class SmileKMeansClustererTest {

    private DocumentVectorizer vectorizer;
    private DocumentVectorizer bow;
    private NGrammer ngrammer;
    private DocumentTextTransformer normalizer;
    private DocumentTokenizer tokenizer;
    private Clusterer clusterer;

    /**
     *
     */
    public SmileKMeansClustererTest() {
        vectorizer = new TFIDFVectorizer();
        ngrammer = new SmileDocumentNGrammer(1, 2);
        tokenizer = new SmileDocumentTokenizer();
        normalizer = new SmileDocumentNormalizer();
        bow = new DocumentBoWVectorizer();
    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.clustering.clusterers.SmileKMeansClusterer#cluster(java.util.List)}.
     */
    @Test
    public void testCluster() {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("Ages 18 to 100 year");
        Document document3 = new Document("Absolute neutrophil count >= 1500/µL");
        Document document4 = new Document("Absolute neutrophil count >= 1600/µL");
        Corpus corpus = new Corpus(document1, document2, document3, document4);

        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        ngrammer.ngram(corpus);
        corpus.forEach(bow::vectorize);
        corpus.forEach(vectorizer::vectorize);
        corpus.forEach(System.out::println);

        // There are 2 clusters that we anticipate
        clusterer = new SmileKMeansClusterer(2);
        List<Cluster> clusters = clusterer.cluster(corpus);

        System.out.println("Clusters:");
        clusters.forEach(System.out::println);

        assertEquals(2, clusters.size());

        // We test that the clusters are the right size and the centroids are set
        Cluster actualCluster1 = clusters.get(0);
        assertEquals(2, actualCluster1.getDocuments().size());
        assertNotNull(actualCluster1.getCentroid());

        Cluster actualCluster2 = clusters.get(1);
        assertEquals(2, actualCluster2.getDocuments().size());
        assertNotNull(actualCluster2.getCentroid());

        // Then we test the the contents are correct using ARI
        Cluster expectedCluster1 = new Cluster("Cluster 1", document1, document2);
        Cluster expectedCluster2 = new Cluster("Cluster 2", document3, document4);
        List<Cluster> expectedClusters = Arrays.asList(expectedCluster1, expectedCluster2);

        AdjustedRandIndex ari = new AdjustedRandIndex();
        double ariScore = ari.calculate(expectedClusters, clusters);
        // We expect a perfect match 1.0
        assertEquals(1.0, ariScore, 0.0);
    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.clustering.clusterers.SmileKMeansClusterer#cluster(java.util.List)}.
     */
    @Test
    public void testClusterOrderAndCentroids() {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("Absolute neutrophil count >= 1500/µL");
        Document document3 = new Document("Absolute neutrophil count >= 1600/µL");
        Document document4 = new Document("Creatinine within the normal institutional limits");
        Document document5 = new Document("Creatinine within normal institutional limits");
        Document document6 = new Document("Creatinine is within normal institutional limits");

        Corpus corpus = new Corpus(document1, document2, document3, document4, document5, document6);

        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        ngrammer.ngram(corpus);
        corpus.forEach(bow::vectorize);
        corpus.forEach(vectorizer::vectorize);
        corpus.forEach(System.out::println);

        // There are 3 clusters that we anticipate
        // We test that they are ordered from smallest to (age with 1) to largest (creatinine with 3)
        clusterer = new SmileKMeansClusterer(3);
        List<Cluster> clusters = clusterer.cluster(corpus);

        System.out.println("Clusters:");
        clusters.forEach(System.out::println);

        assertEquals(3, clusters.size());

        Cluster ageCluster = clusters.get(0);
        List<Document> age = ageCluster.getDocuments();
        assertEquals(1, age.size());
        age.contains(document1);
        // Then we test that the centroid is the right centroid
        assertTrue(ageCluster.getCentroid().toSparseVector().getFeatures().contains(NGram.getNGram("ag")));
        assertFalse(ageCluster.getCentroid().toSparseVector().getFeatures().contains(NGram.getNGram("neutrophil")));
        assertFalse(ageCluster.getCentroid().toSparseVector().getFeatures().contains(NGram.getNGram("creatinin")));

        Cluster neutrophilCluster = clusters.get(1);
        List<Document> anc = neutrophilCluster.getDocuments();
        assertEquals(2, anc.size());
        anc.contains(document2);
        anc.contains(document3);
        assertFalse(neutrophilCluster.getCentroid().toSparseVector().getFeatures().contains(NGram.getNGram("ag")));
        assertTrue(neutrophilCluster.getCentroid().toSparseVector().getFeatures().contains(NGram.getNGram("neutrophil")));
        assertFalse(neutrophilCluster.getCentroid().toSparseVector().getFeatures().contains(NGram.getNGram("creatinin")));

        Cluster creatinineCluster = clusters.get(2);
        List<Document> creatinine = creatinineCluster.getDocuments();
        assertEquals(3, creatinine.size());
        creatinine.contains(document4);
        creatinine.contains(document5);
        creatinine.contains(document6);
        assertFalse(creatinineCluster.getCentroid().toSparseVector().getFeatures().contains(NGram.getNGram("ag")));
        assertFalse(creatinineCluster.getCentroid().toSparseVector().getFeatures().contains(NGram.getNGram("neutrophil")));
        assertTrue(creatinineCluster.getCentroid().toSparseVector().getFeatures().contains(NGram.getNGram("creatinin")));
    }

    /**
     * The purpose of this test is to ensure that this simple 6 document corpus gets clustered correctly. Test method for
     * {@link org.alvearie.dream.intent.nlp.clustering.clusterers.SmileKMeansClusterer#cluster(java.util.List)}.
     */
    @Test
    public void test3SimpleClusters() {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("Absolute neutrophil count >= 1500/µL");
        Document document3 = new Document("Absolute neutrophil count >= 1600/µL");
        Document document4 = new Document("Creatinine within upper normal institutional limits");
        Document document5 = new Document("Creatinine Within normal institutional limits");
        Document document6 = new Document("creatinine less than normal institutional limits");

        Corpus corpus = new Corpus(document1, document2, document3, document4, document5, document6);

        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        ngrammer.ngram(corpus);
        corpus.forEach(bow::vectorize);
        corpus.forEach(vectorizer::vectorize);
        corpus.forEach(System.out::println);

        // There are 3 clusters that we anticipate
        clusterer = new SmileKMeansClusterer(3);
        List<Cluster> clusters = clusterer.cluster(corpus);

        System.out.println("Clusters:");
        clusters.forEach(System.out::println);

        List<Document> age = clusters.get(0).getDocuments();
        assertEquals(1, age.size());
        age.contains(document1);

        List<Document> anc = clusters.get(1).getDocuments();
        assertEquals(2, anc.size());
        anc.contains(document2);
        anc.contains(document3);

        List<Document> creatinine = clusters.get(2).getDocuments();
        assertEquals(3, creatinine.size());
        creatinine.contains(document4);
        creatinine.contains(document5);
        creatinine.contains(document6);
    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.clustering.clusterers.SmileKMeansClusterer#cluster(java.util.List)}.
     */
    @Test(expected = IllegalStateException.class)
    public void testUnvectorizedDocuments() {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("Ages 18 to 100 year");
        Document document3 = new Document("Absolute neutrophil count >= 1500/µL");
        Document document4 = new Document("Absolute neutrophil count >= 1600/µL");
        Corpus corpus = new Corpus(document1, document2, document3, document4);

        corpus.forEach(document -> document.getVector());

        clusterer = new SmileKMeansClusterer(2);
        // This should throw an exception b/c the corpus has not been vectorized
        clusterer.cluster(corpus);
    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.clustering.clusterers.SmileKMeansClusterer#cluster(java.util.List)}. We just
     * test that for an empty Corpus the cluster does not crash.
     */
    @Test
    public void testClusterEmptyCorpus() {
        Corpus corpus = new Corpus();

        corpus.forEach(normalizer::processText);
        corpus.forEach(tokenizer::tokenize);
        ngrammer.ngram(corpus);
        corpus.forEach(bow::vectorize);
        corpus.forEach(vectorizer::vectorize);
        corpus.forEach(System.out::println);

        // There are 2 clusters that we anticipate
        clusterer = new SmileKMeansClusterer(2);
        List<Cluster> clusters = clusterer.cluster(corpus);

        System.out.println("Clusters:");
        clusters.forEach(System.out::println);

        assertEquals(0, clusters.size());
    }
}
