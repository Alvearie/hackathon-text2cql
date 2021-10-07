/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text.processors;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentVectorizer;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.FeatureVector;
import org.alvearie.dream.intent.nlp.text.NGram;

/**
 * Creates a Bag of Words vector for {@link Document}s.
 * <p>
 * This implementation uses regexes to count the feature occurrences in a document.
 * <p>
 * This BoW vectorizer is based on n-grams, not just words, but because of the way this BoW vectorizer is implemented,
 * it can be very slow and is not recommended for production purposes.
 *
 */
public class RegexBoWVectorizer implements DocumentVectorizer {

    private final static Map<String, Pattern> PATTERNS = new ConcurrentHashMap<String, Pattern>();

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.DocumentVectorizer#vectorize(org.alvearie.nlp.text.Document)
     */
    @Override
    public void vectorize(Document document) {
        Set<NGram> features = document.getNGrams();
        if (features == null) {
            throw new IllegalStateException("Valid feature list is required.");
        }
        FeatureVector vector = new FeatureVector();
        for (Feature feature : features) {
            int featureCount = 0;
            Matcher matcher = getPattern("\\b" + feature.getFeature() + "\\b").matcher(document.getText());
            while (matcher.find()) {
                featureCount++;
            }
            if (featureCount > 0) {
                vector.addFeature(feature, Double.valueOf(featureCount));
            }
        }
        // TODO Disabling this for now, but we need to find a way to track state in the document better without maintaining all
        // prior state (when in production mode)
        // if (!Configuration.getDefault().isTraceEnabled()) {
        // document.clearNGrams();
        // }
        document.setVector("BoW", vector);
    }

    /**
     * Gets a {@link Pattern} for the given regex. The pattern may be obtained from a cache.
     *
     * @param pattern the pattern
     * @return the Pattern
     */
    private Pattern getPattern(String pattern) {
        return PATTERNS.computeIfAbsent(pattern, key -> Pattern.compile(pattern));
    }
}
