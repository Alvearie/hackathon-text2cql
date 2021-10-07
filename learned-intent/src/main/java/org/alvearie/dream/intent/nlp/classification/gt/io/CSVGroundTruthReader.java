/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Document;
import org.apache.log4j.Logger;

/**
 * Reads GT files from a csv.
 * 
 */
public class CSVGroundTruthReader implements GroundTruthReader {

    /**
     * The header indicating the fields in the CSV file.
     */
    public static final String[] CSV_FILE_HEADER = new String[] { "Criterion", "Intent" };

    private static final Logger LOGGER = Logger
            .getLogger(CSVGroundTruthReader.class.getName());

    private InputStream inputStream;

    /**
     * @param csvFile the csv file to load
     * @throws FileNotFoundException if the given file does not exist
     */
    public CSVGroundTruthReader(File csvFile) throws FileNotFoundException {
        if (!csvFile.exists()) {
            throw new FileNotFoundException("The GT file does not exist: " + csvFile);
        }
        this.inputStream = new FileInputStream(csvFile);
    }

    /**
     * Load CSV Ground truth via inputStream. This is primarily required so ground truth located in another project can be
     * loaded by resource, instead of file location.
     *
     * @param inputStream
     */
    public CSVGroundTruthReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Reads the GT files in the GT directory provided at construction time. The ground truth is expected to be in the
     * format: Criterion,Intent. If the Criterion text contains commas, it should be encapsulated in double-quotes.
     *
     * @return the GT
     * @throws IOException - if the given file does not exist or is not a directory, or if an I/O error occurs reading from
     *         the file or a malformed or unmappable byte sequence is read
     * @throws IllegalArgumentException - if data in the file is found to not include the proper number of columns for a
     *         data row, this will be thrown.
     */
    @Override
    public Map<String, Collection<Document>> read() throws IOException {
        Map<String, Collection<Document>> gt = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("#")) {
                    System.err.println("Ignoring: " + line);
                    continue;
                }

                String columns[] = splitCSVLine(line);
                // Ignore header row
                if (columns.length < 2) {
                    throw new IllegalArgumentException("Ground truth has malformed data. Can't find 2 columns: " + line);
                }
                if (columns[0].trim().equalsIgnoreCase(CSV_FILE_HEADER[0]) && columns[1].trim().equalsIgnoreCase(CSV_FILE_HEADER[1])) {
                        continue;
                }

                String text = columns[0];
                String intent =  columns[1];

                if (intent == null) {
                    LOGGER.error("Entry will be skipped because it is missing the intent:" + line);
                    continue;
                }
                if (text == null) {
                    LOGGER.error("Entry will be skipped because it is missing the text:" + line);
                    continue;
                }

                Collection<Document> gtForCategory = gt.computeIfAbsent(intent.trim(), i -> new ArrayList<Document>());
                int maxTrainingDataPerClassSize = Configuration.getDefault().getMaxTrainingDataPerClassSize();
                // Limit the number of training cases per class. This can help quickly evaluate model changes by providing a smaller
                // train/test set.
                if (maxTrainingDataPerClassSize > 0 && maxTrainingDataPerClassSize <= gtForCategory.size()) {
                    continue;
                }
                text = text.trim();
                if (text.startsWith("\"") && text.endsWith("\"")) {
                    text = text.substring(1, text.length() - 1);
                }
                gtForCategory.add(new Document(text.trim()));
            }
        }
        return gt;
    }

    /**
     * Split a single line into comma-delimited entries, respecting double-quotes as inclusive boundaries
     *
     * @param line
     * @return an array with the various elements of the CSV line
     */
    public static final String[] splitCSVLine(String line) {
        if (line == null || line.trim().length() == 0) {
            return new String[0];
        }
        String otherThanQuote = " [^\"] ";
        String quotedString = String.format(" \" %s* \" ", otherThanQuote);
        // split on comma, but keep quoted text together
        String regex = String.format("(?x) " + // enable comments, ignore white spaces
                ",                         " + // match a comma
                "(?=                       " + // start positive look ahead
                "  (?:                     " + // start non-capturing group 1
                "    %s*                   " + // match 'otherThanQuote' zero or more times
                "    %s                    " + // match 'quotedString'
                "  )*                      " + // end group 1 and repeat it zero or more times
                "  %s*                     " + // match 'otherThanQuote'
                "  $                       " + // match the end of the string
                ")                         ", // stop positive look ahead
                otherThanQuote, quotedString, otherThanQuote);

        return line.split(regex, -1);
    }
}
