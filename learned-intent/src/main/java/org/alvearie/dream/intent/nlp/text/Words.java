/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * An {@link Set} of words.
 *
 */
public class Words extends HashSet<String> {

    private static final long serialVersionUID = 7814628088429974831L;

    /**
     * Creates an empty {@link Words} object from the given collection of {@link String}s. This object can later be
     * modified.
     */
    public Words() {
    }

    /**
     * Creates a new {@link Words} object from the given collection of {@link String}s.
     *
     * @param words the words
     */
    public Words(Collection<String> words) {
        super(words);
    }

    /**
     * Creates a new {@link Words} object from the given array of {@link String}s.
     *
     * @param words the words
     */
    public Words(String... words) {
        this(Arrays.asList(words));
    }

    /**
     * Load Words from an external file
     *
     * @param f
     * @throws IOException
     */
    public Words(File f) throws IOException {
        for (String fileLine : Files.readAllLines(f.toPath())) {
            if (fileLine == null) {
                continue;
            }
            fileLine = fileLine.trim();
            if (!fileLine.isEmpty() && !fileLine.startsWith("#")) {
                add(fileLine);
            }
        }
    }
}
