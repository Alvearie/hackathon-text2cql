/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alvearie.dream.intent.nlp.text.Document;

import java.util.Set;
import java.util.TreeMap;

/**
 * The {@link MultiFileGroundTruthJoiner} joins the multi-file GT format into the CSV GT file.
 * 
 */

public class MultiFileGroundTruthJoiner implements GroundTruthWriter {

    private static final String DESTINATION_FILE_NAME = "groundTruth.csv";

    private File multiFileGTHomeDirectory;

    /**
     * Creates a {@link MultiFileGroundTruthJoiner} which will write a CSV GT file from the given multi-file home directory.
     *
     * @param multiFileGTHomeDirectory the multi-file GT home directory to convert
     * @throws FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException if the given file is not a directory
     */
    public MultiFileGroundTruthJoiner(File multiFileGTHomeDirectory) throws FileNotFoundException {
        if (!multiFileGTHomeDirectory.exists()) {
            throw new FileNotFoundException("The multi-file GT home directory does not exist: " + multiFileGTHomeDirectory);
        }
        if (!multiFileGTHomeDirectory.isDirectory()) {
            throw new IllegalArgumentException("The given file is not a directory: " + multiFileGTHomeDirectory);
        }
        this.multiFileGTHomeDirectory = multiFileGTHomeDirectory;
    }

    /*
     * (non-Javadoc)
     * @see org.alvearie.nlp.classification.gt.io.GroundTruthWriter#write(java.io.File)
     */
    @Override
    public File write(File destinationDirectory) throws IOException {
        if (destinationDirectory.exists() && !destinationDirectory.isDirectory()) {
            throw new IllegalArgumentException("The destination location is an existing file: " + destinationDirectory);
        }
        destinationDirectory = new File(destinationDirectory, DESTINATION_HOME_DIRECTORY_PREFIX + "-" + TIMESTAMP_FORMATTER.format(LocalDateTime.now()));
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }
        File destinationFile = new File(destinationDirectory, DESTINATION_FILE_NAME);
        GroundTruthReader reader = new MultiFileGroundTruthReader(this.multiFileGTHomeDirectory);
        Map<String, Collection<Document>> gt = reader.read();
        // Sort GT by classification
        gt = new TreeMap<>(gt);
        List<String> csvFileContents = new ArrayList<>();
        csvFileContents.add(String.join(",", CSVGroundTruthReader.CSV_FILE_HEADER));
        Set<Entry<String, Collection<Document>>> entries = gt.entrySet();
        for (Entry<String, Collection<Document>> entry : entries) {
            String classification = entry.getKey();
            Collection<Document> documents = entry.getValue();
            List<String> documentsText = new ArrayList<>(Document.toStringCollection(documents));
            // Sort documents alphabetically
            Collections.sort(documentsText);
            for (String documentText : documentsText) {
                String quotedText = quote(documentText);
                csvFileContents.add(quotedText + "," + classification);
            }
        }
        Files.write(destinationFile.toPath(), csvFileContents);
        System.out.println("Created GT CSV file with a total of " + (csvFileContents.size() - 1) + " documents: " + destinationFile);
        return destinationFile;
    }

    /**
     * Quotes the given text if needed, that is it the text contains commas.
     *
     * @param documentText the text to quote
     * @return the quoted text
     */
    private String quote(String documentText) {
        if (!documentText.contains(",")) {
            return documentText;
        }
        // If we are going to quote the text, we also need to escape any possible existing quotes
        return "\"" + documentText.replaceAll("\"", "\\\"") + "\"";
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: " + MultiFileGroundTruthJoiner.class.getSimpleName() + " <multi-file-GT-home-directory> <csv-GT-home-directory>");
            System.exit(1);
        }
        File gtHomeDirectory = new File(args[0]);
        GroundTruthWriter joiner = new MultiFileGroundTruthJoiner(gtHomeDirectory);
        File csv = new File(args[1]);
        joiner.write(csv);
    }
}
