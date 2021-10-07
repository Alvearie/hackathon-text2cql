/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text.processors.smile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alvearie.dream.intent.nlp.Experiment;
import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.FeatureSpace;
import org.alvearie.dream.intent.nlp.text.NGram;
import org.alvearie.dream.intent.nlp.text.NGrammer;
import org.alvearie.dream.intent.nlp.text.Utils;
import org.apache.log4j.Logger;

import smile.nlp.dictionary.EnglishPunctuations;

/**
 * A {@link NGrammer} implementation based on Smile.
 * <p>
 * This N-Grammer can be configured with the lower and upper boundary of the range of n-values for different n-grams to
 * be extracted. All values of n such that min <= n <= max will be used.
 * <p>
 * This N-Grammer can be configured with the lower and upper boundary of the frequencies that are required for an n-gram
 * to be produced. For instance, it can be specified that in order for an n-gram to be considered it needs to appear in
 * the corpus at least n times (indicating that otherwise it would not be important enough to be an n-gram) and at most
 * m times (indicating that if it it appears to often it is probably not a good differentiating feature).
 * <p>
 * This N-Grammer produces {@link Corpus}-level n-grams, and {@link Document}-level n-grams. These n-grams are
 * accessible via {@link Corpus#getFeatureSpace()} and {@link Document#getNGrams()} respectively. Additionally, invoking
 * the {@link Corpus#getNGramCount(NGram)} and {@link Document#getNGramCount(NGram)} will return the total number of
 * times an n-gram appears in the {@link Corpus} and the {@link Document} respectively.
 *
 */
public class SmileDocumentNGrammer implements NGrammer, Serializable {

    private static final long serialVersionUID = 7284519671678121901L;
    private static final Logger LOGGER = Logger.getLogger(SmileDocumentNGrammer.class.getName());

    private int maximumFrequency;
    private int minimumFrequency;
    private int minLength;
    private int maxLength;

    /**
     * Creates a {@link SmileDocumentNGrammer} that produces all n-grams.
     *
     * @param minLength the lower boundary for the range of n-values
     * @param maxLength the upper boundary for the range of n-values
     */
    public SmileDocumentNGrammer(int minLength, int maxLength) {
        this(minLength, maxLength, 1, Integer.MAX_VALUE);
    }

    /**
     * Creates a {@link SmileDocumentNGrammer} with the given minimum n-gram frequency
     *
     * @param minLength the lower boundary for the range of n-values
     * @param maxLength the upper boundary for the range of n-values
     * @param minimumFrequency the minimum frequency an n-gram must be present in a document
     * @param maximumFrequency the maximum frequency an n-gram must be present in a document
     * @throws IllegalArgumentException if minLength is greater than maxLength
     */
    public SmileDocumentNGrammer(int minLength, int maxLength, int minimumFrequency, int maximumFrequency) {
        if (minLength > maxLength) {
            throw new IllegalArgumentException("min is greater than max");
        }
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.minimumFrequency = minimumFrequency;
        this.maximumFrequency = maximumFrequency;
    }

    /**
     * Creates a new SmileDocumentNGrammer using the provided configuration
     *
     * @param configuration
     */
    public SmileDocumentNGrammer(Configuration configuration) {
        this(configuration.getNGramMinRange(),
        		configuration.getNGramMaxRange(),
        		configuration.getMinimumTokenFrequency(),
        		configuration.getMaximumTokenFrequency());
    }

    /*
     * This will ngram the document passed in. If the document has a featureSpace already set, the ngrams used will be based off of that featureSpace instead of recalculating it.
     *
     * (non-Javadoc)
     * @see org.alvearie.nlp.text.NGramProducer#ngram(org.alvearie.nlp.text.Document)
     */
    @Override
    public void ngram(Document document) {
        Corpus corpus = new Corpus(document);
        if (document.getFeatureSpace() != null) {
            corpus.setFeatureSpace(document.getFeatureSpace());
        }
        ngram(corpus);
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.NGramProducer#ngram(org.alvearie.nlp.text.Corpus)
     */
    @Override
    public void ngram(Corpus corpus) {
        if (corpus.getFeatureSpace() == null || corpus.getFeatureSpace().isEmpty()) {
            Collection<String[]> allCorpusTokens = new ArrayList<>();
            for (Document document : corpus) {
                List<String> tokens = document.getTokens();
                if (tokens == null) {
                    throw new IllegalStateException("A given document is not tokenized. All documents in the corpus need to be tokenized.");
                }
                allCorpusTokens.add(tokens.toArray(new String[] {}));
            }
            FeatureSpace corpusFeatures = new FeatureSpace();
            // We need to pass in 0 to the Smile NGrammer otherwise, say if we pass in (2,4) we won't get anything back
            // So we generate all the ngrams then we trim to whatever the user requested
            List<List<smile.nlp.NGram>> allSmileNGrams = extractNGrams(allCorpusTokens, maxLength, 0);
            for (int i = 0; i < allSmileNGrams.size(); i++) {
                if (!(i >= minLength && i <= maxLength)) {
                    // We only get the n-grams within the requested min and max values
                    continue;
                }
                List<smile.nlp.NGram> smileNGrams = allSmileNGrams.get(i);
                for (smile.nlp.NGram smileNGram : smileNGrams) {
                    if (smileNGram.freq < minimumFrequency) {
                        continue;
                    }
                    if (smileNGram.freq > maximumFrequency) {
                        continue;
                    }
                    NGram ngram = NGram.getNGram(smileNGram.words);
                    corpus.setNGramCount(ngram, smileNGram.freq);
                    corpusFeatures.add(ngram);
                }
            }
            corpus.setFeatureSpace(corpusFeatures);
            Experiment.save("Features.txt", corpusFeatures.stream().map(f -> f.getFeature()).sorted().collect(Collectors.joining("\n")), false);

            LOGGER.info("NGrammer found " + corpusFeatures.size() + " unique n-grams in the corpus.");
        }
        // After we have found all of the n-grams in the whole corpus, filtered them by length and frequencies
        // and set the corresponding counts at the corpus level, we need to run the n-gram extraction again
        // but now at the document level in order to get the n-gram count for each Document but only for those
        // n-grams that made the cut above.
        Utils.stream(corpus.getDocuments()).forEach(
                document -> {
                    Collection<String[]> singleDocumentTokens = new ArrayList<>();
                    singleDocumentTokens.add(document.getTokens().toArray(new String[] {}));
                    List<List<smile.nlp.NGram>> documentSmileNGrams = extractNGrams(singleDocumentTokens, maxLength, 0);
                    for (int i = 0; i < documentSmileNGrams.size(); i++) {
                        List<smile.nlp.NGram> smileNGrams = documentSmileNGrams.get(i);
                        for (smile.nlp.NGram smileNGram : smileNGrams) {
                            NGram ngram = NGram.getNGram(smileNGram.words);
                            // We need to check if this is an n-gram that made the cut at the corpus level if not we skip it
                            if (!corpus.getFeatureSpace().contains(ngram)) {
                                continue;
                            }
                            document.setNGramCount(ngram, smileNGram.freq);
                        }
                    }
                });
    }

    /**
     * Extracts n-gram phrases. Cannot use SMILE's AprioriPhraseExtractor as it will remove all EnglishStopWords without
     * prejudice.
     *
     * @param sentences A collection of sentences (already split).
     * @param maxNGramSize The maximum length of n-gram
     * @param minFrequency The minimum frequency of n-gram in the sentences.
     * @return An array list of sets of n-grams. The i-th entry is the set of i-grams.
     */
    private static final List<List<smile.nlp.NGram>> extractNGrams(Collection<String[]> sentences, int maxNGramSize, int minFrequency) {
        List<Set<smile.nlp.NGram>> features = new ArrayList<>(maxNGramSize + 1);
        features.add(new HashSet<>());
        for (int n = 1; n <= maxNGramSize; n++) {
            Map<smile.nlp.NGram, Integer> candidates = new HashMap<>();
            Set<smile.nlp.NGram> feature = new HashSet<>();
            features.add(feature);
            Set<smile.nlp.NGram> feature_1 = features.get(n - 1);
            for (String[] sentence : sentences) {
                for (int i = 0; i <= sentence.length - n; i++) {
                    smile.nlp.NGram ngram = new smile.nlp.NGram(Arrays.copyOfRange(sentence, i, i + n));
                    boolean add = false;
                    if (n == 1) {
                        add = true;
                    } else {
                        smile.nlp.NGram initialGram = new smile.nlp.NGram(Arrays.copyOfRange(sentence, i, i + n - 1));
                        smile.nlp.NGram finalGram = new smile.nlp.NGram(Arrays.copyOfRange(sentence, i + 1, i + n));
                        if (feature_1.contains(initialGram) && feature_1.contains(finalGram)) {
                            add = true;
                        }
                    }
                    if (add) {
                    	// If NGram contains a stop word, don't include it.
                        add = Arrays.stream(ngram.words).noneMatch(BREAK::equalsIgnoreCase);
                    }

                    if (add) {
                        if (candidates.containsKey(ngram)) {
                            candidates.put(ngram, candidates.get(ngram) + 1);
                        } else {
                            candidates.put(ngram, 1);
                        }
                    }
                }
            }

            for (Map.Entry<smile.nlp.NGram, Integer> entry : candidates.entrySet()) {
                if (entry.getValue() >= minFrequency) {
                    smile.nlp.NGram ngram = entry.getKey();
                    if (ngram.words.length == 1 && EnglishPunctuations.getInstance().contains(ngram.words[0])) {
                        continue;
                    }

                    ngram.freq = entry.getValue();
                    feature.add(ngram);
                }
            }
        }

        List<List<smile.nlp.NGram>> results = new ArrayList<>();
        for (Set<smile.nlp.NGram> ngrams : features) {
            ArrayList<smile.nlp.NGram> result = new ArrayList<>(ngrams);
            Collections.sort(result);
            Collections.reverse(result);
            results.add(result);
        }

        return results;
    }
}
