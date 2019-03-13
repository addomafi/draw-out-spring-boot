package br.com.ideotech.drawout.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Metric {

	private String flowId;
	private Integer deepLevel;
	private String component;
	private Long startTime;
	private Long stopTime;
	private HttpRequest request;
	private HttpResponse response;

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public Integer getDeepLevel() {
		return deepLevel;
	}

	public void setDeepLevel(Integer deepLevel) {
		this.deepLevel = deepLevel;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public void startCounting() {
		this.startTime = System.currentTimeMillis();
	}

	public void stopCounting() {
		this.stopTime = System.currentTimeMillis();
	}

	public Long getTakenTime() {
		return this.stopTime - this.startTime;
	}

	@JsonInclude(Include.NON_NULL)
	public HttpRequest getRequest() {
		return request;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	@JsonInclude(Include.NON_NULL)
	public HttpResponse getResponse() {
		return response;
	}

	public void setResponse(HttpResponse response) {
		this.response = response;
	}

}
