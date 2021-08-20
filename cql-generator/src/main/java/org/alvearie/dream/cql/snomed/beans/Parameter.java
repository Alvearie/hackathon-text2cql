package org.alvearie.dream.cql.snomed.beans;

import java.util.Set;

public class Parameter {
	String name;
	Set<Part> part;
	String valueString;

	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	public String getName() {
		return name;
	}

	public Set<Part> getPart() {
		return part;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPart(Set<Part> part) {
		this.part = part;
	}
}
