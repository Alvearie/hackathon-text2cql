/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

/**
 * A {@link FeatureVector} is a vector describing a document's features. Each feature as a numeric value represented in
 * the vector.
 *
 */
public class FeatureVector implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -1308398570332558759L;

    /**
     * The default initial capacity for vectors.
     */
    private static final int DEFAULT_CAPACITY = 500;

    private Map<Feature, Double> features;

    /**
     * Create an empty {@link FeatureVector} without any features.
     */
    public FeatureVector() {
        this.features = new LinkedHashMap<>(DEFAULT_CAPACITY);
    }

    /**
     * Create a {@link FeatureVector} from the given {@link List} of {@link Feature}s which will have values set by the
     * corresponding elements of the given values array.
     *
     * @param features the features
     * @param values the corresponding values
     * @throws NullPointerException if either the features and values collections are null
     * @throws IllegalArgumentException if the size of the features and values collections don't match
     */
    public FeatureVector(List<Feature> features, double[] values) {
        if (features == null) {
            throw new NullPointerException("The features list was null.");
        }
        if (values == null) {
            throw new NullPointerException("The values array was null.");
        }
        if (features.size() != values.length) {
            throw new IllegalArgumentException("The features and values vectors have different length. They need to correspond.");
        }
        this.features = new LinkedHashMap<>(features.size());
        for (int i = 0; i < features.size(); i++) {
            addFeature(features.get(i), values[i]);
        }
    }

    /**
     * Create an empty {@link FeatureVector} without any features.
     *
     * @param initialCapacity the initial capacity of this vector
     */
    public FeatureVector(int initialCapacity) {
        // We use a LinkedHashMap because we want to preserve the insertion order of the features
        this.features = new LinkedHashMap<>(initialCapacity);
    }

    /**
     * Adds the given feature with the given value.
     * <p>
     * Features are supposed to be unique, if the same feature is added twice, the value of the last entry will be replaced.
     *
     * @param feature the feature
     * @param value the feature's value
     * @throws NullPointerException if either feature or value are null
     */
    public void addFeature(Feature feature, Double value) {
        if (feature == null) {
            throw new NullPointerException("The feature is null.");
        }
        if (value == null) {
            throw new NullPointerException("The value is null.");
        }

        features.put(feature, value);
    }

    /**
     * @param feature the feature to get the value for
     * @return the value for the given feature, or null if it does not exist
     */
    public Double getValue(Feature feature) {
        return features.containsKey(feature) ? features.get(feature) : 0.0;
    }

    /**
     * @return all the features in this vector
     */
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(features.keySet());
    }

    /**
     * @return the value for the features in this vector
     */
    public Collection<Double> getValues() {
        return Collections.unmodifiableCollection(features.values());
    }

    /**
     * @return a {@link Map} of all the features and values in this vector
     */
    public Map<Feature, Double> getFeaturesAndValues() {
        return Collections.unmodifiableMap(features);
    }

    /**
     * @return the size of this vector, that is the number of features it contains
     */
    public int size() {
        return features.size();
    }

    /**
     * @return true if this is an empty vector, that is a vector without features
     */
    public boolean isEmpty() {
        return features.isEmpty();
    }

    /**
     * A zero vector is a vector whose components are all 0.
     *
     * @return true if this is a zero vector, or if this vector is empty (it has not features)
     */
    public boolean isZeroVector() {
        if (isEmpty()) {
            return true;
        }
        Double sum = getValues().stream().reduce(Double::sum).get();
        return sum.equals(0.0);
    }

    /**
     * Adds the given {@link FeatureVector} to this {@link FeatureVector}, by adding the values of corresponding features.
     *
     * @param vector the vector to add to this vector
     * @return a vector that is the size of the 2 vectors
     */
    public FeatureVector add(FeatureVector vector) {
        FeatureVector sumVector = new FeatureVector(vector.size());
        Set<Feature> allFeatures = new LinkedHashSet<>(getFeatures());
        allFeatures.addAll(vector.getFeatures());
        for (Feature feature : allFeatures) {
            Double thisValue = getValue(feature);
            Double otherValue = vector.getValue(feature);
            Double sum = thisValue + otherValue;
            sumVector.addFeature(feature, sum);
        }
        return sumVector;
    }

    /**
     * Assuming this {@link FeatureVector} is a sparse vector, i.e. it only contains the non-zero features in the vector
     * space and not every possible feature, this method will return the dense vector representation given the full feature
     * space.
     * <p>
     * For instance, if this vector is a vector <code>s</code> such that <code>s = [A=1.0, E=2.0, U=3.0]</code>, given a
     * {@link List} with the {@link Feature}-space representing all vowel letters, this method will return vector
     * <code>d</code> such that <code>d = [A=1.0, E=2.0, I=0.0, O=0.0, U=3.0]</code>
     *
     * @param featureSpace the feature space
     * @return the dense vector created using the given feature space and this sparse vector
     */
    public FeatureVector toDenseVector(List<Feature> featureSpace) {
        FeatureVector expandedVector = new FeatureVector(featureSpace, new double[featureSpace.size()]);
        expandedVector.features.putAll(this.features);
        return expandedVector;
    }

    /**
     * Assuming this {@link FeatureVector} is a dense vector, i.e. it contains every possible feature including the zero
     * features, this method will return the sparse vector representation.
     * <p>
     * For instance, if this vector is a vector <code>s</code> such that
     * <code>s = [A=1.0, E=2.0, I=0.0, O=0.0, U=3.0]</code>, given a this method will return vector <code>d</code> such that
     * <code>d = [A=1.0, E=2.0, U=3.0]</code>
     *
     * @return the sparse vector created using the given feature space and this sparse vector
     */
    public FeatureVector toSparseVector() {
        FeatureVector sparseVector = new FeatureVector();
        Set<Entry<Feature, Double>> entries = this.features.entrySet();
        for (Entry<Feature, Double> entry : entries) {
            if (entry.getValue().equals(Double.valueOf(0))) {
                continue;
            }
            sparseVector.addFeature(entry.getKey(), entry.getValue());
        }
        return sparseVector;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        List<String> messages = new ArrayList<>();
        features.forEach((key, value) -> {
            messages.add(key + "=" + value);
        });
        return "[" + String.join(", ", messages) + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof FeatureVector)) {
            return false;
        }

        FeatureVector other = (FeatureVector) object;
        return this.features.equals(other.features);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return features.hashCode();
    }

    /**
     * @return values for this feature vector in an array format
     */
    public double[] toArray() {
        return getValues().stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * @param threshold minimum value features must have to be converted to 1. Otherwise, value will be 0.
     * @return values for this feature vector, converted to binary (0,1) values, in an array format
     */
    public int[] toBinaryArray(final double threshold) {
        return getValues().stream().mapToInt(d -> d.doubleValue() >= threshold ? 1 : 0).toArray();
    }

    /**
     * Creates a sorted version of this vector. The features in a {@link FeatureVector} are naturally ordered by insertion
     * order. This method returns a sorted version where the order is determined by the magnitude of each feature, in
     * descending order.
     * <p>
     * This {@link FeatureVector} remains unchanged.
     *
     * @return the sorted version of this {@link FeatureVector} by feature value
     */
    public FeatureVector sorted() {
        Map<Feature, Double> featureAndValues = getFeaturesAndValues();
        SortedMap<Feature, Double> sortedFeaturesAndValues = FeatureMapSorter.sort(featureAndValues);
        FeatureVector sortedVector = new FeatureVector(size());
        sortedFeaturesAndValues.forEach((feature, value) -> sortedVector.addFeature(feature, value));
        return sortedVector;
    }

    /**
     * Converts the given {@link List} of {@link FeatureVector} vectors to a native array matrix.
     *
     * @param vectors the vectors to convert
     * @return a matrix with the vector's values
     */
    public static double[][] toMatrix(List<FeatureVector> vectors) {
        if (vectors.isEmpty()) {
            return new double[][] {};
        }
        double[][] matrix = new double[vectors.size()][vectors.get(0).size()];

        for (int i = 0; i < vectors.size(); i++) {
            matrix[i] = vectors.get(i).toArray();
        }
        return matrix;
    }

    /**
     * Converts the given {@link List} of {@link FeatureVector} vectors to a native array matrix, converting doubles to ints
     * based on the threshold.
     *
     * @param vectors the vectors to convert
     * @param threshold minimum value features must have to be converted to 1. Otherwise, values will be 0.
     * @return a matrix with the binary-converted values of this vector
     */
    public static int[][] toBinaryMatrix(List<FeatureVector> vectors, double threshold) {
        int[][] matrix = new int[vectors.size()][vectors.get(0).size()];

        for (int i = 0; i < vectors.size(); i++) {
            matrix[i] = vectors.get(i).toBinaryArray(threshold);
        }
        return matrix;
    }

    /**
     * Calculates the centroid for the given {@link List} of {@link FeatureVector}s, using the given feature space.
     *
     * @param vectors the list of vectors
     * @param featureSpace the feature space
     * @return the centroid
     * @throws NullPointerException if the feature space is null
     * @throws IllegalArgumentException if the feature space is empty
     */
    public static FeatureVector centroid(List<FeatureVector> vectors, List<Feature> featureSpace) {
        if (featureSpace == null) {
            throw new NullPointerException("The faeture space is null.");
        }
        if (featureSpace.isEmpty()) {
            throw new IllegalArgumentException("The feature space is empty.");
        }
        FeatureVector sums = new FeatureVector(featureSpace.size());
        for (FeatureVector vector : vectors) {
            sums = sums.add(vector.toDenseVector(featureSpace));
        }
        FeatureVector centroid = new FeatureVector(featureSpace.size());
        for (Feature feature : sums.getFeatures()) {
            Double value = sums.getValue(feature);
            centroid.addFeature(feature, value / vectors.size());
        }
        return centroid;
    }

    /**
     * Calculate the cosine similarity between this and another vector
     *
     * @param otherVector
     * @return the cosine similarity
     */
    public double cosineSimilarity(FeatureVector otherVector) {
        Set<Feature> allFeatures = new HashSet<>();
        allFeatures.addAll(getFeatures());
        allFeatures.addAll(otherVector.getFeatures());
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (Feature feature : allFeatures) {
            dotProduct += getValue(feature) * otherVector.getValue(feature);
            normA += Math.pow(getValue(feature), 2);
            normB += Math.pow(otherVector.getValue(feature), 2);
        }
        if (normA == 0 || normB == 0) {
            return 0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
