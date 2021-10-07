/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text.processors.smile;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTextTransformer;
import org.alvearie.dream.intent.nlp.text.NGrammer;
import org.alvearie.dream.intent.nlp.text.Words;
import org.apache.log4j.Logger;

import smile.nlp.dictionary.EnglishPunctuations;
import smile.nlp.dictionary.EnglishStopWords;
import smile.nlp.normalizer.SimpleNormalizer;
import smile.nlp.stemmer.PorterStemmer;
import smile.nlp.stemmer.Stemmer;
import smile.nlp.tokenizer.SimpleTokenizer;
import smile.nlp.tokenizer.Tokenizer;

/**
 * Provides a default implementation of a {@link DocumentTextTransformer} that uses Smile to:
 * <ol>
 * <li>Normalization (converting to lower case, appliying Unicode normalization form NFKC, strip, trim, and compress
 * whitespace, remove control and formatting characters, normalize dash, double and single quotes)
 * <li>Tokenization
 * <li>Stemming
 * <li>Removing stop words
 * </ol>
 *
 */
public class SmileDocumentNormalizer implements DocumentTextTransformer {

    private static final Logger LOGGER = Logger.getLogger(SmileDocumentNormalizer.class.getName());

    private static final Pattern LOGICAL_OPERATOR_PATTERN = Pattern.compile("[<>=]+");

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+.*");

    private static final String NEGATIVE = "NEG";
    private static final String POSITIVE = "POS";

    // Pattern to find a word ending in "+". Can't use word boundary because "+" qualifies as a word boundary and doesn't
    // fire properly.
    private static final String POSITIVE_PATTERN = "([a-zA-Z0-9])\\+([^a-zA-Z])";
    // Temporary replace text to use when "+" found. This needs to be something unique that won't normalize out
    // (capitalization helps it be unique)
    private static final String POSITIVE_REPLACE_PATTERN = "$1" + POSITIVE + "$2";

    // Pattern to find a word ending in "-". Can't use word boundary because "-" qualifies as a word boundary and doesn't
    // fire properly.
    private static final String NEGATIVE_PATTERN = "([a-zA-Z0-9])\\-([^a-zA-Z])";
    // Temporary replace text to use when "-" found. This needs to be something unique that won't normalize out
    // (capitalization helps it be unique)
    private static final String NEGATIVE_REPLACE_PATTERN = "$1" + NEGATIVE + "$2";

    // remove "e.g." - trailing period optional
    private static final String EG_PATTERN = "e\\.g\\.?";
    // remove "i.e." - trailing period optional
    private static final String IE_PATTERN = "i\\.e\\.?";

    /*
     * TODO(luisg): The domain specific patterns like the ones we have below, should be configurable and not part of the general text analytics component.
     */
    /**
     * Cytogenetic abnormalities, e.g. add(1), add(1)(q22), t(9;22), etc.
     */
    private static final Pattern ABNORMALITY_PATTERN = Pattern.compile("\\w+\\(.*");

    /**
     * CD markers, e.g. CD23, CD19, CD34
     */
    private static final Pattern CD_PATTERN = Pattern.compile("cd\\d+");

    private Words stopWords;
    private Words allowedWords;
    private Words breakWords;
    private boolean breakOnSpecialCharacters;
    private int minimumTokenLength;
    private boolean stem;
    private boolean keepDigitPlaceholder;
    private boolean removeParentheticalText;
    private boolean lemmatize;

    /**
     * Creates a new {@link SmileDocumentNormalizer} without any stop or allowed words.
     */
    public SmileDocumentNormalizer() {
        this(new Words(), new Words(), new Words(), true, 1, true, false, false, false);
    }

    /**
     * Creates a new {@link SmileDocumentNormalizer} without the given stop or allowed words.
     *
     * @param stopWords words that will be removed by this normalizer
     * @param allowedWords words that will be let through by this normalizer
     * @param breakWords words that will be replaced by this normalizer with a stop word indicator
     * @param breakOnSpecialCharacters treat special characters that would normally be removed as indicators of sentence
     *        breaks that should be respected later in ngramming.
     * @param minimumTokenLength words that don't meet this threshold will be removed, words need to be at least this long
     * @param stem enable stemming
     * @param lemmatize enable lemmatization
     * @param keepDigitPlaceholder keep digits in normalized text via placeholder digits
     * @param removeParentheticalText remove all text contained in parenthesis
     */
    public SmileDocumentNormalizer(Words stopWords, Words allowedWords, Words breakWords, boolean breakOnSpecialCharacters, int minimumTokenLength, boolean stem, boolean lemmatize, boolean keepDigitPlaceholder, boolean removeParentheticalText) {
        this.stopWords = stopWords;
        this.allowedWords = allowedWords;
        this.breakWords = breakWords;
        this.breakOnSpecialCharacters = breakOnSpecialCharacters;
        this.minimumTokenLength = minimumTokenLength;
        this.stem = stem;
        this.lemmatize = lemmatize;
        this.keepDigitPlaceholder = keepDigitPlaceholder;
        this.removeParentheticalText = removeParentheticalText;
    }

    /**
     * Creates a SmileDocumentNormalizer using the provided configuration
     *
     * @param configuration
     */
    public SmileDocumentNormalizer(Configuration configuration) {
        this(configuration.getStopWords(),
                configuration.getAllowedWords(),
                configuration.getBreakWords(),
                configuration.breakOnSpecialCharacters(),
                configuration.getMinimumTokenLength(),
                configuration.stem(),
                configuration.lemmatize(),
                configuration.keepDigitPlaceholder(),
                configuration.removeParentheticalText());
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.DocumentNormalizer#normalize(org.alvearie.nlp.text.Document)
     */
    @Override
    public void processText(Document document) {
        normalizeText(document);
        normalizePunctuation(document);
        if (stem) {
            stem(document);
        }
        if (lemmatize) {
            lemmatize(document);
        }
        normalizeSpecialWords(document);
    }

    /**
     * A normalizer for processing Unicode text which:
     * <ul>
     * <li>Converts to lower case
     * <li>Applies Unicode normalization form NFKC.
     * <li>Strip, trim, normalize, and compress whitespace.
     * <li>Remove control and formatting characters.
     * <li>Normalize dash, double and single quotes.
     * </ul>
     *
     * @param document the document to normalize
     * @throws NullPointerException if the document or the text of the document are null
     */
    private void normalizeText(Document document) {
        if (document == null) {
            throw new NullPointerException("Document to normalize was null.");
        }
        if (document.getText() == null) {
            throw new NullPointerException("The text of the document to normalize was null: " + document);
        }
        String text = document.getText().toLowerCase();
        text = SimpleNormalizer.getInstance().normalize(text);
        document.setText("Normalized Text", text);
    }

    /**
     * Normalize document text to remove punctuation and replace with a space, hyphenated words are kept around. Also,
     * remove any content in parenthesis if indicated.
     *
     * @param document document to process
     */
    private void normalizePunctuation(Document document) {
        String text = document.getText();
        if (removeParentheticalText) {
            text = text.replaceAll("\\([^()]*\\)", ""); // remove parenthetical content
        }

        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("[\\p{InCombiningDiacriticalMarks}]", ""); // Remove accents from accented characters
        text = text.replaceAll(POSITIVE_PATTERN, POSITIVE_REPLACE_PATTERN);
        text = text.replaceAll(NEGATIVE_PATTERN, NEGATIVE_REPLACE_PATTERN);
        text = text.replaceAll(EG_PATTERN, NGrammer.BREAK);
        text = text.replaceAll(IE_PATTERN, NGrammer.BREAK);
        text = text.replaceAll("[().;]", " " + NGrammer.BREAK + " "); // break ngrams on parentheses, periods and semi-colons
        text = text.replaceAll("[^\\w<=>]", " ");
        text = text.replaceAll(NEGATIVE, "-");
        text = text.replaceAll(POSITIVE, "+");

        document.setText("Normalized Punctuation", text);
    }

    /**
     * Transforms the words in this document into their lemma form.
     *
     * @param document the document to run through lemmatization
     */
    private void lemmatize(Document document) {
//        DocumentTextTransformer lemmatizer = LWLemmatizer.getInstance();
//        lemmatizer.processText(document);
    }

    /**
     * Transforms the words in this document into their root form.
     *
     * @param document the document to run through stemming
     */
    private void stem(Document document) {
        List<String> documentTokens = document.getTokens();
        String[] tokens;
        if (documentTokens != null) {
            tokens = document.getTokens().toArray(new String[] {});
        } else {
            /// If this document has not been tokenized, we tokenize it here
            Tokenizer tokenizer = new SimpleTokenizer(true);
            tokens = tokenizer.split(document.getText());
        }
        List<String> stems = new ArrayList<>();
        Stemmer stemmer = new PorterStemmer();
        for (String token : tokens) {
            stems.add(stemmer.stem(token));
        }
        document.setText("Stemming", String.join(" ", stems));
    }

    /**
     * Normalizes text based on stop/allowed/break words.
     * <p>
     * <ul>
     * <li>Allowed words will all be kept - Stop words will all be removed
     * <li>Break words will be removed, and replaced by a token indicating a sentence break occurred.
     * <li>Other tokens, such as punctuation and numbers, will be normalized/removed accordingly.
     * </ul>
     *
     * @param document the document to normalize
     */
    private void normalizeSpecialWords(Document document) {
        Tokenizer tokenizer = new SimpleTokenizer(true);
        String[] tokens = tokenizer.split(document.getText());
        List<String> cleanTokens = new ArrayList<>();
        for (String token : tokens) {
            if (allowedWords.contains(token)) {
                cleanTokens.add(token);
                continue;
            }
            if (stopWords.contains(token)) {
                continue;
            }
            if (breakWords.contains(token)) {
                LOGGER.debug("Tossing: " + token);
                if (breakOnSpecialCharacters) {
                    cleanTokens.add(NGrammer.BREAK);
                }
                continue;
            }
            if (ABNORMALITY_PATTERN.matcher(token).matches()) {
                cleanTokens.add(token);
                continue;
            }
            if (CD_PATTERN.matcher(token).matches()) {
                cleanTokens.add(token);
                continue;
            }
            if (NUMBER_PATTERN.matcher(token).matches()) {
                if (keepDigitPlaceholder) {
                    cleanTokens.add("__NUMBER__");
                } else if (breakOnSpecialCharacters) {
                    cleanTokens.add(NGrammer.BREAK);
                }
                continue;
            }
            if (LOGICAL_OPERATOR_PATTERN.matcher(token).matches()) {
                if (keepDigitPlaceholder) {
                    cleanTokens.add("__LOGIC_OPERATOR__");
                }
                continue;
            }
            if (token.length() < minimumTokenLength) {
                LOGGER.debug("Tossing: " + token);
                if (breakOnSpecialCharacters) {
                    cleanTokens.add(NGrammer.BREAK);
                }
                continue;
            }
            if (EnglishStopWords.DEFAULT.contains(token)) {
                LOGGER.debug("Tossing: " + token);
                if (breakOnSpecialCharacters) {
                    cleanTokens.add(NGrammer.BREAK);
                }
                continue;
            }
            if (EnglishPunctuations.getInstance().contains(token)) {
                LOGGER.debug("Tossing: " + token);
                if (breakOnSpecialCharacters) {
                    cleanTokens.add(NGrammer.BREAK);
                }
                continue;
            }
            cleanTokens.add(token);
        }
        // If multiple consecutive stop words exist, collapse down into a single stop word marker. It makes reading the updated
        // text easier.
        List<String> cleanTokensRedux = new ArrayList<>();
        for (int i = 0; i < cleanTokens.size(); i++) {
            String token = cleanTokens.get(i);
            if (!token.equalsIgnoreCase(NGrammer.BREAK) || (i < (cleanTokens.size() - 1) && !cleanTokensRedux.isEmpty() && !cleanTokensRedux.get(cleanTokensRedux.size() - 1).equalsIgnoreCase(NGrammer.BREAK))) {
                cleanTokensRedux.add(token);
            }
        }
        cleanTokens = cleanTokensRedux;
        document.setText("Special Words", String.join(" ", cleanTokens));
    }

    /**
     * @param stem enable or disable stemming, enabled by default
     */
    public void setStem(boolean stem) {
        this.stem = stem;
    }
}
