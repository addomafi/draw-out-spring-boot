package br.com.ideotech.drawout.model;

public class Sample {

	private Integer id;
	private String text;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Sample)) {
			return Boolean.FALSE;
		}
		Sample otherSample = (Sample) other;
		Boolean isEqual = Boolean.TRUE;
		if ((this.id != null  && otherSample.text != null)) {
			isEqual = this.id.equals(otherSample.id);
		}
		if (isEqual && (this.text != null && otherSample.text != null)) {
			isEqual = this.text.equals(otherSample.text);
		}
		return isEqual;
	}
}
