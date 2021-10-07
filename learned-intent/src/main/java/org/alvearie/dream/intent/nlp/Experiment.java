/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.alvearie.dream.intent.nlp.text.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * This class allows easy access to experiment-wide settings. It can be used to create a new folder for logging
 * experiment data, retrieving experiment-wide settings, or log data. It can also retrieve old experiments, and all
 * experiments are segregated by name.
 *
 */
public class Experiment {

    // The base directory to use for experiments (defaults to /home/USER/experiments)
    private static String baseDirectoryName = System.getProperty("user.home") + "/experiments";

    // The name of the last experiment used by the Experiment framework. Allows quick access of the current experiment by
    // code that is agnostic to which experiment is running.
    private static String currentExperimentName = null;

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    /**
     * A cache of previously loaded/created Experiments to speed up access.
     */
    private static final Map<String, List<Experiment>> EXPERIMENT_CACHE = new ConcurrentHashMap<>();

    /**
     * Create a new experiment with the given name.
     *
     * @param experimentName
     * @return A new Experiment
     */
    public static Experiment createExperiment(String experimentName) {
        Experiment experiment = new Experiment(experimentName);
        currentExperimentName = experimentName;
        List<Experiment> experiments = EXPERIMENT_CACHE.computeIfAbsent(experimentName, e -> new ArrayList<>());
        experiments.add(experiment);
        return experiment;
    }

    /**
     * Return the most recent experiment for the experiment name last used.
     *
     * @return the most recent experiment for the experiment name last used
     */
    public static Experiment getCurrentExperiment() {
        return getCurrentExperiment(currentExperimentName);
    }

    /**
     * Return the most recent experiment for the experiment name provided.
     *
     * @param experimentName
     * @return the most recent experiment for the experiment name provided
     */
    public static Experiment getCurrentExperiment(String experimentName) {
        if (experimentName == null) {
            return null;
        }
        List<Experiment> experiments = EXPERIMENT_CACHE.get(experimentName);
        if (experiments != null) {
            currentExperimentName = experimentName;
            return experiments.get(experiments.size() - 1);
        }
        return getLastExperimentBefore(experimentName, null);
    }

    /**
     * @return the name of the experiment last created/loaded for the current process.
     */
    public static String getCurrentExperimentName() {
        return currentExperimentName;
    }

    /**
     * @return the most recent experiment prior to the current experiment for the current experiment name
     */
    public static Experiment getLastExperimentBefore() {
        Experiment currentExperiment = getCurrentExperiment(currentExperimentName);
        return getLastExperimentBefore(currentExperiment.experimentName, currentExperiment.experimentDate);
    }

    /**
     * This method will find the most recent experiment for the given experiment name that occurred prior to the provided
     * date. This is helpful for finding the "last run" to compare a current run to.
     *
     * @param experimentName
     * @param beforeDate the date to start looking prior to for an experiment. Null indicates use "now".
     * @return the last experiment prior to the given date
     */
    public static Experiment getLastExperimentBefore(String experimentName, Date beforeDate) {
        if (experimentName == null) {
            return null;
        }
        if (beforeDate == null) {
            beforeDate = new Date();
        }

        File experimentTypeDirectory = new File(baseDirectoryName, experimentName);

		File[] fileArray = experimentTypeDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});

        if (fileArray == null) {
            return null;
        }
        List<File> experimentDirectories = Arrays.asList(fileArray);
        experimentDirectories.sort(new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });

        File experimentDirectory = null;
        for (int i = experimentDirectories.size() - 1; i >= 0; i--) {
            File directory = experimentDirectories.get(i);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date directoryDate = null;
            try {
                directoryDate = sdf.parse(directory.getName());
            } catch (ParseException e) {
                System.err.println("Experiment directory found that does not match the naming format and it will be skipped: " + directory.getName());
            }
            if (directoryDate.before(beforeDate)) {
                experimentDirectory = directory;
                break;
            }
        }

        if (experimentDirectory == null) {
            System.out.println("No directory found for experiment: " + experimentName + " prior to: " + beforeDate);
            return null;
        }

        currentExperimentName = experimentName; // Remember the experiment name
        List<Experiment> experiments = EXPERIMENT_CACHE.computeIfAbsent(experimentName, e -> new ArrayList<>());
        for (Experiment experiment : experiments) {
            if (experiment.experimentDirectory.equals(experimentDirectory)) {
                // If the target experiment is already loaded, return it.
                return experiment;
            }
        }
        // Target experiment is not loaded. Attempt to load it.
        Experiment experiment = loadExperiment(experimentDirectory);
        if (experiment != null) {
            experiments.add(experiment);
        }
        return experiment;
    }

    /**
     * Load experiment in a given directory
     *
     * @param experimentDirectory
     * @return
     */
    private static Experiment loadExperiment(File experimentDirectory) {
        System.err.println("Loading experiment from: " + experimentDirectory.getAbsolutePath());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date experimentDate;
        try {
            experimentDate = sdf.parse(experimentDirectory.getName());
        } catch (ParseException e) {
            throw new ExperimentException("Error parsing the experiment directory", e);
        }

        String experimentName = experimentDirectory.getParentFile().getName();
        Experiment experiment = new Experiment(experimentName, experimentDate);
        experiment.setConfiguration((Configuration) experiment.load("Configuration.ser"));
        experiment.experimentDirectory = experimentDirectory;
        return experiment;
    }

    /**
     * Save the provided object to the current experiment, if one exists, using the provided filename
     *
     * @param fileName
     * @param serializableObject
     */
    public static void save(String fileName, Serializable serializableObject) {
        save(fileName, serializableObject, false);
    }

    /**
     * Save the provided object to the current experiment, if one exists, using the provided filename
     *
     * @param fileName
     * @param serializableObject
     * @param overwrite
     */
    public static void save(String fileName, Serializable serializableObject, boolean overwrite) {
        Experiment currentExperiment = Experiment.getCurrentExperiment();
        if (currentExperiment != null) {
            currentExperiment.saveObject(fileName, serializableObject, overwrite);
        }
    }

    /**
     * @param newBaseDirectoryName
     */
    public static void setBaseExperimentDirectoryName(String newBaseDirectoryName) {
        baseDirectoryName = newBaseDirectoryName;
    }

    /**
     * Set the current process-wide experiment name. This will be used when code is indifferent to which experiment is
     * running and just calls "getCurrentExperiment()"
     *
     * @param currentExperimentName
     */
    public static void setCurrentExperimentName(String currentExperimentName) {
        Experiment.currentExperimentName = currentExperimentName;
    }

    private Configuration configuration;

    private Date experimentDate;

    private File experimentDirectory;

    private String experimentName;

    private long experimentStartTime;

    private Logger logger;

    private Experiment(String experimentName) {
        this(experimentName, new Date());
    }

    private Experiment(String experimentName, Date experimentDate) {
        this.experimentName = experimentName;
        currentExperimentName = experimentName;
        this.experimentDate = experimentDate;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String formattedDate = sdf.format(experimentDate);

        File experimentTypeDirectory = new File(baseDirectoryName, experimentName);
        this.experimentDirectory = new File(experimentTypeDirectory, formattedDate);

        this.configuration = Configuration.getDefault();
        experimentStartTime = System.currentTimeMillis();
    }

    private Logger createLogger() {
        Logger logger = LogManager.getLogger("ExperimentLogger");
//        try {
//            final FileAppender fileAppender = FileAppender.newBuilder()
//                    .setName("Experiment Log")
//                    .withFileName(new File(experimentDirectory, "Experiment.log").getAbsolutePath())
//                    .withAppend(false)
//                    .build();
//            fileAppender.start();
//            // Note: Need to add the created appender to the log4j configuration before creating the
//            // AsyncAppender. For details, see https://logging.apache.org/log4j/2.x/manual/customconfig.html
//            final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
//            final org.apache.logging.log4j.core.config.Configuration log4jConfig = loggerContext.getConfiguration();
//            log4jConfig.addAppender(fileAppender);
//
//            if (logger instanceof org.apache.logging.log4j.core.Logger) {
//                LoggerConfig loggerConfig = ((org.apache.logging.log4j.core.Logger) logger).get();
//                for (org.apache.logging.log4j.core.Appender appender : loggerConfig.getAppenders().values()) {
//                    loggerConfig.removeAppender(appender.getName());
//                }
//                loggerConfig.addAppender(fileAppender, Level.INFO, null); // null=no filter
//            }
//        } catch (IOException e) {
//            throw new ExperimentException(e);
//        }
        return logger;
    }

    /**
     * @return the Configuration for the current experiment
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @return the date this experiment was created
     */
    public Date getExperimentDate() {
        return experimentDate;
    }

    /**
     * @return the directory where all experiment data is stored.
     */
    public File getExperimentDirectory() {
        return experimentDirectory;
    }

    /**
     * @return the name of the experiment. This allows multiple types of experiments to be segregated in separate
     *         sub-folders.
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * This loads a serialized object from the named file within the current experiment directory. Interpreting/Casting it
     * to the proper type is up to the caller of this method.
     *
     * @param fileName
     * @return the loaded object
     */
    public Object load(String fileName) {
        try {
            File f = new File(experimentDirectory, fileName);
            if (!f.exists()) {
                return null;
            }
            try (ObjectInputStream os = new ObjectInputStream(new FileInputStream(f))) {
                return os.readObject();
            } catch (StreamCorruptedException e) {
                // If the serialized object can't be deserialized, assume it was just a String written to file.
                return new String(Files.readAllBytes(f.toPath()));
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new ExperimentException(e);
        }
    }

    /**
     * Log a message to a log file within the experiment directory.
     *
     * @param s
     */
    public void log(String s) {
        if (logger == null) {
            logger = createLogger();
        }
        logger.error(s);
    }

    /**
     * Save this experiment to the target folder. This will only cover common objects. Process-specific objects need to be
     * saved individually during the experiment pipeline.
     */
    public void save() {
        if (!experimentDirectory.exists()) {
            experimentDirectory.mkdirs();
        }
        log(configuration.toString());
        log("\n\n\nExperiment took: " + (System.currentTimeMillis() - experimentStartTime) + " ms.");
        save("Configuration.ser", configuration);
    }

    /**
     * Save a serializable object to the experiment directory using the provided file name
     *
     * @param fileName
     * @param serializableObject
     * @param overwrite
     */
    private void saveObject(String fileName, Serializable serializableObject, boolean overwrite) {
        if (!experimentDirectory.exists()) {
            experimentDirectory.mkdirs();
        }

        try {
            File file = new File(experimentDirectory, fileName);
            if (!overwrite && file.exists()) {
                return;
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                if (serializableObject instanceof String) {
                    fos.write((((String) serializableObject).getBytes()));
                } else {
                    try (ObjectOutputStream os = new ObjectOutputStream(fos)) {
                        os.writeObject(serializableObject);
                    }
                }
            }
        } catch (IOException e) {
            throw new ExperimentException(e);
        }
    }

    /**
     * Set the configuration used by this experiment.
     *
     * @param configuration
     */
    public void setConfiguration(Configuration configuration) {
        if (configuration == null) {
            configuration = Configuration.getDefault();
        } else {
            Configuration.setDefault(configuration);
        }
        this.configuration = configuration;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return getExperimentName() + " - " + sdf.format(getExperimentDate());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Experiment) {
            Experiment otherExperiment = (Experiment) o;
            return Objects.equals(experimentName, otherExperiment.experimentName) &&
                    Objects.equals(experimentDate, otherExperiment.experimentDate);
        }
        return false;
    }
}
