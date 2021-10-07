/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.clustering.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.alvearie.dream.intent.nlp.classification.gt.io.GroundTruthReader;
import org.alvearie.dream.intent.nlp.classification.gt.io.MultiFileGroundTruthReader;
import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.clustering.Clusterer;
import org.alvearie.dream.intent.nlp.clustering.clusterers.DocumentClusteringPipeline;
import org.alvearie.dream.intent.nlp.clustering.clusterers.SmileKMeansClusterer;
import org.alvearie.dream.intent.nlp.text.Configuration;
import org.alvearie.dream.intent.nlp.text.Corpus;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.DocumentWithPrediction;
import org.alvearie.dream.intent.nlp.text.FeatureSpace;
import org.alvearie.dream.intent.nlp.text.Words;
import org.alvearie.dream.intent.nlp.text.io.CriteriaMetadataCSVDocumentReader;
import org.alvearie.dream.intent.nlp.text.io.DocumentReader;
import org.alvearie.dream.intent.nlp.text.io.SimpleTextFileDocumentReader;
import org.alvearie.dream.intent.nlp.utils.FileUtilities;

import smile.nlp.NGram;
import smile.nlp.SimpleCorpus;
import smile.nlp.collocation.BigramCollocation;
import smile.nlp.collocation.BigramCollocationFinder;
import smile.nlp.keyword.CooccurrenceKeywordExtractor;

/**
 * This is class is meant to be a runner class used for development purposes. Given a CTM criteria metadata report and
 * an existing set of clusters, it goes through the unclustered criteria in the metadata report and clusters them.
 * <p>
 * It is a CTM specific class that uses a CTM report, it will be moved out of this project and into a better location.
 *
 */
public class CriteriaClusteringRunner {

    /**
     * The main method here is just used to demonstrate this {@link Clusterer} implementation.
     *
     * @param args no args are needed
     * @throws IOException if there is a problem reading the corpus
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java CriteriaClusteringRunner <CriteriaMetadataReport.csv> <multi-file-gt-home>");
            System.exit(1);
        }
        File source = new File(args[0]);
        if (!source.exists()) {
            System.err.println("Criteria metadata file " + source + " does not exist.");
            System.exit(1);
        }

        int max = 20000;
        Corpus corpus = null;
        List<Document> gt = loadGT(args[1]);
        corpus = loadCriteria(source);


        // Then we remove the GT from the loaded Corpus
        System.out.println("Removing known GT criteria from corpus to cluster...");
        List<Document> documents = corpus.getDocuments();
        documents.removeAll(gt);
        System.out.println("Corpus size is now " + documents.size() + ".");
        if (max > documents.size()) {
            max = documents.size();
        }
        System.out.println("Will cluster the first " + max + " out of " + documents.size() + " criteria...");
        documents = new ArrayList<>(documents.subList(0, max));
        FeatureSpace featureSpace = corpus.getFeatureSpace();
        corpus = new Corpus(documents);
        corpus.setFeatureSpace(featureSpace);


        Configuration configuration = getClusteringConfiguration();

        // This is a modification of the "Rule of Thumb" method to get k which has provided good results
        int k = (int) Math.sqrt(max / 2.0) * 4;
        System.out.println("Will cluster using k = " + k + "...");
        Clusterer clusterer = new SmileKMeansClusterer(k);
//        Clusterer clusterer = new SmileDBScanClusterer();
//        Clusterer clusterer = new SmileGMeansClusterer(k);
        DocumentClusteringPipeline clustering = new DocumentClusteringPipeline(clusterer, configuration);
        List<Cluster> clusters = clustering.cluster(corpus);

        clusters.removeIf(cluster -> cluster.getDocuments().size() < 3);
        Map<String, List<Cluster>> knownClusters = new HashMap<>();
        List<Cluster> unknownClusters = new ArrayList<>();
        for (Cluster cluster : clusters) {
            List<String> classifications = new ArrayList<>();
            for (Document document : cluster.getDocuments()) {
                DocumentWithPrediction classifiedDocument = (DocumentWithPrediction) document;
                String documentClassification = classifiedDocument.getPrediction();
                classifications.add(documentClassification);
            }
            Map<String, Long> counts = classifications.stream().collect(Collectors.groupingBy(confidence -> confidence, Collectors.counting()));
            Set<Entry<String, Long>> entriesCount = counts.entrySet();
            double maxFrequency = 0.0;
            String clusterName = null;
            for (Entry<String, Long> countEntry : entriesCount) {
                double frequency = (double) countEntry.getValue() / cluster.getDocuments().size();
                if (frequency > maxFrequency) {
                    maxFrequency = frequency;
                    clusterName = countEntry.getKey();
                }
            }
            // Tested various majority representations 1 out of 2, 2 out of 3, 3 out of 5 - 1/2 (0.5) seems to work well
            if (maxFrequency > (1.0 / 2.0)) {
                knownClusters.computeIfAbsent(clusterName, c -> new ArrayList<>()).add(cluster);
            } else {
                unknownClusters.add(cluster);
            }
        }

        File clustersHome = new File(source.getParent(), "Clusters-" + DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss").format(LocalDateTime.now()));
        File knownClustersHome = new File(clustersHome, "PotentialNewGT");
        knownClustersHome.mkdirs();

        System.out.println("Likely known clusters: " + (k - unknownClusters.size()));
        System.out.println("---------------------");
        Set<Entry<String, List<Cluster>>> knownClusterEntries = knownClusters.entrySet();
        for (Entry<String, List<Cluster>> entry : knownClusterEntries) {
            File knownClusterFile = new File(knownClustersHome, entry.getKey() + ".txt");
            List<String> fileContents = new ArrayList<>();
            String msg = "Learned Intent: " + entry.getKey() + "\n";
            System.out.println(msg);
            fileContents.add(msg);
            for (Cluster cluster : entry.getValue()) {
                System.out.println(cluster);
                fileContents.add(cluster.toString());
            }
            Files.write(knownClusterFile.toPath(), fileContents);
            System.out.println("*******************");
        }

        System.out.println("Likely unknown clusters: " + unknownClusters.size());
        System.out.println("---------------------");
        File unknownClusterFile = new File(clustersHome, "unknown-clusters.txt");
        List<String> fileContents = new ArrayList<>();
        for (Cluster cluster : unknownClusters) {
            System.out.println(cluster);
            fileContents.add(cluster.toString());
            SimpleCorpus smileCorpus = new SimpleCorpus();
            for (Document document : cluster.getDocuments()) {
                smileCorpus.add(document.getId(), null, document.getText());
            }
            BigramCollocationFinder instance = new BigramCollocationFinder(1);
            BigramCollocation[] result = null;
            try {
                result = instance.find(smileCorpus, 5);
            } catch (NullPointerException e) {
                // In some cases if it's not possible to find the bi-grams the method may throw an NPE
                // instead of returning an empty array, so we catch it and move on without top-bigrams
            }
            if (result == null) {
                String msg = "No top bi-grams.";
                System.out.println(msg);
                fileContents.add(msg);
            } else {
                String msg = "Top bi-grams:";
                System.out.println(msg);
                fileContents.add(msg);
                for (int i = 0; i < result.length; i++) {
                    System.out.println(result[i]);
                    fileContents.add(result[i].toString());
                }
                System.out.println("");
                fileContents.add("");
            }

            CooccurrenceKeywordExtractor keywordExtractor = new CooccurrenceKeywordExtractor();
            String allDocumentsText = String.join("\n", cluster.getDocuments().stream().map(Document::getText).collect(Collectors.toList()));
            ArrayList<NGram> keywords = keywordExtractor.extract(allDocumentsText);
            String msg = "Keywords: " + keywords + "\n";
            System.out.println(msg);
            fileContents.add(msg);

        }
        Files.write(unknownClusterFile.toPath(), fileContents);
    }

    /**
     * Load the criteria from the given source file.
     *
     * @param source the source file
     * @return the Corpus with the loaded criteria
     * @throws IOException if there is a problem reading the criteria from the file
     */
    private static Corpus loadCriteria(File source) throws IOException {
        Corpus corpus;
        System.out.println("Existing model was not found.");
        System.out.println("Reading all criteria from corpus...");
        DocumentReader reader = null;
        if (source.getName().endsWith(".csv")) {
            reader = new CriteriaMetadataCSVDocumentReader(source);
        } else if (source.getName().endsWith(".txt")) {
            reader = new SimpleTextFileDocumentReader(source);
        } else {
            throw new IllegalArgumentException("Unrecognized file format: " + source.getName());
        }
        List<Document> documents = reader.read();
        System.out.println("Read: " + documents.size() + " unique criteria");
        corpus = new Corpus(documents);
        return corpus;
    }

    /**
     * Loads the GT from the given directory
     *
     * @param gtDirectoryName the name of the GT directory
     * @return the list of GT {@link Document}s
     * @throws IOException if there is a problem reading the GT files
     */
    private static List<Document> loadGT(String gtDirectoryName) throws IOException {
        File gtDirectory = new File(gtDirectoryName);
        if (!gtDirectory.exists()) {
            System.err.println("The GT directory " + gtDirectoryName + " does not exist. Clustering will continue over the whole corpus.");
            return new ArrayList<>();
        }
        System.out.println("Reading known criteria from ground truth...");
        GroundTruthReader gtReader = new MultiFileGroundTruthReader(gtDirectory);
        Map<String, Collection<Document>> gtByIntent = gtReader.read();
        List<Document> gt = new ArrayList<>();
        Collection<Collection<Document>> criteriaGT = gtByIntent.values();
        for (Collection<Document> criteriaGTForIntent : criteriaGT) {
            for (Document criterionForIntent : criteriaGTForIntent) {
                gt.add(criterionForIntent);
            }
        }
        System.out.println("Read: " + gt.size() + " GT criteria");
        return gt;
    }

    /**
     * Builds the configuration for a clustering run.
     *
     * @return the configuration
     */
    public static Configuration getClusteringConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setLemmatize(true);
        configuration.setStem(false);
        try {
            configuration.setStopWords(new Words(FileUtilities.getFileWithPathOrClasspath("clusteringStopWords.txt")));
        } catch (IOException e) {
            System.err.println("Error loading stop words.");
        }
        try {
            configuration.setAllowedWords(new Words(FileUtilities.getFileWithPathOrClasspath("clusteringAllowWords.txt")));
        } catch (IOException e) {
            System.err.println("Error loading stop words.");
        }
        configuration.setMinimumTokenFrequency(3);
        return configuration;
    }
}
