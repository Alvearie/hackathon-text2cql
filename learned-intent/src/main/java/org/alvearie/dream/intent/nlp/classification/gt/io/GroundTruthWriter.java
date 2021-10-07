/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt.io;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Writes GT files to a directory or file depending on the format of the GT provided by the specific writer
 * implementation.
 * 
 */
public interface GroundTruthWriter {

    /**
     * The prefix of the base directory where the GT will be laid out.
     */
    public static final String DESTINATION_HOME_DIRECTORY_PREFIX = "GroundTruth";

    /**
     * The format of the time stamp that the GT home directory will be suffixed with.
     */
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");

    /**
     * Writes a GT representation to the given destination directory.
     * <p>
     * After this method runs there will be a new unique directory prefixed with {@link #DESTINATION_HOME_DIRECTORY_PREFIX}
     * which will contain the GT file(s). A new directory is created every time to avoid accidentally deleting a previously
     * laid out GT directory that may already have been updated.
     *
     * @param destinationDirectory the destination directory for the GT to write to
     * @return the new GT base directory or file created from the GT given at construction or configuration time
     * @throws IOException if the destination is an existing file, or there is an IO error creating the destination
     *         directory or files
     */
    public File write(File destinationDirectory) throws IOException;

}
