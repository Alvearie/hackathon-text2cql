/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.classification.gt.io;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.alvearie.dream.intent.nlp.text.Document;

/**
 * Reads GT files from a directory.
 *

 */
public interface GroundTruthReader {

    /**
     * Reads the GT files in the GT directory provided at construction time.
     *
     * @return the GT
     * @throws IOException - if the given file does not exist or is not a directory, or if an I/O error occurs reading from
     *         the file or a malformed or unmappable byte sequence is read
     */
    public Map<String, Collection<Document>> read() throws IOException;
}
