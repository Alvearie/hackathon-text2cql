/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.lemmatization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.pear.tools.PackageInstaller;
import org.apache.uima.pear.tools.PackageInstallerException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;


/**
 *
 * This class contains two utilities:
 *
 * 1) A method for installing a PEAR file, i.e. extract the LanguageWare model to the file system, and
 * 2) A method for getting a UIMA analysis engine (see {@link AnalysisEngine}) from a given PEAR descriptor
 */
class LanguageWareUtils {

    private static final Logger LOGGER = Logger.getLogger(LanguageWareUtils.class.getName());

    /**
     * Track which Pears have been installed this run, so we don't re-install them over-and-over.
     */
    private static final Collection<String> installedPears = Collections.synchronizedSet(new HashSet<>());

    /**
     * Install the Pear file.
     *
     * @param pearPackage A {@link File} object representing the PEAR file to extract.
     * @param installDir A {@link File} object representing the directory to extract the UIMA PEAR file to.
     *
     * @throws PackageInstallerException if PEAR installation failed.
     *
     */
    static void installPear(File pearPackage, File installDir) throws PackageInstallerException {
    	String key = pearPackage.getAbsolutePath() + "-->" + installDir.getAbsolutePath();
        if (installedPears.contains(key)) {
            return;
        }
        synchronized (LanguageWareUtils.class) {
            if (installedPears.contains(key)) {
                return;
            }
            try {
                PackageInstaller.installPackage(installDir, pearPackage, true, true);
                setFriendlyPermissions(installDir);
                installedPears.add(key);
            } catch (PackageInstallerException e) {
                LOGGER.error("PEAR installation failed", e);
                throw e;
            }
        }
    }

    /**
     * Set permissions on the given File to rwxrwxr-x
     *
     * @param file - file or directory. If dir, then recursive.
     */
    private static void setFriendlyPermissions(File file) {
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
     * Set file permissions on all files and directories in this input dir tree.
     *
     * @param dir
     * @param filePerms
     * @param dirPerms
     */
    private static void recursiveChangePermissions(File dir, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms) {
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
                    LOGGER.warn(String.format("Unable to set permissions on %s. Skipping.", file.getCanonicalPath()),
                            e);
                    continue;
                }
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Unable to set permissions on %s", dir.getAbsolutePath()), e);
        }
    }

    /**
     * Get an {@link AnalysisEngine} from a given UIMA PEAR file.
     *
     * @param pearDescriptor A {@link File} object representing the UIMA PEAR file.
     * @return An {@link AnalysisEngine} object.
     *
     * @throws ResourceInitializationException if the analysis engine could not be initialized.
     * @throws InvalidXMLException if the PEAR descriptor is not a valid XML file.
     * @throws IOException if the PEAR descriptor does not exist.
     *
     */
    static AnalysisEngine getAnalysisEngine(final File pearDescriptor) throws ResourceInitializationException, InvalidXMLException, IOException {

        try {
            /* Create a default resource manager */
            final ResourceManager rsrcMgr = UIMAFramework.newDefaultResourceManager();

            /*
             * Create the analysis engine from the installed PEAR package using
             * the created PEAR specifier
             */
            final XMLInputSource in = new XMLInputSource(pearDescriptor);

            final ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);

            return UIMAFramework.produceAnalysisEngine(specifier, rsrcMgr, null);
        } catch (InvalidXMLException | ResourceInitializationException | IOException e) {
            LOGGER.error("Could not load UIMA analysis engine", e);
            throw e;
        }
    }
}
