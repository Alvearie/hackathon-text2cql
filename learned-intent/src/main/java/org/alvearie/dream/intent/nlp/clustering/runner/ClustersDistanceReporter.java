/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.clustering.runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alvearie.dream.intent.nlp.classification.gt.io.GroundTruthReader;
import org.alvearie.dream.intent.nlp.classification.gt.io.MultiFileGroundTruthReader;
import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.clustering.Clusterer;
import org.alvearie.dream.intent.nlp.clustering.validation.SilhouetteScore;
import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.processors.DocumentVectorizationPipeline;
import org.alvearie.dream.intent.nlp.utils.StopWatch;



public class ClustersDistanceReporter {

    /**
     * The main method here is just used to demonstrate this {@link Clusterer} implementation.
     *
     * @param args no args are needed
     * @throws IOException if there is a problem reading the corpus
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java CriteriaClusteringRunner <multi-file-gt-home>");
            System.exit(1);
        }
        File source = new File(args[0]);
        if (!source.exists()) {
            System.err.println("GT directory " + source + " does not exist.");
            System.exit(1);
        }

        System.out.println("Loading clusters from GT...");
        Map<String, Collection<Document>> clusterMap = loadGT(args[0]);
        List<Document> gt = new ArrayList<>();
        Collection<Collection<Document>> criteriaGT = clusterMap.values();
        for (Collection<Document> criteriaGTForIntent : criteriaGT) {
            gt.addAll(criteriaGTForIntent);
        }
        Corpus corpus = new Corpus(gt);
        Configuration configuration = CriteriaClusteringRunner.getClusteringConfiguration();

        DocumentVectorizationPipeline vectorizer = new DocumentVectorizationPipeline(configuration);
        vectorizer.vectorize(corpus);

        StopWatch sw = StopWatch.start();
        List<Cluster> clusters = Cluster.fromMap(clusterMap);
        System.out.println("Calculating centroids...");
        for (Cluster cluster : clusters) {
            cluster.calculateCentroid();
        }

        System.out.println("Calculating distances...");
        List<Feature> featureSpace = corpus.getFeatureSpace();
        SilhouetteScore scorer = new SilhouetteScore(featureSpace);
        double silhouetteScore = scorer.calculate(clusters);
        Map<Cluster, Cluster> closestClusters = scorer.getClosestClusters();
        Map<Cluster, Double> meanIntraDocumentDistance = scorer.getMeanIntraDocumentDistances();
        System.out.println("Closest Clusters and mean intradocument distance:");
        closestClusters.forEach((cluster, closestCluster) -> System.out.println(cluster.getId() + " -> " + closestCluster.getId() + " (" + meanIntraDocumentDistance.get(cluster) + ")"));
        System.out.println("Silhouette Score: " + silhouetteScore);
        System.out.println("Done! Calculated distances for " + corpus.size() + " clusters in " + sw.stop());
    }

    /**
     * Loads the GT from the given directory
     *
     * @param gtDirectoryName the name of the GT directory
     * @return the list of GT {@link Document}s
     * @throws IOException if there is a problem reading the GT files
     */
    private static Map<String, Collection<Document>> loadGT(String gtDirectoryName) throws IOException {
        File gtDirectory = new File(gtDirectoryName);
        if (!gtDirectory.exists()) {
            System.err.println("The GT directory " + gtDirectoryName + " does not exist. Clustering will continue over the whole corpus.");
            return new HashMap<>();
        }
        System.out.println("Reading known criteria from ground truth...");
        GroundTruthReader gtReader = new MultiFileGroundTruthReader(gtDirectory);
        Map<String, Collection<Document>> gtByIntent = gtReader.read();
        return gtByIntent;
    }

}
