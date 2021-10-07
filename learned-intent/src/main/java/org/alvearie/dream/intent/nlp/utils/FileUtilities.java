/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;


/**
 * Common file utilities
 *
 *
 */
public class FileUtilities {
	private static final Logger LOGGER = Logger.getLogger(FileUtilities.class.getName());

	/**
	 * Copy the input file to the output directory. The input paths may be path relative to the classpath.
	 *
	 * @param src
	 * @param targetDir
	 * @throws Exception
	 */
	public static void copyFileWithPathOrClasspath(String src, String targetDir) throws Exception {
		copyFileWithPathOrClasspath(src, targetDir, null);
	}

	/**
	 * Copy the input file to the output directory. The input paths may be path relative to the classpath. Optionally providing a new name for the targetFile
	 *
	 * @param src
	 * @param targetDirName
	 * @param targetFileName
	 *            - empty string or null assumes same as source file name
	 * @throws Exception
	 */
	public static File copyFileWithPathOrClasspath(String src, String targetDirName, String targetFileName) throws Exception {
		File srcFile = null;
		File tgtDir = null;
		FileWriter tgtFilewriter = null;
		try {
			tgtDir = new File(targetDirName);
			if (null == tgtDir || !tgtDir.isDirectory()) {
				// Try classpath
				URL tgtUrl = FileUtilities.class.getResource(targetDirName);
				if (null != tgtUrl) {
					tgtDir = new File(tgtUrl.toURI());
				} else {
					// If the path doesn't start with a slash, and we still didn't find it, try adding one.
					if (!targetDirName.startsWith("/")) {
						targetDirName = "/" + targetDirName;
					}
					// Try classpath again
					tgtUrl = FileUtilities.class.getResource(targetDirName);
					if (null != tgtUrl) {
						tgtDir = new File(tgtUrl.toURI());
					}
				}
			}

			if (targetFileName == null || targetFileName.isEmpty()) {
				targetFileName = FilenameUtils.getName(src);
			}

			srcFile = getFileWithPathOrClasspath(src);
			if (srcFile != null && srcFile.isFile() && tgtDir != null && tgtDir.isDirectory()) {
				try {
					File tgtFile = new File(tgtDir, targetFileName);
					if (null != tgtFile) {
						FileUtils.copyFile(srcFile, tgtFile);
						return tgtFile;
					}
				} catch (Exception e) {
					LOGGER.debug("Copy error, try via stream next: " + e.getMessage());
				}
			}

			// We didn't find it as a file, trying copying as a stream
			BufferedReader srcRdr = getReaderFromPathOrClasspath(src);
			if (null != srcRdr) {
				File tgtFile = new File(tgtDir, targetFileName);
				if (null != tgtFile) {
					tgtFilewriter = new FileWriter(tgtFile);
					IOUtils.copy(srcRdr, tgtFilewriter);
					tgtFilewriter.close();
					return tgtFile;
				}
			}

			throw new Exception("Could not copy source file as a file or a stream");

		} catch (Exception e) {
			LOGGER.error("Could not copy '" + src + "' to '" + targetDirName + "'", e);
			throw e;
		}
	}

	/**
	 * Calls @see #getFileWithPathOrClasspath(String, boolean) with a the second parameter as false
	 *
	 * @param path
	 * @return File
	 * @throws Exception
	 */
	public static File getFileWithPathOrClasspath(String path) {
		return getFileWithPathOrClasspath(path, false);
	}

	/**
	 * Get a File or Directory from either absolute path, relative path, or classpath. Requires valid existing file. Non-existing File builders should use a relative or absolute path with new File().
	 *
	 * @param path
	 * @param dirsAllowed
	 *            Are we allowed to return a directory?
	 * @return File
	 * @throws Exception
	 */
	public static File getFileWithPathOrClasspath(String path, boolean dirsAllowed) {

		if (LOGGER.isDebugEnabled()) {
			try {
				// Log all occurrences found, in the order found, of this resource on the classpath via different loaders.

				Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
				while (urls.hasMoreElements()) {
					LOGGER.info("Thread.currentThread().getContextClassLoader(): " + urls.nextElement());
				}

				urls = FileUtilities.class.getClassLoader().getResources(path);
				while (urls.hasMoreElements()) {
					LOGGER.info("FileUtilities.class.getClassLoader(): " + urls.nextElement());
				}
			} catch (Exception e) {
			}
		}

		File file = null;
		try {
			file = new File(path);
			if (null != file && file.isFile()) {
				LOGGER.info("Found by File: " + file.getAbsolutePath());
				return file;
			}
		} catch (Exception e) {
			LOGGER.warn("Exception trying to get File", e);
		}

		// Try classpath variations. These will all require existance
		URL srcUrl;

		try {
			srcUrl = Thread.currentThread().getContextClassLoader().getResource(path);
			if (null != srcUrl) {
				file = new File(srcUrl.getFile());
				if (null != file && file.isFile()) {
					LOGGER.info("Found by context CL getResource(): " + file.getAbsolutePath());
					return file;
				} else {
					if (!dirsAllowed) {
						LOGGER.warn("Found by context CL getResource() but not a file: " + file.getAbsolutePath());
					} else {
						return file;
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to get via thread context class loader.", e);
		}

		try {
			srcUrl = FileUtilities.class.getResource(path);
			if (null != srcUrl) {
				file = new File(srcUrl.getFile());
				if (null != file && file.isFile()) {
					LOGGER.info("Found by getResource(): " + file.getAbsolutePath());
					return file;
				} else {
					if (!dirsAllowed) {
						LOGGER.warn("Found by getResource() but not a file: " + file.getAbsolutePath());
					} else {
						return file;
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to get via class loader.", e);
		}

		try {
			// If the path doesn't start with a slash, and we still didn't find it, try adding one.
			if (!path.startsWith("/")) {
				srcUrl = FileUtilities.class.getResource("/" + path);
				if (null != srcUrl) {
					file = new File(srcUrl.getFile());
					if (null != file && file.isFile()) {
						LOGGER.info("Found by getResource(/path): " + file.getAbsolutePath());
						return file;
					} else {
						if (!dirsAllowed) {
							LOGGER.warn("Found by getResource(/path) but not a file: " + file.getAbsolutePath());
						} else {
							return file;
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to get via class loader with leading adjust.", e);
		}

		LOGGER.warn("Unable to get File for " + path);
		return null;
	}

	/**
	 * Open a file for reading, without knowing if the path represents a relative (to classpath) or absolute/relative file path.
	 *
	 * @param path
	 * @return a reader for the given path, or null if one could not be created
	 */
	public static BufferedReader getReaderFromPathOrClasspath(String path) {
		File file = null;
		try {
			file = new File(path);

			if (file != null && file.isFile()) {
				return new BufferedReader(new FileReader(file));
			}
		} catch (Exception e) {
		}

		// Try classpath if didn't return above.
		InputStream stream = FileUtilities.class.getResourceAsStream(path);
		if (null != stream) {
			return new BufferedReader(new InputStreamReader(stream));
		}

		// If the path doesn't start with a slash, and we still didn't find it, try adding one.
		if (!path.startsWith("/")) {
			path = "/" + path;
			stream = FileUtilities.class.getResourceAsStream(path);
			if (null != stream) {
				return new BufferedReader(new InputStreamReader(stream));
			}
		}

		return null;
	}

	/**
	 * Open a file for reading, without knowing if the path represents a relative (to classpath) or absolute/relative file path.
	 *
	 * @param path
	 * @return a reader for the given path, or null if one could not be created
	 */
	public static InputStream getStreamFromPathOrClasspath(String path) {
		InputStream is = FileResource.getFileInputStream(path);
		if (is != null) {
			LOGGER.info("Found " + path + " from FileResource.getFileInputStream");
			return is;
		}

		File file = null;
		try {
			file = new File(path);

			if (file != null && file.isFile()) {
				LOGGER.info("Found " + path + " from path traversal utils");
				return new FileInputStream(file);
			}
		} catch (Exception e) {
		}

		// Try classpath if didn't return above.

		is = FileUtilities.class.getResourceAsStream(path);
		if (null != is) {
			LOGGER.info("Found " + path + " from path traversal utils");
			return is;
		}

		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		if (null != is) {
			LOGGER.info("Found " + path + " from class loader");
			return is;
		}

		// If the path doesn't start with a slash, and we still didn't find it, try adding one.
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		is = FileUtilities.class.getResourceAsStream(path);
		if (null != is) {
			LOGGER.info("Found " + path + " from path traversal utils");
			return is;
		}

		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		if (null != is) {
			LOGGER.info("Found " + path + " from class loader");
			return is;
		}

		return null;
	}

	/**
	 * Get a URL from either absolute path, relative path, or classpath.
	 *
	 * @param path
	 * @return File
	 * @throws Exception
	 */
	public static URL getURL(String path) {

		if (LOGGER.isDebugEnabled()) {
			try {
				// Log all occurrences found, in the order found, of this resource on the classpath via different loaders.

				Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
				while (urls.hasMoreElements()) {
					LOGGER.info("Thread.currentThread().getContextClassLoader(): " + urls.nextElement());
				}

				urls = FileUtilities.class.getClassLoader().getResources(path);
				while (urls.hasMoreElements()) {
					LOGGER.info("FileUtilities.class.getClassLoader(): " + urls.nextElement());
				}
			} catch (Exception e) {
			}
		}

		// Check absolute path first, but only return it if it exists.
		try {
			File file = new File(path);
			if (null != file && file.isFile()) {
				LOGGER.info("Found by File: " + file.getAbsolutePath());
				return new URL("file", null, file.getAbsolutePath());
			}
		} catch (Exception e) {
			LOGGER.warn("Exception trying to get File", e);
		}

		// Try classpath variations. These will all require existance.
		URL srcUrl = null;

		try {
			srcUrl = Thread.currentThread().getContextClassLoader().getResource(path);
			if (null != srcUrl) {
				LOGGER.debug("Found by context CL getResource(): " + srcUrl.getPath());
				return srcUrl;
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to get via thread context class loader.", e);
		}

		if (null == srcUrl) {
			try {
				srcUrl = FileUtilities.class.getResource(path);
				if (null != srcUrl) {
					LOGGER.debug("Found by getResource(): " + srcUrl.getPath());
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to get via class loader.", e);
			}

			if (null == srcUrl) {
				try {
					// If the path doesn't start with a slash, and we still didn't find it, try adding one.
					if (!path.startsWith("/")) {
						srcUrl = FileUtilities.class.getResource("/" + path);
						if (null != srcUrl) {
							LOGGER.debug("Found by getResource(/path): " + srcUrl.getPath());
						}
					}
				} catch (Exception e) {
					LOGGER.warn("Failed to get via class loader with leading adjust.", e);
				}
			}
		}
		// WAS workaround: In WAS the file URL for a file inside a JAR uses protocol"wsjar" but that protocol
		// is not supported by the runtime, so we change it to the standard "jar" protocol.
		if (srcUrl.getProtocol().startsWith("wsjar")) {
			LOGGER.debug("Runtime is using unsupported protocol 'wsjar' so converting...");
			try {
				srcUrl = new URL("jar", srcUrl.getHost(), srcUrl.getPort(), srcUrl.getPath());
				LOGGER.debug("New file URL: " + srcUrl);
			} catch (MalformedURLException e) {
				LOGGER.warn("Error converting URL protocol from 'wsjar' to 'jar': " + e.getMessage());
				return null;
			}
		}

		LOGGER.debug("URL " + srcUrl + " returned for " + path);
		return srcUrl;
	}

	/**
	 * Create dir (and missing preceding parent dirs), with friendly permissions set as done by setFriendlyPermissions() (but only on the base dir being created).
	 *
	 * @param dir
	 *            - create this dir and any preceding components
	 * @return - status of mkdirs(), or true if it already existed.
	 */
	public static boolean mkdirWithFriendlyPermissions(File dir) {
		boolean status = true;
		if (!dir.exists()) {
			mkdirWithFriendlyPermissions(dir.getParentFile());
			status = dir.mkdirs();
			setFriendlyPermissions(dir);
		}
		return status;
	}

	/**
	 * Set file permissions on all files and directories in this input dir tree.
	 *
	 * @param dir
	 * @param filePerms
	 * @param dirPerms
	 */
	public static void recursiveChangePermissions(File dir, Set<PosixFilePermission> filePerms, Set<PosixFilePermission> dirPerms) {
		try {
			Files.setPosixFilePermissions(dir.toPath(), dirPerms);
			File[] files = dir.listFiles();
			if (files == null) {
				return;
			}
			for (File file : files) {
				try {
					if (file.isDirectory()) {
						Files.setPosixFilePermissions(file.toPath(), dirPerms);
						recursiveChangePermissions(file, filePerms, dirPerms);
					} else {
						Files.setPosixFilePermissions(file.toPath(), filePerms);
					}
				} catch (Exception e) {
					LOGGER.warn(String.format("Unable to set permissions on %s. Skipping.", file.getCanonicalPath()), e);
					continue;
				}
			}
		} catch (Exception e) {
			LOGGER.warn(String.format("Unable to set permissions on %s", dir.getAbsolutePath()), e);
		}
	}

	/**
	 * Set permissions on the given File to rwxrwxr-x
	 *
	 * @param file
	 *            - file or directory. If dir, then recursive.
	 */
	public static void setFriendlyPermissions(File file) {
		// Make friendly public permissions
		Set<PosixFilePermission> filePerms = new HashSet<PosixFilePermission>();
		filePerms.add(PosixFilePermission.OWNER_READ);
		filePerms.add(PosixFilePermission.OWNER_WRITE);
		filePerms.add(PosixFilePermission.OWNER_EXECUTE);
		filePerms.add(PosixFilePermission.GROUP_READ);
		filePerms.add(PosixFilePermission.GROUP_WRITE);
		filePerms.add(PosixFilePermission.GROUP_EXECUTE);
		filePerms.add(PosixFilePermission.OTHERS_READ);
		Set<PosixFilePermission> dirPerms = new HashSet<PosixFilePermission>();
		dirPerms.addAll(filePerms);
		dirPerms.add(PosixFilePermission.OTHERS_EXECUTE);
		recursiveChangePermissions(file, filePerms, dirPerms);
	}

    /**
     * Unzip zip file to output dir.
     * @param file
     * @param outputDir
     * @throws IOException
     */
    public static void unzip(File file, File outputDir) throws IOException {
        final int BUFFER = 4096;
        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(file);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            int count;
            byte[] data = new byte[BUFFER];
            // write the new file to output dir
            File newFile = new File(entry.getName());
            File outputFile = new File(outputDir.getAbsolutePath(),
                    newFile.getName());
            FileOutputStream fos = new FileOutputStream(outputFile);
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        zis.close();
    }
}
