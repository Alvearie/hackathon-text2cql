/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * A collection of utilities for NLP text analytics
 *
 */
public class Utils {

    /**
     * This will create a stream, parallel or serial, from the given collection based on the current Configuration setting.
     *
     * @param collection
     * @return stream for given collection
     */
    public static <T> Stream<T> stream(Collection<T> collection) {
        if (Configuration.getDefault().isSerialMode()) {
            return collection.stream();
        }
        return collection.parallelStream();
    }
}
