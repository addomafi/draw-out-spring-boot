package br.com.ideotech.drawout.model;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

	private Map<String, String> headers;
	private String payload;
	private Integer status;
	
	public Map<String, String> getHeaders() {
		return headers;
	}

	public void addHeader(String key, String value) {
		if (this.headers == null)
			this.headers = new HashMap<String, String>();
		this.headers.put(key, value);
	}
	
	public String getPayload() {
		return payload;
	}
	
	public void setPayload(String payload) {
		this.payload = payload;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
