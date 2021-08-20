package org.alvearie.dream.cql.processors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alvearie.dream.concept.Concept;
import org.alvearie.dream.cql.TypeProcessor;
import org.alvearie.dream.cql.snomed.SnoMedExpander;

public class ConditionTypeProcessor implements TypeProcessor {

	private static final boolean EXPAND_SNOMED_CODES = true;
	
	@Override
	public Set<String> getSupportedTypes() {
		return new HashSet<String>(Arrays.asList("condition"));
	}

	public String process(Set<Concept> concepts) {

		Boolean negationContext = false; //was there negation in the text, if so use and later on

		HashSet<String> codesToUse = new HashSet<>();

		for (Concept c : concepts) { // look for all codes
			if (c.getCodeSystem().equals("SNOMED")) {
				Set<String> expandedCodes = EXPAND_SNOMED_CODES ? SnoMedExpander.expandSnoMed(c.getCode())
						: Collections.emptySet();
				if (c.getNegated() == null || (c.getNegated()!=null && !c.getNegated())) {
					codesToUse.add(c.getCode());
					codesToUse.addAll(expandedCodes);
				} else if (c.getNegated()) {
					codesToUse.add(c.getCode() + "NEGATED");
					expandedCodes.forEach(e -> codesToUse.add(e + "NEGATED"));
					negationContext = true;
				}
			}
		}

		StringBuilder cql = new StringBuilder();
		cql.append("library \"ConditionX\" version '1.0.0'\n");
		cql.append("// type = condition patients\n");
		cql.append("using FHIR version '4.0.1'\n");
		cql.append("include \"FHIRHelpers\" version '4.0.1' called FHIRHelpers\n");

		cql.append("codesystem \"SNOMED\": 'http://snomed.info/sct'\n");
		cql.append("codesystem \"CONDCLINSTATUS\": 'http://terminology.hl7.org/CodeSystem/condition-clinical'\n");

		HashSet<String> generatedCodeNames = new HashSet<>();

		//Integer idx = 1;
		for (String acode: codesToUse) {
			String codename = "Code" + acode;
			generatedCodeNames.add(codename);

			String template = "code \"CODENAME\": 'CODEVAL' from \"SNOMED\"\n";
			template = template.replace("CODENAME", codename);
			template = template.replace("CODEVAL", acode.replace("NEGATED",""));
			cql.append(template);
			//idx++;
		}

		cql.append("context Patient\n");

		HashSet<String> generatedDefineNames = new HashSet<>();

		for (String aname: generatedCodeNames) {
			String defineName = "ActiveCondition" + aname;
			generatedDefineNames.add("\"" + defineName + "\"");
			String defineTemplate = "define \"DEFINENAME\":\n"
					+ "	NOTOP exists(\n"
					+ "		[Condition: \"CODENAME\"] Cond\n"
					+ "			where Cond.clinicalStatus.coding[0].code.value = 'active'\n"
					+ "RESOLVED"
					+ "	)\n";

			defineTemplate = defineTemplate.replace("DEFINENAME", defineName);
			defineTemplate = defineTemplate.replace("CODENAME", aname);
			if (!defineName.contains("NEGATED")) {
				defineTemplate = defineTemplate.replace("NOTOP", "");
				defineTemplate = defineTemplate.replace("RESOLVED", "");
			} else {
				defineTemplate = defineTemplate.replace("NOTOP", "not");
				defineTemplate = defineTemplate.replace("RESOLVED", "                       or Cond.clinicalStatus.coding[0].code.value = 'resolved'\n");

			}

			cql.append(defineTemplate);
		}




		// Denominator
		cql.append("define \"Denominator\":\n");
		cql.append("true\n");
		// Numerator
		cql.append("define \"Numerator\":\n");

		//or together the condition define names but use and if there is negation context

		String numerator = null;
		if (negationContext) {
			numerator = String.join(" and ", generatedDefineNames);
		} else {
			numerator = String.join(" or ", generatedDefineNames);
		}
		cql.append(numerator);

		return cql.toString();

	}
}
