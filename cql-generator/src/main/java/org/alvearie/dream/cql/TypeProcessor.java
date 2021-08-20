package org.alvearie.dream.cql;

import java.util.Set;

import org.alvearie.dream.concept.Concept;

public interface TypeProcessor {

	public static final String NO_CQL_GENERATED = "NO CQL GENERATED";

	public Set<String> getSupportedTypes();

	public String process(Set<Concept> concepts);
}