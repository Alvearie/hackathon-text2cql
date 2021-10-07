/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.classification;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * This manager will help create a classifier from the specified input source
 *
 */
public class ClassifierManager {

    /**
     * Load a stored classifier from the given file
     *
     * @param file
     * @return the loaded Classifier
     * @throws IOException if an IO error occurs loading the file, e.g. if the file does not exist
     */
    public static final Classifier load(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return load(inputStream);
        }
    }

    /**
     * Load a stored classifier from the given input stream
     *
     * @param inputStream
     * @return the loaded Classifier
     * @throws IOException
     */
    public static Classifier load(InputStream inputStream) throws IOException {
        try (ObjectInputStream is = new ObjectInputStream(inputStream)) {
            return (Classifier) is.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("The classifier class for the object being loaded cannot be found.", e);
        }
    }
}
