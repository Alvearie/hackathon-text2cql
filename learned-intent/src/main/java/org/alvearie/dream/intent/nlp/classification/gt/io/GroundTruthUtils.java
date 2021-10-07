/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.classification.gt.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.alvearie.dream.intent.nlp.text.Document;

import java.util.Set;

/**
 * Various GT utilities.
 *
 */
public class GroundTruthUtils {

    /**
     * Find {@link Document}s that belong to more than 1 category.
     *
     * @param gt the GT to check
     * @return a {@link Map} with the {@link Document}s that belong to multiple categories, or an empty {@link Map} if no
     *         duplicates are found
     */
    public static Map<Document, Collection<String>> findDuplicateCategories(Map<String, Collection<Document>> gt) {
        Map<Document, Collection<String>> dupes = new HashMap<>();
        Map<Document, Collection<String>> categoriesByDoc = buildDocumentToCategoriesMap(gt);
        Set<Entry<Document, Collection<String>>> entries = categoriesByDoc.entrySet();
        entries.removeIf(entry -> entry.getValue().size() <= 1);
        for (Entry<Document, Collection<String>> entry : entries) {
            dupes.put(entry.getKey(), entry.getValue());
        }
        return dupes;
    }

    /**
     * Given GT {@link Map}s gt1 and gt2, this method removes all entries in gt1 that correspond to an entry in gt2,
     * regardless of what their categorization.
     * <p>
     * For instance if gt1 = {"Document 1"=categoryA, "Document 2"=categoryB, "Document 3"=categoryC} and gt2 = {"Document
     * 1"=categoryZ, "Document 2"=categoryZ} this method will return gt3 = {"Document 3"=categoryC}
     *
     * @param gt1 GT to subtract from in place
     * @param gt2 GT to subtract
     */
    public static void substract(Map<String, Collection<Document>> gt1, Map<String, Collection<Document>> gt2) {
        Map<Document, Collection<String>> categoriesByDoc1 = buildDocumentToCategoriesMap(gt1);
        Map<Document, Collection<String>> categoriesByDoc2 = buildDocumentToCategoriesMap(gt2);
        Set<Document> entries2 = categoriesByDoc2.keySet();
        int i = 0;
        for (Document document : entries2) {
            Collection<String> removedFromCategories = categoriesByDoc1.remove(document);
            if (removedFromCategories != null) {
                for (String categoryToRemoveFrom : removedFromCategories) {
                    gt1.get(categoryToRemoveFrom).remove(document);
                    System.out.println("Removed document: " + document);
                    System.out.println("\tCategories in removed GT: " + removedFromCategories);
                    System.out.println("\tCategories in subtracted GT: " + categoriesByDoc2.get(document));
                    i++;
                }
            }
        }
        System.out.println("Subtracted a total of " + i + " documents.");
    }

    /**
     * Merge two ground truth maps together
     *
     * @param gt1 Ground Truth
     * @param gt2 Ground Truth to add
     * @return merged map
     */
    public static final Map<String, Collection<Document>> add(Map<String, Collection<Document>> gt1, Map<String, Collection<Document>> gt2) {
        Map<String, Collection<Document>> finalGroundTruth = new HashMap<>();
        finalGroundTruth.putAll(gt1);
        gt2.forEach((intent, documents) -> {
            finalGroundTruth.computeIfAbsent(intent, i -> new ArrayList<Document>()).addAll(documents);
        });
        return finalGroundTruth;
    }

    /**
     * Builds a {@link Map} of {@link Document} to categories. For instance if a document is in more that one category,
     * those categories will be in the value of the map for that document.
     *
     * @param gt the GT map
     * @return a map from documents to their categories
     */
    private static Map<Document, Collection<String>> buildDocumentToCategoriesMap(Map<String, Collection<Document>> gt) {
        Map<Document, Collection<String>> mapByDocs = new HashMap<>();
        Set<Entry<String, Collection<Document>>> entries = gt.entrySet();
        for (Entry<String, Collection<Document>> entry : entries) {
            String category = entry.getKey();
            Collection<Document> documents = entry.getValue();
            for (Document document : documents) {
                Collection<String> categories = mapByDocs.computeIfAbsent(document, k -> new HashSet<String>());
                categories.add(category);
            }
        }
        return mapByDocs;
    }

    /**
     * Quotes the given text if needed, that is it the text contains commas.
     *
     * @param documentText the text to quote
     * @return the quoted text
     */
    public static String quote(String documentText) {
        if (!documentText.contains(",")) {
            return documentText;
        }
        // If we are going to quote the text, we also need to escape any possible existing quotes
        return "\"" + documentText.replaceAll("\"", "\\\"") + "\"";
    }
}
