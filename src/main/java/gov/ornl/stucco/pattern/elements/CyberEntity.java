package gov.ornl.stucco.pattern.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="class")
public class CyberEntity extends PatternElement {
	
	@JsonIgnore
	private String type;
	
	@JsonIgnore
	private String subType;
	
	@Override
	@JsonSetter("value")
	public void setValue(String value) {
		this.value = value;
		
		//separate label
		if (super.getValue().contains(".")) {
			int index = super.getValue().indexOf(".");
			this.type = super.getValue().substring(0, index);
			this.subType = super.getValue().substring(index + 1);
		}
		else {
			this.type = super.getValue();
			this.subType = "";
		}
	}

	@JsonIgnore
	public String getType() {
		return type;
	}

	@JsonIgnore
	public String getSubType() {
		return subType;
	}

	@Override
	public String toString() {
		return "CyberEntity=" + value;
	}
	
	
}
