package org.alvearie.dream.cql.processors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.alvearie.dream.concept.Concept;
import org.alvearie.dream.cql.TypeProcessor;

public class LabTypeProcessor implements TypeProcessor {
	
	private static final String LOINC = "LOINC";


	@Override
	public Set<String> getSupportedTypes() {
		return new HashSet<String>(Arrays.asList("hgb", "bilirubin", "anc", "albumin", "alp", "creatinine", "platelet"));
	}

	public String process(Set<Concept> concepts) {

		String trigger = "="; //default to equality
		String boundvalue = "";
        String loinccodes = "";
        Boolean negationContext = false;
        
        Set<String> loincset = new HashSet<>();
		HashSet<String> codesToUse = new HashSet<>();

        
		for (Concept c : concepts) { // look for loinc codes
			if (c.getCodeSystem().equals(LOINC)) {
				
				loinccodes = c.getCode();
				for (String s: loinccodes.split(",")) {
					loincset.add(s);
				}
				
				if (c.getNegated() == null || (c.getNegated()!=null && !c.getNegated())) {
					negationContext = false;
				} else {
					negationContext = true;
				}
				
				if (c.getTrigger() != null) {
					trigger = c.getTrigger();
				}
				boundvalue = c.getValue();
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

		for (String aloinc: loincset) { // go through all loinc codes
				if (!negationContext) {
					codesToUse.add(aloinc);
				} else {
					codesToUse.add(aloinc + "NEGATED");
				}
		}

		StringBuilder cql = new StringBuilder();
		cql.append("library \"LaboratoryX\" version '1.0.0'\n");
		cql.append("// type = laboratory patients\n");
		cql.append("using FHIR version '4.0.1'\n");
		cql.append("include \"FHIRHelpers\" version '4.0.1' called FHIRHelpers\n");

		cql.append("codesystem \"LOINC\": 'http://loinc.org'\n");

		HashSet<String> generatedCodeNames = new HashSet<>();

		for (String acode: codesToUse) {
			String codename = "Code" + acode;
			generatedCodeNames.add(codename);

			String template = "code \"CODENAME\": 'CODEVAL' from \"LOINC\"\n";
			template = template.replace("CODENAME", codename);
			template = template.replace("CODEVAL", acode.replace("NEGATED",""));
			cql.append(template);
		}

		cql.append("context Patient\n");

		HashSet<String> generatedDefineNames = new HashSet<>();

		for (String aname: generatedCodeNames) {
			String defineName = "ActiveCondition" + aname;
			generatedDefineNames.add("\"" + defineName + "\"");
			String defineTemplate = "define \"DEFINENAME\":\n"
					+ "	exists(\n"
					+ "		[Observation: \"CODENAME\"] Obs\n"
					+ "			where Obs.value.value TRIGGER BOUND\n"
					+ "	)\n";


			defineTemplate = defineTemplate.replace("DEFINENAME", defineName);
			defineTemplate = defineTemplate.replace("CODENAME", aname);
			defineTemplate = defineTemplate.replace("TRIGGER", trigger);
			defineTemplate = defineTemplate.replace("BOUND", boundvalue);

			cql.append(defineTemplate);
		}




		// Denominator
		cql.append("define \"Denominator\":\n");
		cql.append("true\n");
		// Numerator
		cql.append("define \"Numerator\":\n");

		//or together the condition define names but use and if there is negation context
		
//		HashSet<String> generatedDefinedExpressions = new HashSet<>();
//		for (String aname: generatedDefineNames) {
//			String expr = "(" + aname + " " + trigger + " " + boundvalue + ")";
//			generatedDefinedExpressions.add(expr);
//		}

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
