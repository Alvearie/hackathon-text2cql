/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering.clusterers;

import org.alvearie.dream.intent.nlp.clustering.Clusterer;

import smile.clustering.KMeans;

/**
 * A {@link Clusterer} implemented using Smile's KMeans clusterization.
 *
 */
public class SmileKMeansClusterer extends AbstractSmileClusterer {

    /**
     * Creates a {@link SmileKMeansClusterer} with the given K.
     *
     * @param k the number or clusters
     */
    public SmileKMeansClusterer(int k) {
        super(k);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.clustering.clusterers.AbstractSmileClusterer#cluster()
     */
    @Override
    protected void cluster() {
        KMeans clusterer = new KMeans(matrix, k);
        clusterIds = clusterer.getClusterLabel();
        centroids = clusterer.centroids();
    }
}
