/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering.clusterers;

import org.alvearie.dream.intent.nlp.clustering.Clusterer;

import smile.clustering.SIB;

/**
 * A {@link Clusterer} implemented using Smile's {@link SIB} clusterization.
 *
 */
public class SmileSIBClusterer extends AbstractSmileClusterer {

    /**
     * Creates a {@link SmileSIBClusterer} with the given K.
     *
     * @param k the number or clusters
     */
    public SmileSIBClusterer(int k) {
        super(k);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.clustering.clusterers.AbstractSmileClusterer#cluster()
     */
    @Override
    protected void cluster() {
        SIB clusterer = new SIB(matrix, k);
        clusterIds = clusterer.getClusterLabel();
        centroids = clusterer.centroids();
    }
}
