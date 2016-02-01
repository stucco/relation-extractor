package gov.ornl.stucco.pattern;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Patterns implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("Patterns")
	private List<Pattern> patterns;

	@JsonGetter("Patterns")
	public List<Pattern> getPatterns() {
		return patterns;
	}

	@JsonSetter("Patterns")
	public void setPatterns(List<Pattern> patterns) {
		this.patterns = patterns;
	}
	
	public void addPattern(Pattern newPattern) {
		this.patterns.add(newPattern);
	}

	@Override
	public String toString() {
		return "Patterns [patterns=" + patterns + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((patterns == null) ? 0 : patterns.hashCode());
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
		Patterns other = (Patterns) obj;
		if (patterns == null) {
			if (other.patterns != null)
				return false;
		} else if (!patterns.equals(other.patterns))
			return false;
		return true;
	}

}
