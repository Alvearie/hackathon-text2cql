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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alvearie.dream.intent.nlp.text.Document;

/**
 * Reads {@link Document}s from a text file where each line contains the text for a {@link Document} in the following
 * format:
 * <p>
 * <code>documentId - documentText</code>\
 * <p>
 * For example: NCT01552434_CRIT_1208_1236-inclusion - Karnofsky >= 60%<br>
 * NCT01552434_CRIT_1727_1799-inclusion - Fasting level of total cholesterol of no more than 350 mg/dL<br>
 * NCT01552434_CRIT_1802_1858-inclusion - Triglyceride level of no more than 400 mg/dL<br>
 * NCT01552434_CRIT_2240_2386-inclusion - Patients may not be receiving any other investigational agents and/or any
 * other concurrent anticancer agents or therapies<bR>
 * <p>
 * This is the format that the cluster pipeline logs to the console, and it may be useful to recluster.
 *
 */
public class DocumentClusterReader implements DocumentReader {

    private File source;
    private Charset charset;

    /**
     * Create a {@link DocumentClusterReader} that will read the given source file using UTF-8.
     *
     * @param source the source file
     */
    public DocumentClusterReader(File source) {
        this(source, StandardCharsets.UTF_8);
    }

    /**
     * Create a {@link DocumentClusterReader} that will read the given source file.
     *
     * @param source the source file
     * @param charset the char set of the source file
     */
    public DocumentClusterReader(File source, Charset charset) {
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
                .map(line -> {
                    String[] idAndText = line.split(Pattern.quote(" - "));
                    return new Document(idAndText[0], idAndText[1]);
                })
                .collect(Collectors.toList());
        return documents;
    }

}
