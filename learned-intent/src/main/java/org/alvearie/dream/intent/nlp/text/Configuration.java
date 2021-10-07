/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text;

import java.io.Serializable;

import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.classification.classifiers.smile.SmileMaxEntClassifier;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This is a collection of Configuration settings
 *
 */
public class Configuration implements Serializable {

    private static Configuration DEFAULT_CONFIGURATION = new Configuration();

    private static final long serialVersionUID = -5053403269274442988L;

    /**
     * Get the current default configuration.
     *
     * @return current configuration
     */
    public static final Configuration getDefault() {
        return DEFAULT_CONFIGURATION;
    }

    /**
     * Update the default configuration
     *
     * @param defaultConfiguration
     */
    public static void setDefault(Configuration defaultConfiguration) {
        DEFAULT_CONFIGURATION = defaultConfiguration;
    }

    private Words allowedWords;
    private Words stopWords;
    private Words breakWords;
    private boolean breakOnSpecialCharacters;
    private int minimumTokenLength;
    private int minimumTokenFrequency;
    private int maximumTokenFrequency;
    private int nGramMinRange;
    private int nGramMaxRange;
    private boolean stem;
    private boolean lemmatize;
    private boolean keepDigitPlaceholder;
    private boolean removeParentheticalText;
    private int featuresPerTree;
    private boolean l2Normalize;
    private double lambda;
    private int maxDecisionTreeNodes;
    private int maxTrainingDataPerClassSize;
    private int maxTrainingIterations;
    private int numberOfTrees;
    private boolean serialMode;
    private double trainingTolerance;
    private Class<? extends Classifier> classifierClass;
    private boolean skipClassificationForGroundTruthEntries;
    private boolean traceEnabled;

    /**
     * Creates a default {@link Configuration} object.
     */
    public Configuration() {
        stopWords = new Words();
        allowedWords = new Words();
        breakWords = new Words();
        breakOnSpecialCharacters = true;
        minimumTokenLength = 1;
        nGramMinRange = 1;
        nGramMaxRange = 2;
        stem = false;
        lemmatize = true;
        keepDigitPlaceholder = false;
        removeParentheticalText = false;
        minimumTokenFrequency = 2;
        maximumTokenFrequency = Integer.MAX_VALUE;
        featuresPerTree = -1;
        l2Normalize = true;
        lambda = 0.000001;
        maxDecisionTreeNodes = 600;
        maxTrainingDataPerClassSize = -1;
        maxTrainingIterations = 1000;
        numberOfTrees = 50;
        serialMode = false;
        trainingTolerance = .001; // Tested and seems to give just as good accuracy as smaller numbers, but much faster
        classifierClass = SmileMaxEntClassifier.class;
        skipClassificationForGroundTruthEntries = false;
        traceEnabled = false;
    }

    /**
     * @return treat special characters that would normally be removed as indicators of sentence breaks.
     */
    public boolean breakOnSpecialCharacters() {
        return breakOnSpecialCharacters;
    }

    /**
     * @return the allowed {@link Words} which will always make it into normalized text regardless of their size, etc.,
     *         default to an empty set of words
     */
    public Words getAllowedWords() {
        return allowedWords;
    }

    /**
     * Get the currently configured Classifier type to use.
     *
     * @return classifier class to use
     */
    public Class<? extends Classifier> getClassifierClass() {
        return classifierClass;
    }

    /**
     * @return number of features per tree to use in random forest model
     */
    public int getFeaturesPerTree() {
        return featuresPerTree;
    }

    /**
     * @return lambda value
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * @return maximum number of nodes in decision tree model
     */
    public int getMaxDecisionTreeNodes() {
        return maxDecisionTreeNodes;
    }

    /**
     * @return the maximumTokenFrequency
     */
    public int getMaximumTokenFrequency() {
        return maximumTokenFrequency;
    }

    /**
     * @return the maximum number of training cases per class to use from training data.
     */
    public int getMaxTrainingDataPerClassSize() {
        return maxTrainingDataPerClassSize;
    }

    /**
     * @return maximum number of training iterations to attempt
     */
    public int getMaxTrainingIterations() {
        return maxTrainingIterations;
    }

    /**
     * @return the minimumTokenFrequency
     */
    public int getMinimumTokenFrequency() {
        return minimumTokenFrequency;
    }

    /**
     * @return the minimum token size, tokens that are not at least this length will be removed, default to 2
     */
    public int getMinimumTokenLength() {
        return minimumTokenLength;
    }

    /**
     * @return the nGramMinRange, the maximum range for n-gramming, default 2
     */
    public int getNGramMaxRange() {
        return nGramMaxRange;
    }

    /**
     * @return the nGramMinRange, the minimum range for n-gramming, default 1
     */
    public int getNGramMinRange() {
        return nGramMinRange;
    }

    /**
     * @return number of trees to use in random forest model
     */
    public int getNumberOfTrees() {
        return numberOfTrees;
    }

    /**
     * @return the {@link Words} that should be ignored, and prevent features from being generated from the words before/after them
     */
    public Words getBreakWords() {
        return breakWords;
    }

    /**
     * @return the {@link Words} to be used as stop words, default to an empty set of words
     */
    public Words getStopWords() {
        return stopWords;
    }

    /**
     * @return tolerance level for train
     */
    public double getTrainingTolerance() {
        return trainingTolerance;
    }

    /**
     * @return boolean indicating whether currently configured for serial mode, this is a JVM-wide configuration.
     */
    public final boolean isSerialMode() {
        return serialMode;
    }

    /**
     * @return trace enabled for current configuration
     */
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    /**
     * Normalization converts digits into a placeholder string, this method determines whether to keep the placeholder
     * around or remove it.
     *
     * @return whether to keep the digit placeholder string around, default to false
     */
    public boolean keepDigitPlaceholder() {
        return keepDigitPlaceholder;
    }

    /**
     * @return should l2 normalization be run
     */
    public boolean l2Normalize() {
        return l2Normalize;
    }

    /**
     * @return whether to run lemmatization as part of text normalization, default is true
     */
    public boolean lemmatize() {
        return lemmatize;
    }

    /**
     * @return the removeParentheticalText, default true
     */
    public boolean removeParentheticalText() {
        return removeParentheticalText;
    }

    /**
     * Sets a set of {@link Words} which will always be part of normalized text.
     *
     * @param allowedWords the allowedWords to set
     */
    public void setAllowedWords(Words allowedWords) {
        this.allowedWords = allowedWords;
    }

    /**
     * Set the current class to use for classifying.
     *
     * @param classifierClass classifier class
     */
    public void setClassifierClass(Class<? extends Classifier> classifierClass) {
        this.classifierClass = classifierClass;
    }

    /**
     * @param featuresPerTree
     */
    public void setFeaturesPerTree(int featuresPerTree) {
        this.featuresPerTree = featuresPerTree;
    }

    /**
     * Normalization converts digits into a placeholder, this method sets whether to keep the placeholder around or remove
     * it.
     *
     * @param keepDigitPlaceholder the keepDigitPlaceholder to set
     */
    public void setKeepDigitPlaceholder(boolean keepDigitPlaceholder) {
        this.keepDigitPlaceholder = keepDigitPlaceholder;
    }

    /**
     * @param l2Normalize
     */
    public void setL2Normalize(boolean l2Normalize) {
        this.l2Normalize = l2Normalize;
    }

    /**
     * @param lambda
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    /**
     * Sets whether to run lemmatization as part of text normalization.
     *
     * @param lemmatize the lemmatize to set
     */
    public void setLemmatize(boolean lemmatize) {
        this.lemmatize = lemmatize;
    }

    /**
     * @param maxDecisionTreeNodes
     */
    public void setMaxDecisionTreeNodes(int maxDecisionTreeNodes) {
        this.maxDecisionTreeNodes = maxDecisionTreeNodes;
    }

    /**
     * @param maximumTokenFrequency the maxinumTokenFrequency to set
     */
    public void setMaximumTokenFrequency(int maximumTokenFrequency) {
        this.maximumTokenFrequency = maximumTokenFrequency;
    }

    /**
     * @param maxTrainingDataPerClassSize the maximum number of training cases per class to use from training data.
     */
    public void setMaxTrainingDataPerClassSize(int maxTrainingDataPerClassSize) {
        this.maxTrainingDataPerClassSize = maxTrainingDataPerClassSize;
    }

    /**
     * @param maxTrainingIterations
     */
    public void setMaxTrainingIterations(int maxTrainingIterations) {
        this.maxTrainingIterations = maxTrainingIterations;
    }

    /**
     * @param minimumTokenFrequency the minimumTokenFrequency to set
     */
    public void setMinimumTokenFrequency(int minimumTokenFrequency) {
        this.minimumTokenFrequency = minimumTokenFrequency;
    }

    /**
     * Sets the minimum token length for normalized text
     *
     * @param minimumTokenLength the minimumTokenLength to set
     */
    public void setMinimumTokenLength(int minimumTokenLength) {
        this.minimumTokenLength = minimumTokenLength;
    }

    /**
     * @param nGramMaxRange the nGramMaxRange to set
     */
    public void setNGramMaxRange(int nGramMaxRange) {
        this.nGramMaxRange = nGramMaxRange;
    }

    /**
     * @param nGramMinRange the nGramMinRange to set
     */
    public void setNGramMinRange(int nGramMinRange) {
        this.nGramMinRange = nGramMinRange;
    }

    /**
     * @param numberOfTrees
     */
    public void setNumberOfTrees(int numberOfTrees) {
        this.numberOfTrees = numberOfTrees;
    }

    /**
     * @param removeParentheticalText the removeParentheticalText to set
     */
    public void setRemoveParentheticalText(boolean removeParentheticalText) {
        this.removeParentheticalText = removeParentheticalText;
    }

    /**
     * @param m use serial mode boolean (default is false), this is a JVM-wide configuration.
     */
    public final void setSerialMode(boolean m) {
        serialMode = m;
    }

    /**
     * @param skipClassificationForGroundTruthEntries
     */
    public void setSkipClassificationForGroundTruthEntries(boolean skipClassificationForGroundTruthEntries) {
        this.skipClassificationForGroundTruthEntries = skipClassificationForGroundTruthEntries;
    }

    /**
     * @param breakWords the {@link Words} that should be ignored, and prevent features from being generated from the words
     *        before/after them
     */
    public void setBreakWords(Words breakWords) {
        this.breakWords = breakWords;
    }

    /**
     * @param breakOnSpecialCharacters treat special characters that would normally be removed as indicators of sentence
     *        breaks.
     */
    public void setBreakOnSpecialCharacters(boolean breakOnSpecialCharacters) {
        this.breakOnSpecialCharacters = breakOnSpecialCharacters;
    }

    /**
     * Sets whether to run stemming as part of text normalization.
     *
     * @param stem the stem to set
     */
    public void setStem(boolean stem) {
        this.stem = stem;
    }

    /**
     * Sets a set of {@link Words} which will be removed from text.
     *
     * @param stopWords the stopWords to set
     */
    public void setStopWords(Words stopWords) {
        this.stopWords = stopWords;
    }

    /**
     * @param traceEnabled
     */
    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    /**
     * @param trainingTolerance
     */
    public void setTrainingTolerance(double trainingTolerance) {
        this.trainingTolerance = trainingTolerance;
    }

    /**
     * @return whether or not to skip classification for entries found exactly in the ground truth
     */
    public boolean skipClassificationForGroundTruthEntries() {
        return skipClassificationForGroundTruthEntries;
    }

    /**
     * @return whether to run stemming as part of text normalization, default is false
     */
    public boolean stem() {
        return stem;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
