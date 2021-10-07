/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link Collection} of {@link Document}s.
 *
 */
public class Corpus implements Vectorizable, Iterable<Document>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2216230674908972826L;

    private List<Document> documents;
    private FeatureSpace featureSpace;
    private Map<NGram, Integer> ngramCount;

    /**
     * Creates a {@link Corpus} from the given {@link Document} {@link List}.
     *
     * @param documents the documents for this corpus
     */
    public Corpus(List<Document> documents) {
        if (documents == null) {
            throw new NullPointerException("The document list is null.");
        }
        this.documents = documents;
        ngramCount = new LinkedHashMap<>();
    }

    /**
     * Creates a {@link Corpus} from the given {@link Document} array.
     *
     * @param documents the documents for this corpus
     */
    public Corpus(Document... documents) {
        if (documents == null) {
            throw new NullPointerException("The document array is null.");
        }
        this.documents = Arrays.asList(documents);
        ngramCount = new LinkedHashMap<>();
    }

    /**
     * @return the documents
     */
    public List<Document> getDocuments() {
        return documents;
    }

    /**
     * @return the number of {@link Document}s in this {@link Corpus}
     */
    public int size() {
        return documents.size();
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Document> iterator() {
        return documents.iterator();
    }

    /**
     * @return the featureSpace for this {@link Corpus} or null if it hasn't been set
     */
    @Override
    public FeatureSpace getFeatureSpace() {
        return featureSpace;
    }

    /**
     * @param featureSpace the feature space for this {@link Corpus} and all of its {@link Document}s
     */
    public void setFeatureSpace(FeatureSpace featureSpace) {
        this.featureSpace = featureSpace;
        for (Document document : getDocuments()) {
            document.setFeatureSpace(featureSpace);
        }
    }

    /**
     * Gets all the n-gram counts for this corpus.
     *
     * @return an unmodifiable {@link Map} from {@link NGram}s to their count in this corpus
     */
    public Map<NGram, Integer> getNGramCounts() {
        return Collections.unmodifiableMap(ngramCount);
    }

    /**
     * Gets the count for the given n-gram in this corpus.
     *
     * @param ngram the n-gram to get the count for
     * @return the count or 0 if the n-gram is not present in this corpus
     */
    public int getNGramCount(NGram ngram) {
        Integer count = ngramCount.get(ngram);
        return count == null ? 0 : count;
    }

    /**
     * Sets the count for the given {@link NGram} in this {@link Corpus}.
     *
     * @param ngram the n-gram
     * @param count the corpus count
     */
    public void setNGramCount(NGram ngram, int count) {
        ngramCount.put(ngram, count);
    }

    /**
     * Prints the feature matrix for this corpus to the console.
     */
    public void printMatrix() {
        if (documents.isEmpty()) {
            return;
        }
        List<String> rows = new ArrayList<>();
        for (Feature feature : featureSpace) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(String.format("%-20s", feature.getFeature()));
            for (Document document : documents) {
                buffer.append(String.format("%.6f  ", document.getVector().getValue(feature)));
            }
            rows.add(buffer.toString());
        }
        System.out.println(String.join("\n", rows));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        List<String> messages = new ArrayList<>();
        messages.add("Corpus: " + size() + " document(s)");
        if (featureSpace != null) {
            messages.add("Feature Space: " + getNGramCounts());
        }
        String message = String.join("\n|-> ", messages);
        return message;
    }
}
