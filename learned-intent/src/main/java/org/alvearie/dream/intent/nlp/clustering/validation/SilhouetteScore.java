
package org.alvearie.dream.intent.nlp.clustering.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alvearie.dream.intent.nlp.clustering.Cluster;
import org.alvearie.dream.intent.nlp.text.Document;
import org.alvearie.dream.intent.nlp.text.Feature;
import org.alvearie.dream.intent.nlp.text.FeatureVector;

import smile.math.distance.EuclideanDistance;

/**
 * The Silhouette Score of a set of clusters is an internal evaluation measure, that is it does not require Ground Truth
 * and the evaluation is performed using the model alone.
 * <p>
 * Given some {@link Cluster}s a higher Silhouette Coefficient score relates to a model with better defined clusters.
 * The Silhouette Coefficient is defined for each {@link Cluster} and is composed of two scores:
 * <p>
 * <ul>
 * <li>a: The mean distance between a sample and all other points in the same class.
 * <li>b: The mean distance between a sample and all other points in the next nearest cluster.
 * </ul>
 * <p>
 * The Silhouette Coefficient, <code>S</code> a single {@link Cluster} is then given as: <blockquote>
 *
 * <pre>
 * S = b - a / max(a, b)
 * </pre>
 *
 * </blockquote>
 * <p>
 * The Silhouette Coefficient for a set of {@link Cluster}s, or Silhouette Score, is given as the mean of the Silhouette
 * Coefficient for each {@link Cluster}.
 * <p>
 * It is possible to try to optimize the number of clusters by maximizing the Silhouette Score for various K sizes as
 * described <a href=
 * "https://scikit-learn.org/stable/auto_examples/cluster/plot_kmeans_silhouette_analysis.html#sphx-glr-auto-examples-cluster-plot-kmeans-silhouette-analysis-py">
 * here</a>.
 * <p>
 * After this score is calculated it is possible to retrieve the closest cluster for each cluster, and their mean
 * intra-document distances.
 *
 */
public class SilhouetteScore {

    private EuclideanDistance distance;
    private List<Feature> featureSpace;

    private Map<Cluster, Cluster> closestClusters;
    private Map<Cluster, Double> meanIntraDocumentDistances;

    /**
     * Creates a new {@link SilhouetteScore} class.
     */
    public SilhouetteScore() {
        this(null);
    }

    /**
     * Creates a new {@link SilhouetteScore} class.
     * <p>
     * If using sparse vectors, the feature space is needed to convert to dense vectors, so all vectors are the same length
     * when calculating their distances.
     *
     * @param featureSpace
     */
    public SilhouetteScore(List<Feature> featureSpace) {
        distance = new EuclideanDistance();
        this.featureSpace = featureSpace;
        closestClusters = new HashMap<>();
        meanIntraDocumentDistances = new HashMap<>();
    }

    /**
     * Calculates the {@link SilhouetteScore} for the given {@link Cluster}s. Typically the {@link List} of {@link Cluster}s
     * would be the result of a clustering operation.
     *
     * @param clusters the clusters to calculate the score for
     * @return the score
     * @throws IllegalArgumentException if the vector lengths across documents are different
     */
    public double calculate(List<Cluster> clusters) {
        List<Double> silhouetteCoefficients = new ArrayList<Double>();
        for (Cluster cluster : clusters) {
            Cluster closestCluster = getClosestCluster(cluster, clusters);
            if (closestCluster != null) {
                closestClusters.put(cluster, closestCluster);
            }
            List<Double> intraDocumentDistances = new ArrayList<>();
            List<Double> silhouetteCoefficientForDocuments = new ArrayList<>();
            for (Document document : cluster.getDocuments()) {
                double a = 0;
                double b = 0;
                a = getMeanIntraDocumentDistance(document, cluster.getDocuments());
                b = getMeanIntraDocumentDistance(document, closestCluster.getDocuments());

                double silhouetteCoefficientForDocument = (b - a) / Math.max(a, b);
                silhouetteCoefficientForDocuments.add(silhouetteCoefficientForDocument);
                intraDocumentDistances.add(a);
            }
            double meanIntradocumentDistanceForCluster = intraDocumentDistances.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0);
            meanIntraDocumentDistances.put(cluster, meanIntradocumentDistanceForCluster);

            double clusterSilouhetteCoefficient = silhouetteCoefficientForDocuments.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0);

            silhouetteCoefficients.add(clusterSilouhetteCoefficient);
        }
        double silouhetteScore = silhouetteCoefficients.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .getAsDouble();

        return silouhetteScore;
    }

    /**
     * Calculates the average distance between the given {@link Document} and the rest of the {@link Document}s given in a
     * {@link List}.
     *
     * @param document the subject document
     * @param documents the other documents to calculate the distance to
     * @return the mean distance between the given document and all others
     * @throws IllegalArgumentException if the vector lengths across documents are different
     */
    private double getMeanIntraDocumentDistance(Document document, List<Document> documents) {
        double meanDistance = 0;
        List<Double> intraDocumentDistances = new ArrayList<>();
        FeatureVector vector = document.getVector();
        if (featureSpace != null) {
            // If the feature space is not null that means we are using sparse vectors that need to be
            // normalized to dense vectors so they are all the same size. We'll do the same to the other vectors.
            vector = vector.toDenseVector(featureSpace);
        }
        double[] subjectDocumentVector = vector.toArray();
        for (Document otherDocument : documents) {
            if (document == otherDocument) {
                continue;
            }
            FeatureVector otherVector = otherDocument.getVector();
            if (featureSpace != null) {
                otherVector = otherVector.toDenseVector(featureSpace);
            }
            double[] otherDocumentVector = otherVector.toArray();
            if (subjectDocumentVector.length != otherDocumentVector.length) {
                String msg = "Intra-document distances cannot be calculated because the document feature vectors are of different sizes: %s, %s. This may happen if sparse vectors are being used and no feature space was provided at construction time.";
                msg = String.format(msg, subjectDocumentVector.length, otherDocumentVector.length);
                throw new IllegalArgumentException(msg);
            }
            intraDocumentDistances.add(distance.d(subjectDocumentVector, otherDocumentVector));
        }
        meanDistance = intraDocumentDistances.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        return meanDistance;
    }

    /**
     * Gets the closest cluster to the given {@link Cluster} from the given {@link List} of {@link Cluster}s.
     *
     * @param cluster the subject cluster
     * @param clusters the other clusters
     * @return the closest cluster to the given cluster, measuring at the centroids
     */
    private Cluster getClosestCluster(Cluster cluster, List<Cluster> clusters) {
        if (cluster.getCentroid() == null) {
            throw new IllegalArgumentException("The subject cluster does not have a centroid. This closest cluster function is performed by centroids.");
        }
        double closestClusterDistance = Double.MAX_VALUE;
        Cluster closestCluster = null;
        double[] thisClusterCentroid = cluster.getCentroid().toArray();
        for (Cluster otherCluster : clusters) {
            if (cluster == otherCluster) {
                continue;
            }
            FeatureVector centroid = otherCluster.getCentroid();
            if (centroid == null) {
                throw new IllegalArgumentException("Cluster " + otherCluster.getId() + " does not have a centroid. This closest cluster function is performed by centroids.");
            }
            double[] otherClusterCentroid = centroid.toArray();
            double intraClusterDistance = distance.d(thisClusterCentroid, otherClusterCentroid);
            if (intraClusterDistance < closestClusterDistance) {
                closestClusterDistance = intraClusterDistance;
                closestCluster = otherCluster;
            }
        }
        return closestCluster;
    }

    /**
     * @return a {@link Map} from each cluster to its closest cluster
     */
    public Map<Cluster, Cluster> getClosestClusters() {
        return closestClusters;
    }

    /**
     * @return a {@link Map} from each cluster to its mean intra document distance
     */
    public Map<Cluster, Double> getMeanIntraDocumentDistances() {
        return meanIntraDocumentDistances;
    }
}
