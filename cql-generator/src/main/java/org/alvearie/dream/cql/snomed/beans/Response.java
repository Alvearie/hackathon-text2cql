package org.alvearie.dream.cql.snomed.beans;

import java.util.Set;

public class Response {
	String resourceType;
	Set<Parameter> parameter;

	public Set<Parameter> getParameter() {
		return parameter;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setParameter(Set<Parameter> parameter) {
		this.parameter = parameter;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
}
