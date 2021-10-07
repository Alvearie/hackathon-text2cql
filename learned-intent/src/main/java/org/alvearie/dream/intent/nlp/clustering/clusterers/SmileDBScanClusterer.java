/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering.clusterers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alvearie.dream.intent.nlp.clustering.Clusterer;
import org.alvearie.dream.intent.nlp.text.Document;

import smile.clustering.Clustering;
import smile.clustering.DBSCAN;
import smile.neighbor.KDTree;

/**
 * A {@link Clusterer} implemented using Smile's {@link DBSCAN} clusterization.
 *
 */
public class SmileDBScanClusterer extends AbstractSmileClusterer {

    /* (non-Javadoc)
     * @see org.alvearie.nlp.clustering.clusterers.AbstractSmileClusterer#cluster()
     */
    @Override
    protected void cluster() {
        DBSCAN<double[]> dbScans = new DBSCAN<>(matrix, new KDTree<>(matrix, matrix), 1, 0.7);
//      DBScan<double[]> dbScans = new DBScan<>(matrix, new EuclideanDistance(), 1, 0.7);
        k = dbScans.getNumClusters();
        clusterIds = dbScans.getClusterLabel();
    }

    /**
     * We need to override the default implementation of this method because this clusterer produces an "Outliers" cluster
     * which other algorithms don't, so we account for that here.
     *
     * @return the cluster map
     */
    @Override
    protected Map<String, List<Document>> initializeClusterMap() {
        Map<String, List<Document>> clusterMap = new LinkedHashMap<>();
        for (int i = 0; i < k; i++) {
            clusterMap.put(String.valueOf(i), new ArrayList<>());
        }
        clusterMap.put(String.valueOf(Clustering.OUTLIER), new ArrayList<>());
        return clusterMap;
    }

}
