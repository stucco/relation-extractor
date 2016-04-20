package gov.ornl.stucco.pattern.elements;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="class")
@JsonSubTypes({ 
	@Type(value = CyberEntity.class), 
	@Type(value = POS.class),
	@Type(value = Token.class),
	@Type(value = TreeElement.class)
})
public abstract class PatternElement {
	
	public static enum edgeVType {
		outV,
		inV
	}
	
	@JsonProperty("value")
	protected String value;
	@JsonProperty("vType")
	protected edgeVType vType;

	@JsonGetter("value")
	public String getValue() {
		return value;
	}

	@JsonSetter("value")
	public void setValue(String value) {
		this.value = value;
	}

	@JsonGetter("vType")
	public edgeVType getvType() {
		return vType;
	}

	@JsonSetter("vType")
	public void setvType(edgeVType vType) {
		this.vType = vType;
	}

	@Override
	public String toString() {
		return "PatternElement [value=" + value + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatternElement other = (PatternElement) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
