/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A utility class to sort {@link Map}s of {@link Feature}s by their {@link Double} value.
 *
 */
class FeatureMapSorter {


    /**
     * Given a {@link Map} this method returns a {@link SortedMap} sorted by the original map's values in descending order.
     *
     * @param mapToSortByValue the map to sort by value
     * @return the {@link SortedMap} sorted by values
     */
    static SortedMap<Feature, Double> sort(Map<Feature, Double> mapToSortByValue) {
        Comparator<Feature> valueComparator = new FeatureByValueComparator(mapToSortByValue);
        SortedMap<Feature, Double> mapByValue = new TreeMap<>(valueComparator);
        mapByValue.putAll(mapToSortByValue);
        return mapByValue;
    }

    /**
     * A {@link Comparator} of {@link Feature}s based on their value in a {@link Map}. {@link Feature}s whose value is a
     * given {@link Map} is higher are ranked higher.
     *
     * @author Luis A. García
     */
    private static class FeatureByValueComparator implements Comparator<Feature> {

        private Map<Feature, Double> referenceValuesMap;

        /**
         * Creates a {@link FeatureByValueComparator} with the given {@link Map} as reference for {@link Feature}'s values.
         *
         * @param mapToSort the map to sort
         */
        FeatureByValueComparator(Map<Feature, Double> mapToSort) {
            this.referenceValuesMap = mapToSort;
        }

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Feature o1, Feature o2) {
            Double value1 = referenceValuesMap.get(o2);
            Double value2 = referenceValuesMap.get(o1);
            if (value1.equals(value2)) {
                return o1.getFeature().compareTo(o2.getFeature());
            }
            return value1.compareTo(value2);
        }
    }
}
