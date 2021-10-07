/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering.clusterers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.clustering.Clusterer;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.FeatureVector;

/**
 * A base implementation for the various Smile-based {@link Clusterer}s.
 * <p>
 * Invoking the {@link #cluster(Corpus)} method in this abstract class is necessary by all children implementations as
 * it creates a dense matrix from the given {@link Corpus}.
 *
 */
abstract class AbstractSmileClusterer implements Clusterer {

    protected int k;
    protected Corpus corpus;

    protected double[][] matrix;

    protected int[] clusterIds;
    protected double[][] centroids;

    /**
     * Create a new {@link AbstractSmileClusterer} with k not set. Some algorithms determine K after clustering. Likely this
     * constructor will be used with those implementations;
     */
    public AbstractSmileClusterer() {
    }

    /**
     * Create a new {@link AbstractSmileClusterer}.
     *
     * @param k the K for the algorithm
     * @throws IllegalArgumentException if k < 1
     */
    public AbstractSmileClusterer(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("Clustering is not defined for 0 or negative Ks.");
        }
        this.k = k;
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.clustering.Clusterer#cluster(org.alvearie.nlp.text.Corpus)
     */
    @Override
    public List<Cluster> cluster(Corpus corpus) {
        if (corpus.getFeatureSpace() == null) {
            throw new IllegalStateException("There is no feature space for this corpus. Documents need to be vectorized in order to be clustered.");
        }
        this.corpus = corpus;
        matrix = toDenseMatrix(corpus);
        System.out.println("Clustering with " + this.getClass().getSimpleName() + "...");

        if (corpus.getDocuments().isEmpty()) {
            return new ArrayList<>();
        }

        cluster();

        Map<String, List<Document>> clusterMap = initializeClusterMap();
        // Populate each cluster in the Map with the resulting documents from the clustering algorithm
        // The following array will contain the cluster ID for each document that was clustered
        for (int i = 0; i < clusterIds.length; i++) {
            String clusterId = String.valueOf(clusterIds[i]);
            List<Document> cluster = clusterMap.get(clusterId);
            cluster.add(corpus.getDocuments().get(i));
        }
        List<Cluster> clusters = new ArrayList<>();
        // It is optional for the specific implementations to set the centroids
        // if they are available we use them if not we calcaulte the centroids here
        if (centroids != null) {
            clusterMap.forEach((clusterId, cluster) -> {
                clusters.add(new Cluster(clusterId, cluster));
            });
            List<Feature> features = corpus.getFeatureSpace();
            for (int i = 0; i < centroids.length; i++) {
                Cluster cluster = clusters.get(i);
                double[] centroid = centroids[i];
                FeatureVector centroidVector = new FeatureVector(features, centroid);
                cluster.setCentroid(centroidVector);
            }
        } else {
            clusterMap.forEach((clusterId, cluster) -> {
                Cluster newCluster = new Cluster(clusterId, cluster);
                if (!newCluster.getDocuments().isEmpty()) {
                    newCluster.calculateCentroid();
                }
                clusters.add(newCluster);
            });
        }

        // We sort the cluster by size
        Collections.sort(clusters);

        return clusters;

    }

    /**
     * Run the actual clustering algorithm from Smile. It is expected that after this method runs the {@link #clusterIds}
     * field will be set containing the ID of the cluster for each document in the corpus, and when available the
     * {@link #centroids} matrix will be set with the centroid vector for each document in the corpus.
     * <p>
     * The centroids matrix is not required, but it saves time to not compute it for those algorithms, that already compute
     * it.
     * <p>
     * The {@link #matrix} field will contain the documents to cluster.
     * <p>
     * If needed k is set by the specific implementation (some algorithms don't need a k as input, in this case k will refer
     * to the total number of clusters created by the algorithm on its own.
     */
    protected abstract void cluster();

    /**
     * Converts the given {@link Corpus} to a dense matrix, that is each vector is converted to a dense vector using the
     * corpus' feature space, and then all vectors are turned into a matrix.
     *
     * @param corpus the corpus to convert
     * @return the dense vector matrix
     */
    protected double[][] toDenseMatrix(Corpus corpus) {
        List<FeatureVector> vectors = corpus.getDocuments().stream()
                .map(document -> {
                    FeatureVector vector = document.getVector().toDenseVector(corpus.getFeatureSpace());
                    return vector;
                })
                .collect(Collectors.toList());

        double[][] matrix = FeatureVector.toMatrix(vectors);
        return matrix;
    }

    /**
     * Create a Map from the cluster ID to its elements. All the clusters are initialized in case some of the clusters come
     * back empty so they will be present in the map.
     *
     * @return the cluster map
     */
    protected Map<String, List<Document>> initializeClusterMap() {
        // Iterating over the map needs to be in insertion order for the Clusters to match the indices that come from the
        // Clustering implementation.
        Map<String, List<Document>> clusterMap = new LinkedHashMap<>();
        for (int i = 0; i < k; i++) {
            clusterMap.put(String.valueOf(i), new ArrayList<>());
        }
        return clusterMap;
    }
}
