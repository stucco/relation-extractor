package gov.ornl.stucco.pattern.elements;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="class")
public class Token extends PatternElement {

	@Override
	public String toString() {
		return "Token=" + value;
	}

}
