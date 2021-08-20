package org.alvearie.dream.cql.processors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.alvearie.dream.concept.Concept;
import org.alvearie.dream.cql.TypeProcessor;

public class RMETypeProcessor implements TypeProcessor {

	@Override
	public Set<String> getSupportedTypes() {
		return new HashSet<String>(
				Arrays.asList("able-to-comply", "able-to-consent", "able-to-provide-samples", "able-to-return"));
	}

	public String process(Set<Concept> concepts) {
		return NO_CQL_GENERATED + "\n\nRequires Medical Evaluation";
	}
}