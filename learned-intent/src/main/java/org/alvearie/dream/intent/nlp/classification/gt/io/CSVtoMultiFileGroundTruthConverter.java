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
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.alvearie.dream.intent.nlp.text.Document;

import java.util.Set;

/**
 * The {@link CSVtoMultiFileGroundTruthConverter} splits the CSV GT format, into the multi-file format.
 * 
 */
public class CSVtoMultiFileGroundTruthConverter implements GroundTruthWriter {

    private static final String CLASSIFICATION_FILE_EXTENSION = ".txt";

    private File csvGroundTruth;

    /**
     * Creates a splitter that will split the given CSV GT file
     *
     * @param csvGroundTruth the CSV GT file to convert
     */
    public CSVtoMultiFileGroundTruthConverter(File csvGroundTruth) {
        this.csvGroundTruth = csvGroundTruth;
    }

    /*
     * (non-Javadoc)
     * @see org.alvearie.nlp.classification.gt.io.GroundTruthWriter#write(java.io.File)
     */
    @Override
    public File write(File destinationDirectory) throws IOException {
        if (!csvGroundTruth.exists()) {
            throw new FileNotFoundException("The CSV GT files does not exist: " + csvGroundTruth);
        }
        if (destinationDirectory.exists() && !destinationDirectory.isDirectory()) {
            throw new FileNotFoundException("The destiation directory is an existing file: " + destinationDirectory);
        }
        // We quality the GT directory with a timestamp because it could be easy to overwrite an existing working GT directory
        File gtHome = new File(destinationDirectory, DESTINATION_HOME_DIRECTORY_PREFIX + "-" + TIMESTAMP_FORMATTER.format(LocalDateTime.now()));
        if (!gtHome.exists()) {
            gtHome.mkdirs();
            System.out.println("GT home directory did not exist and was created: " + gtHome);
        }
        CSVGroundTruthReader reader = new CSVGroundTruthReader(csvGroundTruth);
        Map<String, Collection<Document>> gt = reader.read();
        Set<Entry<String, Collection<Document>>> gtEntries = gt.entrySet();
        int total = 0;
        for (Entry<String, Collection<Document>> gtEntry : gtEntries) {
            String classification = gtEntry.getKey();
            Collection<Document> documents = gtEntry.getValue();
            Collection<String> documentsText = Document.toStringCollection(documents);
            File classificationFile = new File(gtHome, classification + CLASSIFICATION_FILE_EXTENSION);
            Files.write(classificationFile.toPath(), documentsText);
            total += documents.size();
        }
        System.out.println("Created " + gt.values().size() + " GT files with a total of " + total + " documents.");
        return gtHome;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: CSVGroundTruthSplitter <csv-gt-file> <multi-file-destination-directory>");
            return;
        }
        CSVtoMultiFileGroundTruthConverter splitter = new CSVtoMultiFileGroundTruthConverter(new File(args[0]));
        splitter.write(new File(args[1]));
    }
}
