/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text;

/**
 * A marker interface that indicates that something is a text analytics feature.
 * <p>
 * A feature is an individual measurable property of characteristic of a document, used to create representations of
 * that document in a vector space model.
 * <p>
 * It is important to make all {@link Feature} implementations explicitly differentiatable from other {@link Feature}s
 * and that is why this interface requires implementors to implement the {@link #equals(Object)} and {@link #hashCode()}
 * methods.
 *
 */
public interface Feature {

    /**
     * Gets the feature name for this feature
     *
     * @return the feature name
     */
    public String getFeature();

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    boolean equals(Object o);

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    int hashCode();
}
