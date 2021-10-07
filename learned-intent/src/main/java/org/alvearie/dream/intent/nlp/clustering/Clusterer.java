/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering;

import java.util.List;

import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;

/**
 * A {@link Clusterer} is an NLP clusterer for {@link Document}s in a {@link Corpus}.
 *
 */
public interface Clusterer {

    /**
     * Cluster the given {@link Document}s.
     *
     * @param documents the corpus
     * @return the list of {@link Cluster}s
     */
    public List<Cluster> cluster(Corpus documents);
}
