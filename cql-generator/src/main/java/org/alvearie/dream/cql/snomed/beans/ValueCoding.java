package org.alvearie.dream.cql.snomed.beans;

public class ValueCoding {
	String system;
	String code;
	String display;

	public String getCode() {
		return code;
	}

	public String getDisplay() {
		return display;
	}

	public String getSystem() {
		return system;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public void setSystem(String system) {
		this.system = system;
	}
}
