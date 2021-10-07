/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text;

import java.util.ArrayList;

/**
 * An ordered representation of all of the features used by a particular process (clustering/classification).
 *
 */
public class FeatureSpace extends ArrayList<Feature> {

    private static final long serialVersionUID = -6808897882139184455L;

    /*
     * Override default implementation to prevent excessive processing and impacts to debugging interactively.
     *
     * (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        if (!Configuration.getDefault().isTraceEnabled()) {
            return this.size() + " features";
        }
        return toVerboseString();
    }

    /**
     * Generate the traditional toString method, containing each entry in the feature space. This may be excessively long.
     *
     * @return list of all features in feature space
     */
    public String toVerboseString() {
        return super.toString();
    }
}
