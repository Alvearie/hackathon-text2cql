/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alvearie.dream.intent.nlp.text.Document;

/**
 * Reads GT files from a directory.
 * 
 *
 */
public class MultiFileGroundTruthReader implements GroundTruthReader {

	private File directory;

	/**
	 * @param directory
	 *            the directory with the GT files
	 */
	public MultiFileGroundTruthReader(File directory) {
		this.directory = directory;
	}

	/**
	 * Reads the GT files in the GT directory provided at construction time.
	 *
	 * @return the GT
	 * @throws IOException
	 *             - if the given file does not exist or is not a directory, or if an I/O error occurs reading from the
	 *             file or a malformed or unmappable byte sequence is read
	 */
	@Override
	public Map<String, Collection<Document>> read() throws IOException {
		if (!directory.exists()) {
			throw new IOException("The GT directory does not exist: " + directory);
		}
		if (!directory.isDirectory()) {
			throw new IOException("The GT files was not not a directory: " + directory);
		}
		File[] gtFiles = directory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".txt");
			}
		});
		Map<String, Collection<Document>> gt = new HashMap<>();
		for (File gtFile : gtFiles) {
			List<String> lines = Files.readAllLines(Paths.get(gtFile.toURI()), StandardCharsets.UTF_8);
			String classification = gtFile.getName().split(Pattern.quote("."))[0];
            Set<Document> documents = new LinkedHashSet<>();
			for (String line : lines) {
				if (line.trim().isEmpty()) {
					continue;
				}
				if (line.startsWith("#")) {
					continue;
				}
				documents.add(new Document(line.trim()));
			}
			gt.put(classification, documents);
		}
		return gt;
	}
}
