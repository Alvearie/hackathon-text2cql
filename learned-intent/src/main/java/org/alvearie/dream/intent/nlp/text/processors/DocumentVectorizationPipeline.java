/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.alvearie.dream.intent.nlp.text.processors;

import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentTextTransformer;
import org.alvearie.dream.intent.nlp.text.DocumentTokenizer;
import org.alvearie.dream.intent.nlp.text.DocumentVectorizer;
import org.alvearie.dream.intent.nlp.text.NGrammer;
import org.alvearie.dream.intent.nlp.text.Utils;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentNGrammer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentNormalizer;
import org.alvearie.dream.intent.nlp.text.processors.smile.SmileDocumentTokenizer;
import org.alvearie.dream.intent.nlp.utils.StopWatch;

/**
 * A default vectorization flow which includes normalization, tokenization, n-gramming, tf-idf vectorization.
 * <p>
 * If the given corpus contains {@link Document}s that are already vectorized, this pipeline will not re-vectorize.
 *
 */
public class DocumentVectorizationPipeline implements DocumentVectorizer {

    private DocumentVectorizer vectorizer;
    private DocumentVectorizer bow;
    private NGrammer ngrammer;
    private DocumentTextTransformer normalizer;
    private DocumentTokenizer tokenizer;

    /**
     * Creates a {@link DocumentVectorizationPipeline}.
     *
     * @param configuration the NLP text processing configuration, if null the default configuration will be used
     */
    public DocumentVectorizationPipeline(Configuration configuration) {
        if (configuration == null) {
            configuration = Configuration.getDefault();
        }
        vectorizer = new TFIDFVectorizer();
        ngrammer = new SmileDocumentNGrammer(configuration);
        tokenizer = new SmileDocumentTokenizer();
        normalizer = new SmileDocumentNormalizer(configuration);
        bow = new DocumentBoWVectorizer();
    }

    /*
     * (non-Javadoc)
     * @see org.alvearie.nlp.text.DocumentVectorizer#vectorize(org.alvearie.nlp.text.Corpus)
     */
    @Override
    public void vectorize(Corpus corpus) {
        StopWatch sw = StopWatch.start();

        // If the Corpus already has a feature space it means it's already vectorized so we skip vectorization
        if (corpus.getFeatureSpace() != null) {
            return;
        }

        System.out.println("Normalizing...");
        Utils.stream(corpus.getDocuments()).forEach(normalizer::processText);

        System.out.println("Tokenizing...");
        Utils.stream(corpus.getDocuments()).forEach(tokenizer::tokenize);

        System.out.println("NGramming...");
        ngrammer.ngram(corpus);

        System.out.println("Calculating BOW vector...");
        Utils.stream(corpus.getDocuments()).forEach(bow::vectorize);

        System.out.println("Calculating TF-IDF vector...");
        vectorizer.vectorize(corpus);

        System.out.println("Done! Vectorized " + corpus.size() + " documents in " + sw.stop());
    }

    /* (non-Javadoc)
     * @see org.alvearie.nlp.text.DocumentVectorizer#vectorize(org.alvearie.nlp.text.Document)
     */
    @Override
    public void vectorize(Document document) {
        vectorize(new Corpus(document));
    }
}
