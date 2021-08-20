package org.alvearie.dream.cql.processors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.alvearie.dream.concept.Concept;
import org.alvearie.dream.cql.TypeProcessor;

public class AgeGenderTypeProcessor implements TypeProcessor {

	private static final String FEMALE_SNOMED_CODE = "446141000124107";
	private static final String MALE_SNOMED_CODE = "446151000124109";
	private static final String AGE_CUI = "C0001779";

	@Override
	public Set<String> getSupportedTypes() {
		return new HashSet<String>(Arrays.asList("ageGender"));
	}

	public String process(Set<Concept> concepts) {
		// extract and replace
		String trigger = "="; //default to equality
		String agevalue = "";

		for (Concept c : concepts) { // look for age cui
			if (c.getCode().equals(AGE_CUI)) {
				if (c.getTrigger() != null) {
					trigger = c.getTrigger();
				}
				agevalue = c.getValue();
				break;
			}
		}

		if (trigger.equals("greater than")) {
			trigger = ">";
		} else if (trigger.equals("greater than or equal to")) {
			trigger = ">=";
		} else if (trigger.equals("less than")) {
			trigger = "<";
		} else if (trigger.equals("less than or equal to")) {
			trigger = "<=";
		}

		boolean male = false;
		boolean female = false;
		for (Concept c : concepts) { // look for gender code
			if (c.getCode().equals(FEMALE_SNOMED_CODE)) {
				female = true;
			} else if (c.getCode().equals(MALE_SNOMED_CODE)) {
				male = true;
			}
		}

		StringBuilder cql = new StringBuilder();
		cql.append("library \"PatientsByAgeGender\" version '1.0.0'\n");
		cql.append("// gender based patients by age restriction\n");
		cql.append("using FHIR version '4.0.1'\n");
		cql.append("include \"FHIRHelpers\" version '4.0.1' called FHIRHelpers\n");
		cql.append("context Patient\n");
		// Denominator
		cql.append("define \"Denominator\":\n");
		if (male && !female) {
			cql.append("Patient.gender.value = 'male'\n");
		} else if (female && !male) {
			cql.append("Patient.gender.value = 'female'\n");
		} else {
			cql.append("true\n");
		}
		// Numerator
		cql.append("define \"Numerator\":\n");
		cql.append("AgeInYears() " + trigger + " " + agevalue);

		return cql.toString();
	}
}