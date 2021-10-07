/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.log4j.Logger;

/*
 * This class is a utility to get an input stream for a resource.  Resources may reside in the classpath, in the workspace (relative path), or anywhere on the file system. This utility will follow the following rules:
 * 1) If a config set value exists, use it before using the default path
 * 2) For a given path, try to find it in the classpath first.  If found, use it.
 * 3) If not found in the classpath, assume path is a file system path. *
 */

public class FileResource {
	private static Logger logger = Logger.getLogger(FileResource.class.getName());

	public static InputStream getFileInputStream(String fileLocation) {
		return getFileInputStream(fileLocation, (ClassLoader) null);
	}

	public static final InputStream getFileInputStream(String fileLocation, ClassLoader classLoader) {
		Path path = getFilePath(fileLocation, classLoader);
		if (path == null) {
			return null;
		}
		try {
			return Files.newInputStream(path);
		} catch (IOException e) {
			logger.warn("Problem loading file: " + fileLocation);
		}
		return null;
	}

	/**
	 * Given a file or directory name this method tries to resolve its {@link Path} by looking in the file system first, and if
	 * the file is not there, it tries to resolve it as a file resource from the classpath.
	 *
	 * @param fileLocation
	 *            the file location
	 * @return the Path constructed from the given string or null if the file can't be resolved
	 */
	public static Path getFilePath(String fileLocation) {
		return getFilePath(fileLocation, (ClassLoader) null);
	}

	public static final Path getFilePath(String fileLocation, ClassLoader classLoader) {
		if (fileLocation == null) {
			return null;
		}
		// We might be trying to load a file from the file system or a file resource from the classpath;
		// if the file does not exist in the file system, we assume it is in the classpath
		Path configFile = Paths.get(fileLocation);
		if (Files.exists(configFile)) {
			logger.debug("Found file '" + fileLocation + "' resolved as '" + configFile.toAbsolutePath() + "' from the file system...");
			return configFile;
		}

		URL fileURL = classLoader == null ? FileResource.class.getResource(fileLocation) : classLoader.getResource(fileLocation);
		if (fileURL == null) {
			// If we didn't find the file through the class loader, search using the class object itself
			// since this works a bit differently
			fileURL = Thread.currentThread().getContextClassLoader().getResource(fileLocation);
		}

		// URL fileURL = FileResource.class.getResource(fileLocation);
		if (fileURL == null) {
			logger.debug("Could not resolve file '" + fileLocation + "' in the file system or the classpath.");
			return null;
		}
		logger.debug("Found file with URL: " + fileURL);
		String source = "the classpath";
		// WAS workaround: In WAS the file URL for a file inside a JAR uses protocol"wsjar" but that protocol
		// is not supported by the runtime, so we change it to the standard "jar" protocol.
		if (fileURL.getProtocol().startsWith("wsjar")) {
			logger.debug("Runtime is using unsupported protocol 'wsjar' so converting...");
			try {
				fileURL = new URL("jar", fileURL.getHost(), fileURL.getPort(), fileURL.getPath());
				logger.debug("New file URL: " + fileURL);
			} catch (MalformedURLException e) {
				logger.warn("Error converting URL protocol from 'wsjar' to 'jar': " + e.getMessage());
				logger.debug("Could not resolve file '" + fileLocation + "' from the classpath.");
				return null;
			}
		}
		try {
			configFile = Paths.get(URI.create(fileURL.toString()));
		} catch (FileSystemNotFoundException e) {
			// We could not load the file from the classpath using the default file system, that means the file must be in
			// a JAR in the classpath (instead of it being a regular file in the file system).
			// So let's build a FileSystem for the JAR and resolve the file
			String[] jarAndFileURIs = fileURL.toString().split("!");
			synchronized (FileResource.class) {
				// synchronize across FileResource to make sure that only one thread tries to create a "newFileSystem" for a
				// specific jar
				try {
					configFile = Paths.get(URI.create(fileURL.toString()));
				} catch (Exception ignoreException) {
					try {
						FileSystem jar = FileSystems.newFileSystem(URI.create(jarAndFileURIs[0]), new HashMap<String, Object>());
						configFile = jar.getPath(jarAndFileURIs[1]);
						source = "JAR '" + jar + "' in the classpath";
					} catch (IOException ioe) {
						logger.warn("Error reading JAR to resolve file: " + fileURL.toString() + ": " + ioe.getMessage());
						logger.debug("Could not resolve file '" + fileLocation + "' from the classpath.");
						return null;
					}
				}
			}
		}
		logger.debug("Reading file '" + fileLocation + "' resolved as '" + configFile.toAbsolutePath() + "' from " + source + "...");
		return configFile;
	}
}
