/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.FeatureVector;

import java.util.Set;

/**
 * A {@link Cluster} represents a grouped of {@link Document}s that were clustered together by a clustering algorithm.
 * It contains some sort of unique identifier, the centroid used to group the {@link Document}s, and the actual
 * {@link List} of documents in the cluster.
 * <p>
 * A {@link Cluster}s natural order is determined by its size.
 *
 */
public class Cluster implements Comparable<Cluster> {

    private String id;
    private FeatureVector centroid;
    private List<Document> documents;

    /**
     * Create a {@link Cluster} with the given ID and {@link Document}s
     *
     * @param id the cluster ID
     * @param documents the cluster's contents
     */
    public Cluster(String id, List<Document> documents) {
        this.id = id;
        this.documents = documents;
    }

    /**
     * Create a {@link Cluster} with the given ID and {@link Document}s
     *
     * @param id the cluster ID
     * @param documents the cluster's contents
     */
    public Cluster(String id, Document... documents) {
        this(id, Arrays.asList(documents));
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the documents
     */
    public List<Document> getDocuments() {
        return Collections.unmodifiableList(documents);
    }

    /**
     * Sets the centroid of this {@link Cluster}.
     *
     * @param centroidVector the centroid
     */
    public void setCentroid(FeatureVector centroidVector) {
        centroid = centroidVector;
    }

    /**
     * @return the centroid vector for this cluster or null if it is not set
     */
    public FeatureVector getCentroid() {
        return centroid;
    }

    /**
     * The centroid for a cluster can be set or if it isn't available it can be calculated using this method. After this
     * method is invoked the centroid can be retrieved using {@link #getCentroid()}.
     * <p>
     * The feature space is required to calcaulte the centroid.
     *
     * @throws IllegalStateException if a document in the cluster is missing its feature space
     */
    public void calculateCentroid() {
        List<Feature> featureSpace = null;
        List<FeatureVector> vectors = new ArrayList<>(documents.size());
        for (Document document : documents) {
            if (document.getFeatureSpace() == null) {
                throw new IllegalStateException("Not all documents in the cluster have a feature space, the centroid cannt be computed.");
            }
            featureSpace = document.getFeatureSpace();
            vectors.add(document.getVector());
        }
        this.centroid = FeatureVector.centroid(vectors, featureSpace);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Cluster o) {
        return getDocuments().size() - o.getDocuments().size();
    }

    /**
     * @return the size of this cluster or zero if it's empty
     */
    public int size() {
        return getDocuments().size();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Cluster: " + getId() + " - " + getDocuments().size() + "\n");
        if (getCentroid() != null) {
            // Will show top-10 features not all
            Set<Feature> features = getCentroid().toSparseVector().sorted().getFeatures();
            List<Feature> subList = new ArrayList<>(features).subList(0, Math.min(features.size(), 9));
            buffer.append("Relevant Features: " + subList + "\n");
        }
        buffer.append("==============\n");
        getDocuments().forEach(document -> buffer.append(document.getId() + " - " + document.getOriginalText() + "\n"));
        return buffer.toString();
    }

    /**
     * Builds a {@link List} of {@link Cluster}s from the given {@link Map} of cluster IDs to {@link Document}s in that
     * cluster.
     *
     * @param clustersAsMap the clustrs as a {@link Map}
     * @return the list of {@link Cluster}s created from the given {@link Map}, or an empty list if the map is empty
     */
    public static List<Cluster> fromMap(Map<String, Collection<Document>> clustersAsMap) {
        List<Cluster> clusters = new ArrayList<>();
        Set<Entry<String, Collection<Document>>> entries = clustersAsMap.entrySet();
        for (Entry<String, Collection<Document>> entry : entries) {
            String clusterId = entry.getKey();
            Collection<Document> documents = entry.getValue();
            clusters.add(new Cluster(clusterId, new ArrayList<Document>(documents)));
        }
        return clusters;
    }
}
