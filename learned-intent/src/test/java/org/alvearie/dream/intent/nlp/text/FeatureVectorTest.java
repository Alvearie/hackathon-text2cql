/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.text;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;


public class FeatureVectorTest {
    /**
     *
     */
    private static final double MARGIN_OF_ERROR = 0.0001;

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.text.FeatureVector#add(org.alvearie.dream.intent.nlp.text.FeatureVector)}.
     */
    @Test
    public void testAdd() {
        FeatureVector v1 = new FeatureVector();
        v1.addFeature(NGram.getNGram("the"), 2.0);
        v1.addFeature(NGram.getNGram("cat"), 1.0);
        v1.addFeature(NGram.getNGram("in"), 1.0);
        v1.addFeature(NGram.getNGram("hat"), 1.0);
        FeatureVector v2 = new FeatureVector();
        v2.addFeature(NGram.getNGram("the"), 2.0);
        v2.addFeature(NGram.getNGram("dog"), 1.0);
        v2.addFeature(NGram.getNGram("in"), 1.0);
        v2.addFeature(NGram.getNGram("hat"), 1.0);
        FeatureVector v3 = v1.add(v2);
        assertEquals(4.0, v3.getValue(NGram.getNGram("the")), MARGIN_OF_ERROR);
        assertEquals(1.0, v3.getValue(NGram.getNGram("cat")), MARGIN_OF_ERROR);
        assertEquals(1.0, v3.getValue(NGram.getNGram("dog")), MARGIN_OF_ERROR);
        assertEquals(2.0, v3.getValue(NGram.getNGram("in")), MARGIN_OF_ERROR);
        assertEquals(2.0, v3.getValue(NGram.getNGram("hat")), MARGIN_OF_ERROR);
    }

    /**
     *
     */
    @Test
    public void testIsEmpty() {
        FeatureVector v1 = new FeatureVector();
        assertTrue(v1.isEmpty());
    }

    /**
     *
     */
    @Test
    public void testIsZeroVector() {
        FeatureVector vector = new FeatureVector();
        vector.addFeature(NGram.getNGram("token1"), 0.0);
        vector.addFeature(NGram.getNGram("token2"), 0.0);
        vector.addFeature(NGram.getNGram("token3"), 0.0);
        vector.addFeature(NGram.getNGram("token4"), 0.0);
        assertTrue(vector.isZeroVector());

        vector = new FeatureVector();
        vector.addFeature(NGram.getNGram("token1"), .0);
        vector.addFeature(NGram.getNGram("token2"), .0);
        vector.addFeature(NGram.getNGram("token3"), .0);
        vector.addFeature(NGram.getNGram("token4"), .0);
        assertTrue(vector.isZeroVector());

        vector = new FeatureVector();
        vector.addFeature(NGram.getNGram("token1"), 0.0);
        vector.addFeature(NGram.getNGram("token2"), 0.0);
        vector.addFeature(NGram.getNGram("token3"), 0.0);
        vector.addFeature(NGram.getNGram("token4"), 0.000000000000000000000000000000000001);
        assertFalse(vector.isZeroVector());

    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.text.FeatureVector#sorted()}.
     */
    @Test
    public void testSorted() {
        FeatureVector v1 = new FeatureVector();
        v1.addFeature(NGram.getNGram("the"), 2.0);
        v1.addFeature(NGram.getNGram("cat"), 19.0);
        v1.addFeature(NGram.getNGram("in"), 3.0);
        v1.addFeature(NGram.getNGram("hat"), 5.0);
        // The natural order of a vector is insertion order, but it can be sorted by magnitude descending using sorted()
        FeatureVector sorted = v1.sorted();
        List<Feature> sortedFeatures = new ArrayList<>(sorted.getFeatures());
        assertEquals(NGram.getNGram("cat"), sortedFeatures.get(0));
        assertEquals(NGram.getNGram("hat"), sortedFeatures.get(1));
        assertEquals(NGram.getNGram("in"), sortedFeatures.get(2));
        assertEquals(NGram.getNGram("the"), sortedFeatures.get(3));
    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.text.FeatureVector#cosineSimilarity()}.
     */
    @Test
    public void testCosineSimilarity() {
        FeatureVector v1 = new FeatureVector();
        v1.addFeature(NGram.getNGram("the"), 2.0);
        v1.addFeature(NGram.getNGram("cat"), 19.0);
        v1.addFeature(NGram.getNGram("in"), 3.0);
        v1.addFeature(NGram.getNGram("hat"), 5.0);

        FeatureVector v2 = new FeatureVector();
        v2.addFeature(NGram.getNGram("foo"), 2.0);
        v2.addFeature(NGram.getNGram("bar"), 19.0);
        v2.addFeature(NGram.getNGram("boo"), 3.0);
        v2.addFeature(NGram.getNGram("far"), 5.0);

        assertEquals(1.0, v1.cosineSimilarity(v1), MARGIN_OF_ERROR);
        assertEquals(0.0, v1.cosineSimilarity(v2), MARGIN_OF_ERROR);
    }

    /**
     * Test method for {@link org.alvearie.dream.intent.nlp.text.FeatureVector#toDenseVector(List)}.
     */
    @Test
    public void testToDenseAndToSparseVector() {
        NGram a = NGram.getNGram("a");
        NGram e = NGram.getNGram("e");
        NGram i = NGram.getNGram("i");
        NGram o = NGram.getNGram("o");
        NGram u = NGram.getNGram("u");

        FeatureVector sparse = new FeatureVector();
        sparse.addFeature(a, 1.0);
        sparse.addFeature(e, 2.0);
        sparse.addFeature(u, 3.0);

        List<Feature> featureSpace = Arrays.asList(a, e, i, o, u);

        FeatureVector expectedDense = new FeatureVector();
        expectedDense.addFeature(a, 1.0);
        expectedDense.addFeature(e, 2.0);
        expectedDense.addFeature(i, 0.0);
        expectedDense.addFeature(o, 0.0);
        expectedDense.addFeature(u, 3.0);

        FeatureVector denseVector = sparse.toDenseVector(featureSpace);
        assertEquals(expectedDense, denseVector);

        // This feature space is missing a letter so it won't be equal to the expected dense vector
        List<Feature> badFeatureSpace = Arrays.asList(a, e, o, u);
        assertNotEquals(expectedDense, sparse.toDenseVector(badFeatureSpace));

        // Now we turn the dense back into a sparse
        assertEquals(sparse, denseVector.toSparseVector());
    }

    /**
     *
     */
    @Test
    public void testCentroidDense() {
        NGram feature1 = NGram.getNGram("token1");
        NGram feature2 = NGram.getNGram("token2");
        NGram feature3 = NGram.getNGram("token3");
        NGram feature4 = NGram.getNGram("token4");

        FeatureVector vector1 = new FeatureVector();
        vector1.addFeature(feature1, 1.0);
        vector1.addFeature(feature2, 0.0);
        vector1.addFeature(feature3, 0.1);
        vector1.addFeature(feature4, 1.0);

        FeatureVector vector2 = new FeatureVector();
        vector2.addFeature(feature1, 1.0);
        vector2.addFeature(feature2, 100.0);
        vector2.addFeature(feature3, 0.2);
        vector2.addFeature(feature4, 2.0);

        FeatureVector vector3 = new FeatureVector();
        vector3.addFeature(feature1, 1.0);
        vector3.addFeature(feature2, 50.0);
        vector3.addFeature(feature3, 0.3);
        vector3.addFeature(feature4, 3.0);

        FeatureVector centroid = FeatureVector.centroid(Arrays.asList(vector1, vector2, vector3), Arrays.asList(feature1, feature2, feature3, feature4));
        assertEquals(1.0, centroid.getValue(feature1), MARGIN_OF_ERROR);
        assertEquals(50.0, centroid.getValue(feature2), MARGIN_OF_ERROR);
        assertEquals(0.2, centroid.getValue(feature3), MARGIN_OF_ERROR);
        assertEquals(2.0, centroid.getValue(feature4), MARGIN_OF_ERROR);
    }

    /**
     *
     */
    @Test
    public void testCentroidSparse() {
        NGram feature1 = NGram.getNGram("token1");
        NGram feature2 = NGram.getNGram("token2");
        NGram feature3 = NGram.getNGram("token3");
        NGram feature4 = NGram.getNGram("token4");

        FeatureVector vector1 = new FeatureVector();
        vector1.addFeature(feature1, 1.0);
        vector1.addFeature(feature2, 0.0);
        vector1.addFeature(feature3, 0.6);

        FeatureVector vector2 = new FeatureVector();
        vector2.addFeature(feature1, 1.0);
        vector2.addFeature(feature2, 99.0);
        vector2.addFeature(feature4, 6.0);

        FeatureVector vector3 = new FeatureVector();
        vector3.addFeature(feature1, 1.0);
        vector3.addFeature(feature3, 0.3);
        vector3.addFeature(feature4, 3.0);

        FeatureVector centroid = FeatureVector.centroid(Arrays.asList(vector1, vector2, vector3), Arrays.asList(feature1, feature2, feature3, feature4));
        System.out.println(centroid);
        assertEquals(1.0, centroid.getValue(feature1), MARGIN_OF_ERROR);
        assertEquals(33.0, centroid.getValue(feature2), MARGIN_OF_ERROR);
        assertEquals(0.3, centroid.getValue(feature3), MARGIN_OF_ERROR);
        assertEquals(3.0, centroid.getValue(feature4), MARGIN_OF_ERROR);
    }

    @Test
    public void testToMatrixEmptyList() {
        assertArrayEquals(new double[][] {}, FeatureVector.toMatrix(new ArrayList<>()));
    }

}
