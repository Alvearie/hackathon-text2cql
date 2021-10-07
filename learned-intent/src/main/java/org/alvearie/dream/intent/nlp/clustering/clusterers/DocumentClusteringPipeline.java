/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering.clusterers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.clustering.Clusterer;
import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentVectorizer;
import org.alvearie.dream.intent.nlp.text.io.DocumentReader;
import org.alvearie.dream.intent.nlp.text.io.SimpleTextFileDocumentReader;
import org.alvearie.dream.intent.nlp.text.processors.DocumentVectorizationPipeline;
import org.alvearie.dream.intent.nlp.utils.StopWatch;

/**
 * A default Clustering flow which includes normalization, tokenization, n-gramming, tf-idf vectorization and ultimately
 * clustering.
 * <p>
 * If the given corpus contains {@link Document}s that are already vectorized, this pipeline will not re-vectorize, it
 * will simply run the clustering algorithm.
 *
 */
public class DocumentClusteringPipeline implements Clusterer {

    private Configuration configuration;
    private Clusterer clusterer;

    /**
     * Creates a {@link DocumentClusteringPipeline} with the default configuration and clusterer.
     *
     * @param k the number of clusters to create
     */
    public DocumentClusteringPipeline(int k) {
        this(new SmileKMeansClusterer(k), null);
    }

    /**
     * Creates a {@link DocumentClusteringPipeline} which will cluster a {@link Corpus} into <code>k</code> clusters.
     *
     * @param clusterer the {@link Clusterer} object to use in this pipeline
     * @param configuration the NLP text processing configuration, if null the default configuration will be used
     */
    public DocumentClusteringPipeline(Clusterer clusterer, Configuration configuration) {
        if (configuration == null) {
            configuration = Configuration.getDefault();
        }
        this.clusterer = clusterer;
        this.configuration = configuration;
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.clustering.Clusterer#cluster(org.alvearie.nlp.text.Corpus)
     */
    @Override
    public List<Cluster> cluster(Corpus corpus) {
        StopWatch sw = StopWatch.start();

        // If the Corpus already has a feature space it means it's already vectorized so we skip vectorization
        if (corpus.getFeatureSpace() == null) {
            DocumentVectorizer vectorizer = new DocumentVectorizationPipeline(configuration);
            vectorizer.vectorize(corpus);
        }

        corpus.getDocuments().removeIf(document -> document.getNGrams().isEmpty());
        System.out.println("Dropped 0-vector documents");

        System.out.println("Matrix size: " + corpus.size() + "x" + corpus.getFeatureSpace().size());
        System.out.println("Feature Space: " + corpus.getFeatureSpace());

        List<Cluster> clusters = clusterer.cluster(corpus);

        System.out.println("Done! Clustered " + corpus.size() + " criteria in " + sw.stop());

        // If there are more than 3 clusters we check if the size of the biggest cluster is 2x greater than
        // its next biggest cluster, if so this is a good indication that the biggest cluster is the "noise" or
        // garbage cluster and we re-cluster that last cluster.
        if (clusters.size() > 3) {
            Cluster lastCluster = clusters.get(clusters.size() - 1);
            Cluster nextTolastCluster = clusters.get(clusters.size() - 2);
            if (lastCluster.size() > (nextTolastCluster.size() * 2)) {
                clusters.remove(lastCluster);
                Corpus newCorpus = new Corpus(lastCluster.getDocuments());
                newCorpus.setFeatureSpace(corpus.getFeatureSpace());
                List<Cluster> reClusters = clusterer.cluster(newCorpus);
                clusters.addAll(reClusters);
                System.out.println("Done! Reclustered " + newCorpus.size() + " noise cluster in " + sw.stop());
            }
        }

        return clusters;
    }

    /**
     * The main method here is just used to demonstrate this {@link Clusterer} implementation.
     *
     * @param args no args are needed
     * @throws IOException if there is a problem reading the corpus
     */
    public static void main(String[] args) throws IOException {
        int k = 50;
        System.out.println("Reading corpus...");
        File source = new File("src/test/resources/small-corpus.txt");
        DocumentReader reader = new SimpleTextFileDocumentReader(source, StandardCharsets.UTF_8);
        List<Document> documents = reader.read();
        Corpus corpus = new Corpus(documents);
        Clusterer clusterer = new DocumentClusteringPipeline(k);
        List<Cluster> clusters = clusterer.cluster(corpus);
        System.out.println("Clusters:");
        System.out.println("-------------");
        clusters.forEach(System.out::println);
    }

}
