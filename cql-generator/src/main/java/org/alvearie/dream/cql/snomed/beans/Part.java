package org.alvearie.dream.cql.snomed.beans;

public class Part {
	String name;
	String valueString;
	String valueCode;
	ValueCoding valueCoding;

	public ValueCoding getValueCoding() {
		return valueCoding;
	}

	public void setValueCoding(ValueCoding valueCoding) {
		this.valueCoding = valueCoding;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValueCode() {
		return valueCode;
	}

	public void setValueCode(String valueCode) {
		this.valueCode = valueCode;
	}

	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}
}
