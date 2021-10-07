/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alvearie.dream.intent.nlp.text.Document;

import java.util.Set;

/**
 * Writes a {@link Map} of {@link Document}s to categories as a multi-file GT format.
 * 
 */
public class MultiFileGroundTruthWriter implements GroundTruthWriter {

    private static final String CLASSIFICATION_FILE_EXTENSION = ".txt";

    private Map<String, Collection<Document>> gt;

    /**
     * Creates a {@link MultiFileGroundTruthWriter} that will write the given {@link Map}
     *
     * @param gt the GT map
     */
    public MultiFileGroundTruthWriter(Map<String, Collection<Document>> gt) {
        this.gt = gt;
	}

    /**
     * Writes the GT {@link Map} given at construction time, such that a text file is created for each category and the
     * documents belonging to that category are entered as lines in that text file.
     * <p>
     * After this method runs there will be a new unique directory prefixed with {@link #DESTINATION_HOME_DIRECTORY_PREFIX}
     * which will contain the various GT text files. A new directory is created every time to avoid accidentally deleting a
     * previously laid out GT directory that may already have been updated.
     *
     * @param destinationDirectory the destination directory for the multi-GT file
     * @return the new multi-file GT base directory created from the given GT
     * @throws IOException if the CSV GT file does not exist, the destination is an existing file, or there is an IO error
     *         creating the destination directory or files
     */
    @Override
    public File write(File destinationDirectory) throws IOException {
        File gtHome = new File(destinationDirectory, DESTINATION_HOME_DIRECTORY_PREFIX + "-" + TIMESTAMP_FORMATTER.format(LocalDateTime.now()));
        if (!gtHome.exists()) {
            gtHome.mkdirs();
            System.out.println("GT home directory did not exist and was created: " + gtHome);
        }
        Set<Entry<String, Collection<Document>>> gtEntries = gt.entrySet();
        int total = 0;
        for (Entry<String, Collection<Document>> gtEntry : gtEntries) {
            String classification = gtEntry.getKey();
            Collection<Document> documents = gtEntry.getValue();
            List<String> documentsText = new ArrayList<>(new HashSet<>(Document.toStringCollection(documents)));
            Collections.sort(documentsText);
            File classificationFile = new File(gtHome, classification + CLASSIFICATION_FILE_EXTENSION);
            Files.write(classificationFile.toPath(), documentsText);
            total += documents.size();
        }
        System.out.println("Created " + gt.values().size() + " GT files with a total of " + total + " documents.");
        return gtHome;
    }
}
