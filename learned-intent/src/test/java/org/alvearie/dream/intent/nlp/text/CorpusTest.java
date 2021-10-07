/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.clustering.Clusterer;
import org.alvearie.dream.intent.nlp.clustering.clusterers.DocumentClusteringPipeline;
import org.alvearie.dream.intent.nlp.clustering.clusterers.SmileKMeansClusterer;
import org.alvearie.dream.intent.nlp.clustering.validation.AdjustedRandIndex;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;


public class CorpusTest {

    private static final double MARGIN_OF_ERROR = 0.000001;

    @Test
    public void testSerializeCorpus() throws IOException {
        Document document1 = new Document("Age: 18 to 100 years");
        Document document2 = new Document("Absolute neutrophil count >= 1500/µL");
        Document document3 = new Document("Absolute neutrophil count >= 1600/µL");
        Document document4 = new Document("Creatinine within the normal institutional limits");
        Document document5 = new Document("Creatinine within normal institutional limits");
        Document document6 = new Document("Creatinine is within normal institutional limits");
        Corpus corpus = new Corpus(document1, document2, document3, document4, document5, document6);
        Configuration configuration = new Configuration();
        configuration.setMinimumTokenFrequency(1);
        Clusterer clusterer = new DocumentClusteringPipeline(new SmileKMeansClusterer(3), configuration);
        List<Cluster> clusters = clusterer.cluster(corpus);

        // Now that we have a vectorized corpus we serialize it
        Path model = Files.createTempFile("corpus", ".model");
        model.toFile().deleteOnExit();

        try (OutputStream modelStream = new FileOutputStream(model.toFile())) {
            SerializationUtils.serialize(corpus, modelStream);
            System.out.println("Serialized model " + model);
        }

        // Now we re-load the saved model and re-cluster it. The clustering pipeline will not
        // run vectorization again, it should use the existing deserialized vectors, but we should
        // still get the same results.
        try (InputStream modelStream = new FileInputStream(model.toFile())) {
            corpus = (Corpus) SerializationUtils.deserialize(modelStream);
            System.out.println("Deserialized model " + model);
        }
        clusterer = new DocumentClusteringPipeline(3);
        List<Cluster> clustersFromSavedModel = clusterer.cluster(corpus);

        AdjustedRandIndex similarityScorer = new AdjustedRandIndex();
        double similarityScore = similarityScorer.calculate(clusters, clustersFromSavedModel);
        assertEquals(1.0, similarityScore, MARGIN_OF_ERROR);
    }

}
