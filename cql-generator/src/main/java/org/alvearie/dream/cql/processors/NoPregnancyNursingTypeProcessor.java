package org.alvearie.dream.cql.processors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.alvearie.dream.concept.Concept;
import org.alvearie.dream.cql.TypeProcessor;

public class NoPregnancyNursingTypeProcessor implements TypeProcessor {

	@Override
	public Set<String> getSupportedTypes() {
		return new HashSet<String>(Arrays.asList("no-pregnancy-nursing"));
	}

	public String process(Set<Concept> concepts) {
		return "";
	}
}