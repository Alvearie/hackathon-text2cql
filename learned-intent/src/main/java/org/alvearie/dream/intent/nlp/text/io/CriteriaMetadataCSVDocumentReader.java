/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentWithPrediction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Reads {@link Document}s from a CTM CriteriaMetadata CSV file.
 * <p>
 * NOTE: This is a CTM-report specific file and needs to be moved out of this project and into a CTM-specific project.
 *
 */
public class CriteriaMetadataCSVDocumentReader implements DocumentReader {

    private File source;

    /**
     * Create a {@link CriteriaMetadataCSVDocumentReader} that will read the given source file using UTF-8.
     *
     * @param source the source file
     */
    public CriteriaMetadataCSVDocumentReader(File source) {
        this.source = source;
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.io.DocumentReader#read()
     */
    @Override
    public List<Document> read() throws IOException {
        if (!source.exists()) {
            throw new FileNotFoundException("The source document does not exists.");
        }
        Set<Document> documents = new LinkedHashSet<>();
        CSVParser csv = CSVFormat.EXCEL
                .withHeader()
                .parse(new FileReader(source));

        List<CSVRecord> metadataEntries = new ArrayList<>(csv.getRecords());
        System.out.println("Read: " + metadataEntries.size() + " total criteria");
        for (CSVRecord csvEntry : metadataEntries) {
            String criterionID = csvEntry.get("Criterion Id");
            String eligibility = csvEntry.get("Section Type");
            String criterion = csvEntry.get("Criterion Text");
            if (criterion.trim().isEmpty()) {
                continue;
            }
            if (criterionID.contains("WCT")) {
                continue;
            }
            if (criterion.contains("LRSSLL")) {
                continue;
            }
            if (criterion.toLowerCase().startsWith("note:")
                    || criterion.toLowerCase().startsWith("notes:")
                    || criterion.toLowerCase().startsWith("note ")
                    || criterion.toLowerCase().startsWith("(note:")
                    || criterion.toLowerCase().startsWith("*note:")) {
                continue;
            }
            String intent = csvEntry.get("Learned Intent");
            documents.add(new DocumentWithPrediction(criterionID + "-" + eligibility, criterion, intent));
        }

        System.out.println("Read: " + documents.size() + " criteria. Removed notes, LRSSLL and mock criteria");
        return new ArrayList<Document>(documents);
    }

}
