/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering.clusterers;

import org.alvearie.dream.intent.nlp.clustering.Clusterer;

import smile.clustering.XMeans;

/**
 * A {@link Clusterer} implemented using Smile's {@link XMeans} clusterization.
 * <p>
 * Looking at the implementation it seems like this clustering algorithm is meant to be used with data sets where some
 * clusters are expected to be greater than 25 entries.
 *
 */
public class SmileXMeansClusterer extends AbstractSmileClusterer {

    /**
     * Creates a {@link SmileXMeansClusterer} with the given K.
     *
     * @param k the number or clusters
     */
    public SmileXMeansClusterer(int k) {
        super(k);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.clustering.clusterers.AbstractSmileClusterer#cluster()
     */
    @Override
    protected void cluster() {
        XMeans clusterer = new XMeans(matrix, k);
        clusterIds = clusterer.getClusterLabel();
        centroids = clusterer.centroids();
    }
}
