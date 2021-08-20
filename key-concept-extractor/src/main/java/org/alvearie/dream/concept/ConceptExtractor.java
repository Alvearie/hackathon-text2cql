package org.alvearie.dream.concept;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.BasicAuthenticator;
import com.ibm.watson.health.acd.v1.AnnotatorForClinicalData;
import com.ibm.watson.health.acd.v1.model.ContainerGroup;
import com.ibm.watson.health.acd.v1.model.LabValueAnnotation;

//run ./mvnw package
//
//run ./mvnw compile quarkus:dev

@Path("/")
public class ConceptExtractor {
	private static final String ACD_API_KEY = "iRIov6UHCMMG17ZGlgtEoEGNc1SrqN64cYM9Iw5X7FFv";
	private static final String ACD_API_URL = "https://us-east.wh-acd.cloud.ibm.com/wh-acd/api";
	private static final String ACD_API_VERSION = "2020-03-31";
	private static final String ACD_FLOW_ID = "wh_acd.ibm_clinical_insights_v1.0_standard_flow";

	// @formatter:off
	private static final Set<String> conditionLIs = new HashSet<>(Arrays.asList(
			"hiv", 
			"hepatitis", 
			"heporhiv",
			"pneumonitis", 
			"neuropathy", 
			"hypothyroidism", 
			"immunodeficiency", 
			"diabetes", 
			"angina", 
			"pleural-effusion",
			"proteinuria", 
			"pancreatitis", 
			"htnbp", 
			"gidisease", 
			"sickle-cell", 
			"cancertype"
			));

	private static final Set<String> blackListLIs = new HashSet<>(Arrays.asList(
			"able-to-comply", 
			"able-to-consent",
			"able-to-provide-samples", 
			"able-to-return", 
			"no-pregnancy-nursing"));
	// @formatter:on

	private static final String CCS_CODE_SYSTEM = "CCS";
	private static final String HCC_CODE_SYSTEM = "HCC";
	private static final String ICD10_CODE_SYSTEM = "ICD10";
	private static final String ICD9_CODE_SYSTEM = "ICD9";
	private static final String LOINC_CODE_SYSTEM = "LOINC";
	private static final String SNOMED_CODE_SYSTEM = "SNOMED";
	private static final String UMLS_CODE_SYSTEM = "UMLS";

	private static final String FEMALE_PATTERN = "\\b(female|woman|women|females|ladies|lady)\\b";
	private static final String MALE_PATTERN = "\\b(male|man|men|males)\\b";

	private static final String FEMALE_SNOMED_CODE = "446141000124107";
	private static final String MALE_SNOMED_CODE = "446151000124109";

	private static final String CONDITION_TYPE = "condition";
	
	public static void main(String[] args) {
		ConceptExtractor conceptExtractor = new ConceptExtractor();
		String text = "No history of bronchitis";
		System.err.println(conceptExtractor.extractConcepts("other", text));
		System.exit(0);
	}

	private ObjectMapper mapper = new ObjectMapper();

	// TODO FIXME (not sure about ranges- will need to look at one of those)
	private void addConceptValues(ContainerGroup resp, ConceptSet conceptSet) {
		if (resp.getConceptValues() != null) {
			for (com.ibm.watson.health.acd.v1.model.ConceptValue conceptValue : resp.getConceptValues()) {
				Concept concept = new Concept();
				concept.setOrigin("ConceptValue");
				concept.setText(conceptValue.getPreferredName());
				concept.setCode(conceptValue.getCui());
				concept.setCodeSystem(UMLS_CODE_SYSTEM);
				concept.setTrigger(conceptValue.getTrigger());
				concept.setValue(conceptValue.getValue());
				concept.setUnits(conceptValue.getUnit());
				if (conceptValue.isNegated() != null && conceptValue.isNegated()) {
					concept.setNegated(true);
				}
				concept.setBegin(conceptValue.getBegin());
				concept.setEnd(conceptValue.getEnd());
				conceptSet.addConcept(concept);
			}
		}
	}

	// For Diagnosis :
//	Attribute Name / Type = Diagnosis
//	Preferred Name = "<string>"
//	SNOMED Code(s) = [one or more codes, comma delimited]
//	Negated: true or false
	private void addDiagnosis(ContainerGroup resp, ConceptSet conceptSet) {
		if (resp.getCancerDiagnosis() != null) {
			for (com.ibm.watson.health.acd.v1.model.CancerDiagnosis cancerDiagnosis : resp.getCancerDiagnosis()) {
				if (cancerDiagnosis.getCui() != null) {
					Concept concept = new Concept();
					concept.setOrigin("CancerDiagnosis");
					concept.setCode(cancerDiagnosis.getCui().toString());
					concept.setCodeSystem(CCS_CODE_SYSTEM);
					if (cancerDiagnosis.isNegated() != null && cancerDiagnosis.isNegated()) {
						concept.setNegated(cancerDiagnosis.isNegated());
					}
					concept.setBegin(cancerDiagnosis.getBegin());
					concept.setEnd(cancerDiagnosis.getEnd());
					conceptSet.addConcept(concept);
				}
			}
		}
	}

	//
//	For LabValue:
//	IF you find Attribute Name: LabValue
//	Attribute Name / Type = LabValue
//	Preferred Name = "<string>"
//	LOINC Code(s) = [one or more codes, comma delimited]
//	Negated
//	Trigger
//	Value
//	Value Units
//	(note that you will also find a ConceptValue for these - which has similar information - need to determine whether to skip the C/V or include both
	private void addLabValues(ContainerGroup resp, ConceptSet conceptSet) {
		if (resp.getLabValueInd() != null) {
			for (LabValueAnnotation labValue : resp.getLabValueInd()) {
				Concept concept = new Concept();
				concept.setOrigin("LabValues");
				concept.setText(labValue.getLabTypeNormalizedName());
				concept.setCode(labValue.getLoincId());
				concept.setCodeSystem(LOINC_CODE_SYSTEM);
//				concept.setTrigger(labValue.getTrigger());
				concept.setValue(labValue.getLabValue());
//				concept.setUnits(labValue.getUnit());
				if (labValue.isNegated() != null && labValue.isNegated()) {
					concept.setNegated(labValue.isNegated());
				}
				concept.setBegin(labValue.getBegin());
				concept.setEnd(labValue.getEnd());
				conceptSet.addConcept(concept);
			}
		}
	}

	private void addRegexes(String text, ConceptSet conceptSet) {
		Matcher matcher = Pattern.compile(MALE_PATTERN, Pattern.CASE_INSENSITIVE).matcher(text);
		if (matcher.find()) {
			Concept concept = new Concept();
			concept.setOrigin("Regex");
			concept.setCode(MALE_SNOMED_CODE);
			concept.setCodeSystem(SNOMED_CODE_SYSTEM);
			concept.setBegin(matcher.start());
			concept.setEnd(matcher.end());
			conceptSet.addConcept(concept);
		}
		//
		matcher = Pattern.compile(FEMALE_PATTERN, Pattern.CASE_INSENSITIVE).matcher(text);
		if (matcher.find()) {
			Concept concept = new Concept();
			concept.setOrigin("Regex");
			concept.setCode(FEMALE_SNOMED_CODE);
			concept.setCodeSystem(SNOMED_CODE_SYSTEM);
			concept.setBegin(matcher.start());
			concept.setEnd(matcher.end());
			conceptSet.addConcept(concept);
		}
	}

	private void addSymptomDisease(ContainerGroup resp, ConceptSet conceptSet) {
		if (resp.getSymptomDisease() != null) {
			for (com.ibm.watson.health.acd.v1.model.SymptomDisease symptomDisease : resp.getSymptomDisease()) {
				Concept concept = null;
				if (symptomDisease.getSnomedConceptId() != null) {
					concept = new Concept();
					concept.setCode(symptomDisease.getSnomedConceptId());
					concept.setCodeSystem(SNOMED_CODE_SYSTEM);
				} else if (symptomDisease.getIcd10Code() != null) {
					concept = new Concept();
					concept.setCode(symptomDisease.getIcd10Code());
					concept.setCodeSystem(ICD10_CODE_SYSTEM);
				} else if (symptomDisease.getIcd9Code() != null) {
					concept = new Concept();
					concept.setCode(symptomDisease.getIcd9Code());
					concept.setCodeSystem(ICD9_CODE_SYSTEM);
				} else if (symptomDisease.getCcsCode() != null) {
					concept = new Concept();
					concept.setCode(symptomDisease.getCcsCode());
					concept.setCodeSystem(CCS_CODE_SYSTEM);
				} else if (symptomDisease.getHccCode() != null) {
					concept = new Concept();
					concept.setCode(symptomDisease.getHccCode());
					concept.setCodeSystem(HCC_CODE_SYSTEM);
				} else if (symptomDisease.getCui() != null) {
					concept = new Concept();
					concept.setCode(symptomDisease.getCui());
					concept.setCodeSystem(UMLS_CODE_SYSTEM);
				}
				if (concept != null) {
					if (symptomDisease.isNegated() != null && symptomDisease.isNegated()) {
						concept.setNegated(true);
					}
					concept.setText(symptomDisease.getSymptomDiseaseNormalizedName());
					concept.setOrigin("SymptomDisease");
					concept.setBegin(symptomDisease.getBegin());
					concept.setEnd(symptomDisease.getEnd());
					conceptSet.addConcept(concept);
				}
			}
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String extractConcepts(@QueryParam("intent") String intent, @QueryParam("text") String text) {
		ContainerGroup resp = runACD(text);

		ConceptSet conceptSet = new ConceptSet();

		String type = intent;
		if (!blackListLIs.stream().anyMatch(type::equalsIgnoreCase)) {
			addConceptValues(resp, conceptSet);
			addDiagnosis(resp, conceptSet);
			addSymptomDisease(resp, conceptSet);
			addLabValues(resp, conceptSet);
			addRegexes(text, conceptSet);
			resolveOverlappingConcepts(conceptSet);
		}

		if (conditionLIs.stream().anyMatch(intent::equalsIgnoreCase)) {
			type = CONDITION_TYPE;
		} else if (conceptSet.getConcepts()!=null && conceptSet.getConcepts().size() == 1
				&& conceptSet.getConcepts().iterator().next().getOrigin().equals("SymptomDisease")) {
			// If only one concept is found from ACD and it's a SymptomDisease, ignore the
			// learned intent and let it get handled like a "condition"
			type = CONDITION_TYPE;
		}
		conceptSet.setType(type);

		try {
			mapper.setSerializationInclusion(Include.NON_NULL);
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(conceptSet);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Concept merge(Concept c1, Concept c2) {
		Concept mergedConcept = new Concept();

		String t1 = c1.getCodeSystem();
		String t2 = c2.getCodeSystem();
		if (t1 == null) {
			mergedConcept.setCodeSystem(t2);
		} else if (t2 == null) {
			mergedConcept.setCodeSystem(t1);
		} else if (t1.equals(t2)) {
			mergedConcept.setCodeSystem(t1);
			List<String> codes = Arrays.asList(c1.getCode(), c2.getCode());
			codes.removeIf(c -> c == null);
			mergedConcept.setCode(String.join(",", codes));
		} else if (t1.equals(LOINC_CODE_SYSTEM)) {
			mergedConcept.setCodeSystem(t1);
			mergedConcept.setCode(c1.getCode());
			mergedConcept.setOrigin(c1.getOrigin());
		} else if (t2.equals(LOINC_CODE_SYSTEM)) {
			mergedConcept.setCodeSystem(t2);
			mergedConcept.setCode(c2.getCode());
			mergedConcept.setOrigin(c2.getOrigin());
		} else {
			System.err.println("Don't know how to merge code systems!!! 1: " + t1 + "\t2:" + t2);
			mergedConcept.setCodeSystem(t1);
		}
		//
		if (mergedConcept.getOrigin() == null) {
			Set<String> origins = new HashSet<>(Arrays.asList(c1.getOrigin(), c2.getOrigin()));
			origins.removeIf(c -> c == null);
			mergedConcept.setOrigin(String.join(",", origins));
		}
		//
		t1 = c1.getText();
		t2 = c2.getText();
		if (t1 == null) {
			mergedConcept.setText(t2);
		} else if (t2 == null) {
			mergedConcept.setText(t1);
		} else if (c1.getEnd() - c1.getBegin() > c2.getEnd() - c2.getBegin()) {
			mergedConcept.setText(t1);
		} else if (c2.getEnd() - c2.getBegin() > c1.getEnd() - c1.getBegin()) {
			mergedConcept.setText(t2);
		} else {
			System.err.println("Don't know how to merge text!!! 1: " + t1 + "\t2:" + t2);
			mergedConcept.setText(t1);
		}
		//
		if (mergedConcept.getCode() == null) {
			t1 = c1.getCode();
			t2 = c2.getCode();
			mergedConcept.setCode(t1 == null ? t2 : t1);
		}
		//
		t1 = c1.getTrigger();
		t2 = c2.getTrigger();
		mergedConcept.setTrigger(t1 == null ? t2 : t1);
		//
		t1 = c1.getValue();
		t2 = c2.getValue();
		mergedConcept.setValue(t1 == null ? t2 : t1);
		//
		t1 = c1.getUnits();
		t2 = c2.getUnits();
		mergedConcept.setUnits(t1 == null ? t2 : t1);
		//
		Boolean b1 = c1.getNegated();
		Boolean b2 = c2.getNegated();
		mergedConcept.setNegated(b1 == null ? b2 : b1);
		//
		long l1 = c1.getBegin();
		long l2 = c2.getBegin();
		mergedConcept.setBegin(Math.min(l1, l2));
		//
		l1 = c1.getEnd();
		l2 = c2.getEnd();
		mergedConcept.setEnd(Math.max(l1, l2));
		//
		return mergedConcept;
	}

	private void resolveOverlappingConcepts(ConceptSet conceptSet) {
		if (conceptSet.getConcepts() == null || conceptSet.getConcepts().size() < 2) {
			return;
		}
		Set<Concept> orderedConceptSet = new TreeSet<Concept>(new Comparator<Concept>() {
			@Override
			public int compare(Concept c1, Concept c2) {
				if (c1.getBegin() < c2.getBegin()) {
					return -1;
				}
				if (c1.getBegin() > c2.getBegin()) {
					return 1;
				}
				if (c1.getEnd() < c2.getEnd()) {
					return -1;
				}
				if (c1.getEnd() > c2.getEnd()) {
					return 1;
				}
				if (c1.getCode().compareTo(c2.getCode()) < 0) {
					return -1;
				}
				if (c1.getCode().compareTo(c2.getCode()) > 0) {
					return 1;
				}
				return 0;
			}
		});
		orderedConceptSet.addAll(conceptSet.getConcepts());

		Set<Concept> newConceptSet = new HashSet<>();
		Iterator<Concept> conceptIterator = orderedConceptSet.iterator();
		Concept curConcept = conceptIterator.next();
		while (conceptIterator.hasNext()) {
			Concept nextConcept = conceptIterator.next();
			long b1 = curConcept.getBegin();
			long e1 = curConcept.getEnd();
			long b2 = nextConcept.getBegin();
			long e2 = nextConcept.getEnd();

			if (e2 >= b1 && e1 >= b2) {
				curConcept = merge(curConcept, nextConcept);
				nextConcept = null;
			} else {
				newConceptSet.add(curConcept);
				curConcept = nextConcept;
			}
			if (!conceptIterator.hasNext()) {
				newConceptSet.add(curConcept);
			}
		}
		conceptSet.setConcepts(newConceptSet);
	}

	private ContainerGroup runACD(String text) {
		Authenticator authenticator = new BasicAuthenticator("apikey", ACD_API_KEY);
		AnnotatorForClinicalData acd = new AnnotatorForClinicalData(ACD_API_VERSION,
				AnnotatorForClinicalData.DEFAULT_SERVICE_NAME, authenticator);
		acd.setServiceUrl(ACD_API_URL);
		if (!text.trim().endsWith(".")) {
			return acd.analyzeWithFlow(ACD_FLOW_ID, text + ".");
		} else {
			return acd.analyzeWithFlow(ACD_FLOW_ID, text);
		}
	}
}