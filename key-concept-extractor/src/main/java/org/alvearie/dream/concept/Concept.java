package org.alvearie.dream.concept;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Concept implements Serializable {

	private static final long serialVersionUID = 316142873900075806L;

	private String code;
	private String codeSystem;
	private Boolean negated;
	private String origin;
	private String text;
	private String trigger;
	private String units;
	private String value;

	@JsonIgnore
	private long begin;
	
	@JsonIgnore
	private long end;

	public Concept() {
	}

	public long getBegin() {
		return begin;
	}

	public String getCode() {
		return code;
	}

	public String getCodeSystem() {
		return codeSystem;
	}

	public long getEnd() {
		return end;
	}

	public Boolean getNegated() {
		return negated;
	}

	public String getOrigin() {
		return origin;
	}

	public String getText() {
		return text;
	}

	public String getTrigger() {
		return trigger;
	}

	public String getUnits() {
		return units;
	}

	public String getValue() {
		return value;
	}

	public void setBegin(long begin) {
		this.begin = begin;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setCodeSystem(String codeSystem) {
		this.codeSystem = codeSystem;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setNegated(Boolean negated) {
		this.negated = negated;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
