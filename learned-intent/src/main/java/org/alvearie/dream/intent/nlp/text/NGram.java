/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An n-gram is a contiguous sequence of n-tokens from a given text.
 * <p>
 * The 1-gram, or unigram, can be considered a Token.
 *
 */
public class NGram implements Feature, Serializable {

    private static final long serialVersionUID = 2071887859074692685L;

    private static final ConcurrentMap<String, NGram> INSTANCES = new ConcurrentHashMap<>();

    private List<String> words;
    private String span;

    /**
     * Create an n-gram from the given words.
     *
     * @param corpusCount the number of NGrams in the corpus
     * @param words the span the span that this n-gram covers
     * @param words the word
     */
    private NGram(String span, String... words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(word);
        }
        this.span = span;
    }

    /**
     * @return the words
     */
    public List<String> getWords() {
        return words;
    }

    /**
     * @return the word span in the occuring text, e.g. if bigram (the, dog), this method returns "the dog"
     */
    public String getWordSpan() {
        return span;
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.Feature#getFeature()
     */
    @Override
    public String getFeature() {
        return getWordSpan();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return span.hashCode();
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

        if (!(object instanceof NGram)) {
            return false;
        }

        final NGram other = (NGram) object;
        return span.equals(other.getWordSpan());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "(" + String.join(", ", words) + ")";
    }

    /**
     * Build the text span for the given set of words
     *
     * @param words
     * @return
     */
    private static String buildSpan(String... words) {
        return String.join(" ", words);
    }

    /**
     * Create an n-gram from the given words.
     *
     * @param words the words to create the n-gram with
     * @return the NGram
     */
    public static NGram getNGram(String... words) {
        String span = NGram.buildSpan(words);
        return INSTANCES.computeIfAbsent(span, key -> new NGram(span, words));
    }
}
