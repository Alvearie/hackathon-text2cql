/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering.clusterers;

import org.alvearie.dream.intent.nlp.clustering.Clusterer;

import smile.clustering.CLARANS;
import smile.math.distance.EuclideanDistance;

/**
 * A {@link Clusterer} implemented using Smile's {@link CLARANS} clusterization.
 *
 */
public class SmileCLARANSClusterer extends AbstractSmileClusterer {

    /**
     * Creates a {@link SmileCLARANSClusterer} with the given K.
     *
     * @param k the number or clusters
     */
    public SmileCLARANSClusterer(int k) {
        super(k);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.clustering.clusterers.AbstractSmileClusterer#cluster()
     */
    @Override
    protected void cluster() {
        CLARANS<double[]> clusterer = new CLARANS<>(matrix, new EuclideanDistance(), k, corpus.size() / 2);
        clusterIds = clusterer.getClusterLabel();
        centroids = clusterer.medoids();
    }
}
