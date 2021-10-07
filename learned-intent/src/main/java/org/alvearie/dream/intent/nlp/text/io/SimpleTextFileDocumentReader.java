/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.alvearie.dream.intent.nlp.text.Document;

/**
 * Reads {@link Document}s from a text file where each line contains the text for a {@link Document}.
 *
 */
public class SimpleTextFileDocumentReader implements DocumentReader {

    private File source;
    private Charset charset;

    /**
     * Create a {@link SimpleTextFileDocumentReader} that will read the given source file using UTF-8.
     *
     * @param source the source file
     */
    public SimpleTextFileDocumentReader(File source) {
        this(source, StandardCharsets.UTF_8);
    }

    /**
     * Create a {@link SimpleTextFileDocumentReader} that will read the given source file.
     *
     * @param source the source file
     * @param charset the char set of the source file
     */
    public SimpleTextFileDocumentReader(File source, Charset charset) {
        this.source = source;
        this.charset = charset;
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.io.DocumentReader#read()
     */
    @Override
    public List<Document> read() throws IOException {
        if (!source.exists()) {
            throw new FileNotFoundException("The source document does not exists.");
        }
        List<Document> documents = Files.lines(source.toPath(), charset)
                .filter(line -> !line.trim().isEmpty())			// Ignore empty lines
                .filter(line -> !line.trim().startsWith("#"))	// Ignore # Comments
                .map(line -> new Document(line))
                .collect(Collectors.toList());
        return documents;
    }

}
