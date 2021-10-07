/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.lemmatization;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;


public class FileUtils {
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

    /**
     * @param propertiesFile a file of key=value pairs defining the location/name of the PEAR file
     *                       and which location it should be extracted to
     * @return A {@link Properties} object containing the properties from the properties file
     */
    static Properties loadProperties(String propertiesFile) {

        Properties properties = new Properties();
        try (InputStream inputStream
                     = FileUtils.class.getClassLoader().getResourceAsStream(propertiesFile)) {

            if (inputStream == null) {
                LOGGER.error(String.format("Unable to find properties file < %s >", propertiesFile));
            } else {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to load properties from properties file", e);
        }

        return properties;
    }

    /**
     * @param properties a {@link Properties} object
     * @return A {@link HashMap} containing {@link File} objects represtenting the PEAR file and containing directory, respectively.
     * @throws URISyntaxException if the {@link File} could not be that a string could not be parsed as a {@link java.net.URI} reference
     */
    static Map<String, File> getPearDirAndFile(Properties properties) throws URISyntaxException {

        Map<String, File> fileMap = new HashMap<>();

        fileMap.put("pearfile", getFileFromClasspath(properties.getProperty("pearfile")));
        fileMap.put("peardir", getFileFromClasspath(properties.getProperty("peardir")));

        return fileMap;
    }

    /**
     * @param properties a {@link Properties} object
     * @return A {@link File} object representing the PEAR descriptor
     * @throws URISyntaxException if the {@link File} could not be that a string could not be parsed as a {@link java.net.URI} reference
     */
    static File getPearDescriptor(Properties properties) throws URISyntaxException {

        return getFileFromClasspath(properties.getProperty("peardescriptor"));
    }

    private static File getFileFromClasspath(String path) throws URISyntaxException {
        return new File(Objects.requireNonNull(FileUtils.class.getClassLoader().getResource(path)).toURI());
    }
}
