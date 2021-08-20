package org.alvearie.dream.concept;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ConceptSet implements Serializable {

	private static final long serialVersionUID = 3876210800894423584L;

	private Set<Concept> concepts;
	private String type;

	public void addConcept(Concept concept) {
		if (concepts == null) {
			concepts = new HashSet<>();
		}
		concepts.add(concept);
	}

	public Set<Concept> getConcepts() {
		return concepts;
	}

	public String getType() {
		return type;
	}

	public void setConcepts(Set<Concept> concepts) {
		this.concepts = concepts;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}