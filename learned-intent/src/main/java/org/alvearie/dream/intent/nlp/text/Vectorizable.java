/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import java.util.List;

/**
 * A {@link Vectorizable} is an object that can be turned into a {@link FeatureVector}, in order for something to be
 * {@link Vectorizable} it should be breakable into individual {@link Feature}s.
 *
 */
public interface Vectorizable {

    /**
     * @return the {@link Feature} space for this {@link Vectorizable} object
     */
    public List<Feature> getFeatureSpace();

}
