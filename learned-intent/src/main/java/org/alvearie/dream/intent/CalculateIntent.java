package org.alvearie.dream.intent;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.alvearie.dream.intent.nlp.classification.Classification;
import org.alvearie.dream.intent.nlp.classification.Classifier;
import org.alvearie.dream.intent.nlp.classification.ClassifierManager;

//run ./mvnw package
//
//run ./mvnw compile quarkus:dev

@Path("/")
public class CalculateIntent {
	public static final String CRITERIA_CLASSIFICATION_MODEL = "criteria-classification.model";

	@GET
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String calculateIntent(
			@QueryParam("text") String text) {
		org.alvearie.dream.intent.nlp.text.Document nlpDocument = new org.alvearie.dream.intent.nlp.text.Document(text);
		List<Classification> classifications;
		synchronized (getClassifier()) {
			classifications = getClassifier().classify(nlpDocument);
			System.err.println(classifications);
			return classifications.get(0).getCategory();
		}
	}

//	@GET
//	@POST
//	@Produces(MediaType.TEXT_PLAIN)
//	public String updateIntent(@QueryParam("text") String text, @QueryParam("intent") String intent) {
//
//		// Load second GT file and merge
//		Map<String, Collection<Document>> groundTruth = new HashMap<>();
//		try {
//		GroundTruthReader reader = new CSVGroundTruthReader(new File(TrainCriteriaClassification.class
//				.getResource(TrainCriteriaClassification.CRITERIA_CLASSIFICATION_GROUND_TRUTH_USER_CSV).getFile()));
//			groundTruth = reader.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		Collection<Document> criteriaForIntent=groundTruth.computeIfAbsent(intent, i -> new HashSet<Document>());
//		if (!criteriaForIntent.stream().anyMatch(d -> d.getOriginalText().equals(text))) {
//			criteriaForIntent.add(new org.alvearie.nlp.text.Document(text));
//		}
//
////		File destinationFile = new File(TrainCriteriaClassification.CRITERIA_CLASSIFICATION_GROUND_TRUTH_USER_CSV);
//		URL userCSVURL = TrainCriteriaClassification.class.getResource(TrainCriteriaClassification.CRITERIA_CLASSIFICATION_GROUND_TRUTH_USER_CSV);
//		File destinationFile = new File(userCSVURL.getFile());
//		
//		// Sort GT by classification
//		groundTruth = new TreeMap<>(groundTruth);
//		List<String> csvFileContents = new ArrayList<>();
//		csvFileContents.add(String.join(",", CSVGroundTruthReader.CSV_FILE_HEADER));
//		Set<Entry<String, Collection<Document>>> entries = groundTruth.entrySet();
//		for (Entry<String, Collection<Document>> entry : entries) {
//			String classification = entry.getKey();
//			Collection<Document> documents = entry.getValue();
//			List<String> documentsText = new ArrayList<>(Document.toStringCollection(documents));
//			// Sort documents alphabetically
//			Collections.sort(documentsText);
//			for (String documentText : documentsText) {
//				String quotedText = quote(documentText);
//				csvFileContents.add(quotedText + "," + classification);
//			}
//		}
//		try {
//			Files.write(destinationFile.toPath(), csvFileContents);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("Created GT CSV file with a total of " + (csvFileContents.size() - 1) + " documents: "
//				+ destinationFile);
//
//		try {
//			TrainCriteriaClassification.main(null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return "";
//	}

//	/**
//	 * Quotes the given text if needed, that is it the text contains commas.
//	 * 
//	 * @param documentText the text to quote
//	 * @return the quoted text
//	 */
//	private String quote(String documentText) {
//		if (!documentText.contains(",")) {
//			return documentText;
//		}
//		// If we are going to quote the text, we also need to escape any possible
//		// existing quotes
//		return "\"" + documentText.replaceAll("\"", "\\\"") + "\"";
//	}

	private Classifier getClassifier() {
		if (classifier == null) {
			synchronized (this) {
				if (classifier == null) {
					try (InputStream inputStream = getClass()
							.getResourceAsStream("/" + CRITERIA_CLASSIFICATION_MODEL)) {
						// Use model generated previously during ingestion
						classifier = ClassifierManager.load(inputStream);
					} catch (Throwable t) {
						classifier = null;
						t.printStackTrace();
						System.err.println("Disabling criteria classification due to initialization failure");
					}
				}
			}
		}
		return classifier;
	}

	private Classifier classifier = null;

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public static void main(String[] args) {
		String criterion = "18 years of age or older";
		String intent = new CalculateIntent().calculateIntent(criterion);

		System.err.println(intent + " ---> " + criterion);

//		new CalculateIntent().updateIntent(criterion, "ageGender");
//
//		intent = new CalculateIntent().calculateIntent(criterion);
//		System.err.println(intent + " ---> " + criterion);
	}

}