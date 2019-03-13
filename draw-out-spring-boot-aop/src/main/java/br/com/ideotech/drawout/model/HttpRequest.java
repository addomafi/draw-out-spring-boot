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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequest {
	
	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HttpRequest.class);

	private String version;
	private String scheme;
	private String protocol;
	private String host;
	private Integer port;
	private String method;
	private String uri;
	private Map<String, String> queryParameters;
	private Map<String, String> headers;
	private Map<String, String> cookies;
	private Map<String, String> attributes;
	private Object payload;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}

	public void addQueryParameter(String key, String value) {
		if (this.queryParameters == null)
			this.queryParameters = new HashMap<String, String>();
		this.queryParameters.put(key, value);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void addHeader(String key, String value) {
		if (this.headers == null)
			this.headers = new HashMap<String, String>();
		this.headers.put(key, value);
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void addAttributes(String key, String value) {
		if (this.attributes == null)
			this.attributes = new HashMap<String, String>();
		this.attributes.put(key, value);
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public void addCookie(String key, String value) {
		if (this.cookies == null)
			this.cookies = new LinkedHashMap<String, String>();
		this.cookies.put(key, value);
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getBaseUrl() {
		String baseRequestURL;
		baseRequestURL = this.scheme + "://" + this.host;
		if (("http".equals(this.scheme) && (this.port == 80)) || ("https".equals(this.scheme) && (this.port == 443))) {
			LOGGER.debug("normal case, don't add the port");
		} else {
			baseRequestURL += ":" + this.port;
		}
		return baseRequestURL;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

}
