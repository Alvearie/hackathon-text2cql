/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.lemmatization;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTextTransformer;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;

//import com.ibm.langware.uimatypes.Alphabetic;
//import com.ibm.sai.utilities.uima.UimaJava5;
//import uima.tt.Lemma;

/**
 *
 * This class contains a method {@link #processText(Document)} that replaces the text of an incoming document with its
 * lemmatized version.
 *
 * The lemmatization is achieved by using a simple LanguageWare model to get the lemma for each of the words in the
 * document passed in. LanguageWare comes with a built in TokenAnnotation that splits the text into individual tokens and
 * contains the lemma for each alphabetic token. We then use the uimaFIT library to extract the tokens from the CAS,
 * from which we get the lemma for each token of the document we analysed.
 */
public final class LWLemmatizer implements DocumentTextTransformer {

    /**
     * A hyphen.
     */
    private static final String HYPHEN = "-";

    /**
     * A flag used to mark where hyphens used to be. It needs to be a token that starts with a number otherwise
     * LW will try to break it up or parse it.
     */
    private static final String HYPHEN_MARKER = "0HYPHENMARKER0";

    private static LWLemmatizer INSTANCE;

    private AnalysisEngine tae;
    private final JCas cas;

    private static final Logger LOGGER = Logger.getLogger(LWLemmatizer.class.getName());

    private LWLemmatizer() throws ResourceInitializationException, URISyntaxException, IOException, InvalidXMLException {

        // Load the required filenames from lemmatizer.properties
        Properties properties = FileUtils.loadProperties("lemmatizer.properties");

        // Get the PEAR directory and the PEAR file from the classpath
        Map<String, File> pearDirAndFile = FileUtils.getPearDirAndFile(properties);

        // Extract the PEAR file into the PEAR directory retrieved above
        LanguageWareUtils.installPear(pearDirAndFile.get("pearfile"), pearDirAndFile.get("peardir"));

        // Initialize the analysis engine from the PEAR
        tae = LanguageWareUtils.getAnalysisEngine(FileUtils.getPearDescriptor(properties));

        // Initialize the CAS from the analysis engine. We use the JCas because it is easier to work with in Java
        cas = tae.newJCas();
    }

    /**
     * We implement the lemmatizer as a singleton so we only instantiate it once.
     *
     * @return An instance of the {@link LWLemmatizer} class
     */
    public static LWLemmatizer getInstance() {
        if (INSTANCE == null) {
            synchronized (LWLemmatizer.class) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = new LWLemmatizer();
                    } catch (URISyntaxException | ResourceInitializationException | InvalidXMLException | IOException e) {
                        LOGGER.error("Error creating Lemmatizer", e);
                        INSTANCE = null;
                    }
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Convert all tokens that have lemmas into their lemma and return the modified String.
     *
     * @param text The string to return the lemmas for.
     * @return the modified string containing lemma forms of known tokens
     * @throws AnalysisEngineProcessException This exception is thrown if processing of the UIMA CAS failed.
     */
//    synchronized String lemmatizeText(String text) throws AnalysisEngineProcessException {
//        // We want to preserve hyphens, which would be discarded by the lemmatizer and treated as a token separator.
//        // So we replace them with a flag so those words are left as is by the lemmatizer
//        text = text.replace(HYPHEN, HYPHEN_MARKER);
//        // Synchronized as accessing one CAS across multiple threads is not safe. We may want to consider adding a pool of
//        // CAS'es if performance becomes a concern.
//        setUpAndProcessCas(text);
//
//        List<Alphabetic> alphabetics = new ArrayList<>();
//        UimaJava5.annotations(cas, Alphabetic.class).forEach(a -> alphabetics.add(a));
//
//        // Sort by begin/end for more efficient replacement.
//        alphabetics.sort(new Comparator<Alphabetic>() {
//            @Override
//            public int compare(Alphabetic a1, Alphabetic a2) {
//                int rc = Integer.compare(a1.getBegin(), a2.getBegin());
//                if (rc == 0) {
//                    rc = Integer.compare(a1.getEnd(), a2.getEnd());
//                    if (rc == 0) {
//                        rc = a1.getType().getName().compareTo(a2.getType().getName());
//                    }
//                }
//                return rc;
//            }
//        });
//        int end = -1;
//        for (Alphabetic alphabetic : alphabetics) {
//            if (alphabetic.getBegin() < end) {
//                // I don't believe this can ever happen, but in case alphabetics overlap, throw an exception rather than corrupting the
//                // criterion text and silently continuing.
//                throw new AnalysisEngineProcessException(new IllegalArgumentException("Unexpected overlapping lemmas for: " + text));
//            }
//            end = alphabetic.getEnd();
//        }
//
//        StringBuilder newText = new StringBuilder();
//        int curIndex = 0;
//        while (alphabetics.size() > 0) {
//            Alphabetic alphabetic = alphabetics.remove(0);
//            newText.append(text.substring(curIndex, alphabetic.getBegin()));
//            Lemma lemma = alphabetic.getLemma();
//            String lemmaText = alphabetic.getCoveredText();
//            if (lemma != null && lemma.getKey() != null && !lemma.getKey().isEmpty()) {
//                lemmaText = lemma.getKey();
//            }
//            newText.append(lemmaText);
//            curIndex = alphabetic.getEnd();
//        }
//        newText.append(text.substring(curIndex));
//        text = newText.toString();
//        // If the text contains the hyphen flag we change the flag back to the hyphen
//        text = text.replace(HYPHEN_MARKER, HYPHEN);
//        return text;
//    }

    private void setUpAndProcessCas(String text) throws AnalysisEngineProcessException {

        /* Set up the cas */
        cas.reset();
        cas.setDocumentText(text);
        cas.setDocumentLanguage("en");

        /* Run the text through the LanguageWare model. */
        tae.process(cas);
    }

    /**
     * Lemmatize a given document, i.e. given a text like "the men are walking" modify the document such that the text
     * becomes "the man is walk"
     *
     * @param document the document to lemmatize
     */
    @Override
    public void processText(Document document) {
//        try {
//            document.setText("Lemmatization", lemmatizeText(document.getText()));
//        } catch (AnalysisEngineProcessException e) {
//            LOGGER.error("Failed to process the text correctly", e);
//        }
    }
}
