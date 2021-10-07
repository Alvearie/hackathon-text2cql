/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentVectorizer;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.FeatureSpace;
import org.alvearie.dream.intent.nlp.text.FeatureVector;
import org.alvearie.dream.intent.nlp.text.NGram;
import org.alvearie.dream.intent.nlp.text.Utils;

import java.util.Set;

/**
 * Creates a Term Frequency - Inverse Document Frequency vector for a collection of {@link Document}s.
 *
 */
public class TFIDFVectorizer implements DocumentVectorizer, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -911548753272103091L;

    /**
     * The default TF-IDF vectorization used in the scikit proof of concept, used L2-normed so in order to recreate that we
     * implement l2 norm here too.
     * <p>
     * The following text was copied from <a href="https://machinelearningmastery.com/vector-norms-machine-learning/">
     * Gentle Introduction to Vector Norms in Machine Learning</a> <i>
     * <p>
     * Calculating the size or length of a vector is often required either directly or as part of a broader vector or
     * vector-matrix operation. The length of the vector is referred to as the vector norm or the vector’s magnitude. The
     * length of a vector is a nonnegative number that describes the extent of the vector in space, and is sometimes
     * referred to as the vector’s magnitude or the norm. The length of the vector is always a positive number, except for a
     * vector of all zero values. It is calculated using some measure that summarizes the distance of the vector from the
     * origin of the vector space.
     * <p>
     * The L2 norm calculates the distance of the vector coordinate from the origin of the vector space. As such, it is also
     * known as the Euclidean norm as it is calculated as the Euclidean distance from the origin. The result is a positive
     * distance value </i>
     *
     * @param document
     */
    private static void l2Normalize(Document document) {
        Double norm = 0.0;
        FeatureVector tfidf = document.getVector();
        if (tfidf.isZeroVector()) {
            // The Zero Vector cannot be normalized, b/c it will result in NaNs
            // The normalization of the zero vector is the zero vector itself
            document.setVector("L2", tfidf);
            return;
        }
        Collection<Double> values = tfidf.getValues();
        for (Double value : values) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        FeatureVector l2Norm = new FeatureVector(tfidf.size());
        for (Entry<Feature, Double> entry : tfidf.getFeaturesAndValues().entrySet()) {
            Feature feature = entry.getKey();
            Double value = entry.getValue();
            Double normalizedValue = value / norm;
            l2Norm.addFeature(feature, normalizedValue);
        }
        document.setVector("L2", l2Norm);
    }

    private FeatureVector idfs;

    private boolean l2Normalize;

    /**
     * Create a default {@link TFIDFVectorizer} with L2 normalization.
     */
    public TFIDFVectorizer() {
        this(true);
    }

    /**
     * Create a {@link TFIDFVectorizer} with L2 normalization.
     *
     * @param l2Normalization apply L2 normalization to the final vector
     */
    public TFIDFVectorizer(boolean l2Normalization) {
        l2Normalize = l2Normalization;
    }

    /**
     * Create a {@link TFIDFVectorizer} using the provided configuration
     *
     * @param configuration
     */
    public TFIDFVectorizer(Configuration configuration) {
        this(configuration.l2Normalize());
    }

    /**
     * Calculate the Inverse Document Frequency vector.
     * <p>
     * The IDF is calculated by getting the corpus frequency of all features, that is the number of times each features
     * occurs in the whole corpus. This is done by adding all the number of times each feature occurs in each document.
     * Subsequently, once we have the document frequencies we calculate IDF by using the following formula: <code>
     * <p>
     *      idf = log(1.0 + number of documents) / (1 + term corpus frequency) + 1
     *
     * </code>
     * <p>
     * The above formula was obtained from the documentation of the TF-IDF transformer from scikit-learn: <code>
     * <p>If ``smooth_idf=True`` (the default), the constant "1" is added to the
     * numerator and denominator of the idf as if an extra document was seen
     * containing every term in the collection exactly once, which prevents
     * zero divisions: idf(d, t) = log [ (1 + n) / (1 + df(d, t)) ] + 1.
     * </code>
     *
     * @param documents the corpus
     * @return the IDF vector
     */
    private FeatureVector calculateIDFs(Corpus corpus, List<Feature> features) {
        int n = corpus.size();
        int m = features.size();
        FeatureVector idfs = new FeatureVector(m);
        for (Feature feature : features) {
            if (feature instanceof NGram) {
                NGram ngram = (NGram) feature;
                int nGramFrequency = corpus.getNGramCount(ngram);
                if (nGramFrequency == -1) {
                    throw new IllegalStateException("This TF-IDF vectorizer requires the n-gram in corpus frequency to be set. If that is not possible this implementation needs to change so that is implemented.");
                }
                double idf = Math.log((1.0 + n) / (1.0 + nGramFrequency)) + 1;
                idfs.addFeature(feature, idf);
            }
        }
        return idfs;
    }

    /**
     * Calculate the Term Frequencies vector.
     * <p>
     * This method assumes a BoW Vectorizer has run first, which will be used to calculate an adjusted TF per Document using
     * the following formula: <code>
     * <p>
     *      term frequency = feature frequency / number of features in document
     * </code>
     *
     * @param documents the corpus
     * @return returns a list of {@link FeatureVector}s containing the TFs for each {@link Document}
     */
    private List<FeatureVector> calculateTermFrequencies(List<Document> documents) {
        final List<FeatureVector> termFrequenciesVectors = new ArrayList<>(documents.size());

        documents.stream().map(document -> {
            FeatureVector bowVector = document.getVector();
            if (bowVector == null) {
                throw new IllegalStateException("The TF-IDF vectorizer requires the BoW vectorizer to on this Document run first.");
            }
            FeatureVector tfVector = new FeatureVector(bowVector.size());
            Map<Feature, Double> featuresAndFrequenciesMap = bowVector.getFeaturesAndValues();
            Set<Entry<Feature, Double>> featuresAndFrequencies = featuresAndFrequenciesMap.entrySet();
            for (Entry<Feature, Double> featureAndFrequency : featuresAndFrequencies) {
                Feature feature = featureAndFrequency.getKey();
                Double bowFrequency = featureAndFrequency.getValue();
                // We calculate the TF adjusted to the number of terms (n-grams) in the document
                double tf = bowFrequency / bowVector.size();
                tfVector.addFeature(feature, tf);
            }
            return tfVector;
        }).forEach(tfVector -> termFrequenciesVectors.add(tfVector));
        return termFrequenciesVectors;
    }

    /**
     * This method builds the TF-IDF vector for all documents, using the following formula: <code>
     * <p>
     *      tf-idf = tf * idf
     * </code>
     * <p>
     * The Document's vector will be set by this method after running.
     *
     * @param documents the documents
     * @param termFrequencies the term frequencies for each document
     * @param idfs the inverse document frequencies for each feature
     */
    private void calculateTFIDF(List<Document> documents, List<FeatureVector> termFrequencies, FeatureVector idfs) {
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            FeatureVector documentTermFrequency = termFrequencies.get(i);
            FeatureVector tfidfVector = new FeatureVector(documentTermFrequency.size());
            Map<Feature, Double> featureMap = documentTermFrequency.getFeaturesAndValues();
            for (Entry<Feature, Double> entry : featureMap.entrySet()) {
                Feature feature = entry.getKey();
                Double tf = entry.getValue();
                Double idf = idfs.getValue(feature);
                Double tfidf = tf * idf;
                tfidfVector.addFeature(feature, tfidf);
            }
            document.setVector("TF-IDF", tfidfVector);
        }
    }

    /**
     * Run l2Normalization against a list of documents
     *
     * @param documents the document's whose vectors will be l2 normalized
     */
    private void l2Normalize(List<Document> documents) {
        Utils.stream(documents).forEach(TFIDFVectorizer::l2Normalize);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.DocumentVectorizer#vectorize(org.alvearie.nlp.text.Document)
     */
    @Override
    public void vectorize(Document document) {
        FeatureSpace corpusFeatures = document.getFeatureSpace();
        if (corpusFeatures == null) {
            throw new IllegalArgumentException("The Corpus feture space is not set in the given Document and it is required.");
        }
        Corpus corpus = new Corpus(document);
        corpus.setFeatureSpace(corpusFeatures);
        vectorize(corpus);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.DocumentVectorizer#vectorize(org.alvearie.nlp.text.Corpus)
     */
    @Override
    public void vectorize(Corpus corpus) {
        if (corpus == null) {
            throw new NullPointerException("The corpus was null.");
        }
        List<Document> documents = corpus.getDocuments();
        List<Feature> features = corpus.getFeatureSpace();
        List<FeatureVector> termFrequencies = calculateTermFrequencies(documents);
        if (idfs == null) {
            idfs = calculateIDFs(corpus, features);
        }
        calculateTFIDF(documents, termFrequencies, idfs);

        if (l2Normalize) {
            l2Normalize(documents);
        }
    }
}
