/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents any generic document that needs to be put through some analytics.
 * <p>
 * A {@link Document} is agnostic from the specific analytics that it will be put through, it doesn't know about
 * classifications, clustering, etc., however it does know about the history of the text operations that were performed
 * on it before it was submitted to analytics.
 * <p>
 * A {@link Document}'s text can be transformed, this class includes {@link #getText()} to retrieve the latest version
 * of the text. A {@link Document} can be tokenized, this class includes {@link #getTokens()} to retrieve its tokens
 * when that happens. A {@link Document} can be n-grammed, this class includes {@link #getNGrams()} to retrieve its
 * n-grams when that happens.
 * <p>
 * A {@link Document} can be vectorized, this class includes a {@link #getVector()} method to retrieve its vector when
 * that happens.
 *
 */
public class Document implements Vectorizable, Serializable {

    private static final long serialVersionUID = 4053914420978754377L;

    private String id;
    private String originalText;
    private String text;
    private Map<String, String> textHistory;
    private List<String> tokens;
    private Map<NGram, Integer> ngramCounts;
    private FeatureSpace featureSpace;
    private FeatureVector vector;
    private Map<String, FeatureVector> vectorHistory;

    /**
     * Create a {@link Document} with the given text and ID
     *
     * @param text the text
     */
    public Document(String text) {
        this(null, text);
    }

    /**
     * Create a {@link Document} with the given text and ID
     *
     * @param id an optional ID
     * @param text the text
     */
    public Document(String id, String text) {
        this.id = id;
        originalText = text;
        textHistory = new LinkedHashMap<>();
        setText("Initial Text", originalText);
        vectorHistory = new LinkedHashMap<>();
        ngramCounts = new LinkedHashMap<>();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the current text of this document
     */
    public String getText() {
        return text;
    }

    /**
     * @return the current vector representation of this document
     */
    public FeatureVector getVector() {
        return vector;
    }

    /**
     * @return the original text with which this document was created
     */
    public String getOriginalText() {
        return originalText;
    }

    /**
     * @return the tokens, or null if this document has not been tokenized
     */
    public List<String> getTokens() {
        return tokens;
    }

    /**
     * @return the feature space
     */
    @Override
    public FeatureSpace getFeatureSpace() {
        return featureSpace;
    }

    /**
     * Sets the feature space
     *
     * @param featureSpace feature space for the current corpus
     */
    public void setFeatureSpace(FeatureSpace featureSpace) {
        this.featureSpace = featureSpace;
    }

    /**
     * Changes the text of this document as a result of performing the given operation
     *
     * @param operation the operation that was performed on this document
     * @param text the new text
     */
    public void setText(String operation, String text) {
        if (text == null) {
            throw new NullPointerException("Text was null");
        }
        this.text = text;
        if (Configuration.getDefault().isTraceEnabled()) {
            textHistory.put(operation, text);
        }
    }

    /**
     * Changes the text of this document as a result of performing the given operation
     *
     * @param vectorName the operation that was performed to create this vector
     * @param vector the vector
     */
    public void setVector(String vectorName, FeatureVector vector) {
        if (vector == null) {
            throw new NullPointerException("The vector was null");
        }
        this.vector = vector;
        if (Configuration.getDefault().isTraceEnabled()) {
            vectorHistory.put(vectorName, vector);
        }
    }

    /**
     * Sets the tokens for this document
     *
     * @param tokens the tokens for this text
     */
    public void setTokens(List<String> tokens) {
        if (tokens == null) {
            throw new NullPointerException("Tokens were null");
        }
        this.tokens = new ArrayList<>(tokens);
    }

    /**
     * Gets the NGrams for this Document.
     *
     * @return the ngrams
     */
    public Set<NGram> getNGrams() {
        return ngramCounts.keySet();
    }

    /**
     * Gets the count for the given n-gram in this document.
     *
     * @param ngram the n-gram to get the count for
     * @return the count or 0 if the n-gram is not present in this document
     */
    public int getNGramCount(NGram ngram) {
        Integer count = ngramCounts.get(ngram);
        return count == null ? 0 : count;
    }

    /**
     * Sets the count for the given {@link NGram} in this {@link Document}.
     *
     * @param ngram the n-gram
     * @param count the corpus count
     */
    public void setNGramCount(NGram ngram, int count) {
        ngramCounts.put(ngram, count);
    }

    /**
     * Allow clearing out ngrams for this document
     */
    public void clearNGrams() {
        ngramCounts.clear();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getOriginalText().hashCode();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Document)) {
            return false;
        }

        final Document other = (Document) object;
        return getOriginalText().trim().equals(other.getOriginalText().trim());
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String id = getId();
        if (id == null) {
            return getOriginalText();
        }
        return id + " - " + getOriginalText();
    }

    /**
     * Generate a verbose description of this document
     *
     * @return a verbose description of this document, or an empty list if the document has not been modified
     */
    public String toVerboseString() {
        List<String> messages = new ArrayList<>();
        Set<Entry<String, String>> changes = textHistory.entrySet();
        for (Entry<String, String> change : changes) {
            String name = change.getKey();
            String text = change.getValue();
            messages.add(name + ": " + text);
        }
        if (tokens != null) {
            messages.add("Tokens: " + tokens);
        }
        if (ngramCounts != null) {
            messages.add("NGrams: " + ngramCounts);
        }
        if (featureSpace != null) {
            messages.add("Feature Space: " + featureSpace);
        }
        Set<Entry<String, FeatureVector>> vectorChanges = vectorHistory.entrySet();
        for (Entry<String, FeatureVector> vectorChange : vectorChanges) {
            String name = vectorChange.getKey();
            FeatureVector documentVector = vectorChange.getValue();
            messages.add(name + ": " + documentVector);
        }

        String message = String.join("\n|-> ", messages);
        return message;
    }

    /**
     * Converts a {@link Collection} of {@link Document}s into a {@link Collection} of {@link String}s created from the
     * document's text.
     *
     * @param documents the documents to convert
     * @return the collection
     * @throws NullPointerException if documents is null
     */
    public static Collection<String> toStringCollection(Collection<Document> documents) {
        if (documents == null) {
            throw new NullPointerException("The document collection is null.");
        }
        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.toList());
    }
}
