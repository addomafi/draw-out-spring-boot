/**
 * Copyright 2019 Adauto Martins <adauto.martin@ideotech.com.br>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
