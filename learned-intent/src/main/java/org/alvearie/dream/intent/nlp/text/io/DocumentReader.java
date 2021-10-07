/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.io;

import java.io.IOException;
import java.util.List;

import org.alvearie.dream.intent.nlp.text.Document;

/**
 * A {@link DocumentReader} is used to parse files and load {@link Document}s from them.
 *
 */
public interface DocumentReader {

    /**
     * Reads the document source and creates a list of {@link Document}s from it.
     *
     * @return the list of documents or an empty list if none were read
     * @throws IOException if there is a problem creating the {@link Document} list
     */
    public List<Document> read() throws IOException;
}
