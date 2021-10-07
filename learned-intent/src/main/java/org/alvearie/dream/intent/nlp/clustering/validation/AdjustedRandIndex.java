/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.clustering.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.text.Document;

/**
 * The Adjusted Rand Index is an external evaluation measure, that is it does require Ground Truth.
 * <p>
 * The Adjusted Rand Index is a function that measures the similarity between two groups of {@link Cluster}s namely an
 * expected and an actual list of clusters, ignoring permutations and with chance normalization. In other words, this
 * function compares sets of clusters regardless of how those clusters are ordered, that is what is Cluster 1 in one set
 * can be Cluster 999 in the other set, however it only evaluates how likely those two clusters are based on the
 * clusters' contents.
 * <p>
 * More information about the ARI measure can be found
 * <a href= "https://scikit-learn.org/stable/modules/clustering.html#adjusted-rand-index">here</a>.
 *
 */
public class AdjustedRandIndex {

    private smile.validation.AdjustedRandIndex ari;

    /**
     * Creates a new {@link AdjustedRandIndex} class.
     */
    public AdjustedRandIndex() {
        ari = new smile.validation.AdjustedRandIndex();
    }

    /**
     * Calculates the {@link AdjustedRandIndex} for the given expected and actual {@link Cluster}s.
     *
     * @param expectedClusters the cluster ground truth
     * @param actualClusters the actual clusters from a clustering algorithm
     * @return the ARI score between [-1, 1] where 1 is a perfect match
     */
    public double calculate(List<Cluster> expectedClusters, List<Cluster> actualClusters) {
        Map<Document, String> expectedDocumentToClusterIds = buildDocumentToClusterIDMap(expectedClusters);
        Map<Document, String> actualDocumentToClusterIds = buildDocumentToClusterIDMap(actualClusters);
        int[] expected = new int[expectedDocumentToClusterIds.size()];
        int[] actual = new int[actualDocumentToClusterIds.size()];
        Set<Document> documents = expectedDocumentToClusterIds.keySet();
        int i = 0;
        for (Document document : documents) {
            String expectedClusterId = expectedDocumentToClusterIds.get(document);
            String actualClusterId = actualDocumentToClusterIds.get(document);
            expected[i] = expectedClusterId.hashCode();
            actual[i] = actualClusterId.hashCode();
            i++;
        }
        return ari.measure(expected, actual);
    }

    /**
     * Builds a {@link Map} from {@link Document}s to the ID of its corresponding {@link Cluster}.
     *
     * @param clusters a set of clusters to index by document
     * @return
     */
    private Map<Document, String> buildDocumentToClusterIDMap(List<Cluster> clusters) {
        Map<Document, String> documentToClusterIds = new HashMap<>();
        for (Cluster cluster : clusters) {
            List<Document> documents = cluster.getDocuments();
            for (Document document : documents) {
                documentToClusterIds.put(document, cluster.getId());
            }
        }
        return documentToClusterIds;
    }
}
