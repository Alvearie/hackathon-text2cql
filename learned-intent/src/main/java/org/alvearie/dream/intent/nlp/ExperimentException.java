/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp;

/**
 * An exception class that will encapsulate errors in the Experiment framework. This allows most uncommon errors to be
 * converted to RuntimeExceptions and not overtly impact the callers by requiring exception handling for every call.
 *
 */
public class ExperimentException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 6742602528320479790L;

    /**
     * Create an ExperimentException
     */
    public ExperimentException() {
        super();
    }

    /**
     * Create an ExperimentException for the given throwable
     *
     * @param t
     */
    public ExperimentException(Throwable t) {
        super(t);
    }

    /**
     * Create an ExperimentException for the given message and throwable
     *
     * @param message
     * @param t
     */
    public ExperimentException(String message, Throwable t) {
        super(message, t);
    }
}
