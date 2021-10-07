/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTextTransformer;
import org.alvearie.dream.intent.nlp.text.DocumentTokenizer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentNormalizer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentTokenizer;
import org.apache.commons.lang3.StringUtils;

/**
 * A utility class containing methods that, for a given document, will get a set of tokens lost during text normalization.
 * The set is sorted alphabetically.
 *
 * If we choose not to lemmatize the text during normalization, the set of lost tokens is simply the difference between
 * the set of tokens before normalization and the set of tokens after normalization.
 *
 * On the other hand, if we choose to lemmatize during normalization, we don't count
 *
 */
public class LostTokensUtils {

    /**
     * This is not set in stone, but was found with experimentation (and can be changed if needed)
     */
    public static final int DISTANCE_THRESHOLD = 4;

    private final DocumentTextTransformer normalizer;
    private final DocumentTokenizer documentTokenizer;

    /**
     * Create an instance of the LostTokensUtils class with a given configuration for the text normalizer.
     *
     * @param configuration {@link Configuration} to use for the document normalizer.
     */
    public LostTokensUtils(Configuration configuration) {
        this.normalizer = new SmileDocumentNormalizer(configuration);
        this.documentTokenizer = new SmileDocumentTokenizer();
    }

    /**
     * Get the set of lost tokens for a given document.
     *
     * @param document A {@link Document} object containing the text and ID of the criterion.
     * @return A {@link Set} of tokens that were dropped during normalization of the given document.
     */
    public Set<String> getLostTokens(Document document) {

        Document documentCopy = new Document(document.getId(), document.getText());

        documentTokenizer.tokenize(documentCopy);

        List<String> originalTokens = documentCopy.getTokens().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        normalizer.processText(documentCopy);

        documentTokenizer.tokenize(documentCopy);

        List<String> newTokens = documentCopy.getTokens();

        return getLostTokens(originalTokens, newTokens);
    }

    /**
     * Return a sorted set of tokens that were lost during normalization of the input text.
     *
     * @param originalTokens A list of tokens from the original text.
     * @param newTokens A list of tokens from the normalized text.
     *
     * @return A {@link SortedSet} of the difference between the original tokens and new tokens.
     */
    private SortedSet<String> getLostTokens(List<String> originalTokens, List<String> newTokens) {

        SortedSet<String> originalTokenSet = new TreeSet<>(originalTokens);
        SortedSet<String> newTokenSet = new TreeSet<>(newTokens);

        SortedSet<String> difference = new TreeSet<>(originalTokenSet);
        difference.removeAll(newTokenSet);

        /* If lemmatization is done during normalization,
            use Levenshtein distance to determine whether a token is a lemma of the original token,
            hence doesn't count as a lost token */

        List<String> stringsToRemove = difference.stream()

                // Combine the tokens from the difference set above with the tokens from the normalized text
                .flatMap(token -> newTokenSet.stream().map(newToken -> Arrays.asList(token, newToken)))

                /* StringUtils.getLevenshteinDistance(CharSequence, CharSequence, int threshold)
                    only returns a positive result if 0 < distance <= threshold,
                    so any combination of tokens with a distance of less than the threshold
                    will be removed from the difference set above.

                    This means that we don't count a token like "created" as a lost token when it was lemmatized to "create"
                 */

                .filter(tokens -> StringUtils.getLevenshteinDistance(tokens.get(0), tokens.get(1), DISTANCE_THRESHOLD) > 0
                        && tokens.get(0).startsWith(tokens.get(1)))

                // Pick out the first half of the token combination above
                .map(strings -> strings.get(0))
                .collect(Collectors.toList());

        difference.removeAll(stringsToRemove);

        return difference;
    }
}
