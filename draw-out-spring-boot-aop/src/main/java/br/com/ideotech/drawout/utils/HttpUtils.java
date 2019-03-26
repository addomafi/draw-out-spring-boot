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
package br.com.ideotech.drawout.utils;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.ideotech.drawout.model.HttpRequest;
import br.com.ideotech.drawout.model.HttpResponse;

public class HttpUtils {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HttpUtils.class);
	private static final Boolean DUMP_SENSITIVE_DATA = Boolean.TRUE.toString()
			.equals(PropertiesUtil.getInstance().getValue("drawout.dump.request.sensitive-data"));
	private static final Boolean DUMP_REQUEST_PAYLOAD = Boolean.TRUE.toString()
			.equals(PropertiesUtil.getInstance().getValue("drawout.dump.request.payload"));
	private static final Boolean DUMP_RESPONSE_PAYLOAD = Boolean.TRUE.toString()
			.equals(PropertiesUtil.getInstance().getValue("drawout.dump.response.payload"));

	private HttpUtils() {
		throw new IllegalStateException("Utility class");
	}
	
	private static void extractQueryParams(HttpRequest request, String queryString) {
		String[] params = queryString.split("&");
		for (String param : params) {
			String[] kv = param.split("\\=");
			String key = "";
			String value = "";
			if (kv.length > 0) {
				key = kv[0];
			}
			if (kv.length > 1) {
				value = kv[1];
			}
			request.addQueryParameter(key, value);
		}
	}

	private static void dumpHttpQueryParams(HttpRequest request, HttpServletRequest httpServletRequest) {
		extractQueryParams(request, httpServletRequest.getQueryString());
	}

	private static void dumpCookies(HttpRequest request, HttpServletRequest httpServletRequest) {
		for (Cookie cookie : httpServletRequest.getCookies()) {
			request.addCookie(cookie.getName(), cookie.getValue());
		}
	}

	private static void dumpAttributes(HttpRequest request, HttpServletRequest httpServletRequest) {
		Enumeration<String> attributeNameEnum = httpServletRequest.getAttributeNames();
		while (attributeNameEnum.hasMoreElements()) {
			String attributeName = attributeNameEnum.nextElement();
			if (attributeName != null && attributeName.indexOf("org.springframework") < 0) {
				request.addAttributes(attributeName, String.valueOf(httpServletRequest.getAttribute(attributeName)));
			}
		}
	}

	private static void dumpHeaderParams(HttpRequest request, HttpServletRequest httpServletRequest) {
		Enumeration<String> headerNameEnum = httpServletRequest.getHeaderNames();
		while (headerNameEnum.hasMoreElements()) {
			String headerName = headerNameEnum.nextElement();
			request.addHeader(headerName.toLowerCase(), httpServletRequest.getHeader(headerName));
		}
	}

	public static HttpRequest dumpRequestInfo(HttpServletRequest httpServletRequest) {
		HttpRequest request = new HttpRequest();
		request.setScheme(httpServletRequest.getScheme());
		request.setProtocol(httpServletRequest.getProtocol());
		request.setMethod(httpServletRequest.getMethod());
		request.setHost(httpServletRequest.getServerName());
		request.setPort(httpServletRequest.getServerPort());
		request.setUri(httpServletRequest.getRequestURI());

		if (DUMP_SENSITIVE_DATA) {
			// If has query parameters
			if (httpServletRequest.getQueryString() != null) {
				dumpHttpQueryParams(request, httpServletRequest);
			}
			// If has cookies
			if (httpServletRequest.getCookies() != null) {
				dumpCookies(request, httpServletRequest);
			}
			// If has request attributes
			if (httpServletRequest.getAttributeNames() != null) {
				dumpAttributes(request, httpServletRequest);
			}
			// If has headers parameters
			if (httpServletRequest.getHeaderNames() != null) {
				dumpHeaderParams(request, httpServletRequest);
			}
		}
		return request;
	}
	
	public static HttpResponse dumpResponseInfo(HttpServletResponse httpServletResponse) {
		HttpResponse response = new HttpResponse();
		dumpResponseInfo(response, httpServletResponse);
		return response;
	}

	public static void dumpResponseInfo(HttpResponse response, HttpServletResponse httpServletResponse) {
		if (httpServletResponse.getHeaderNames() != null) {
			Iterator<String> headerNameEnum = httpServletResponse.getHeaderNames().iterator();
			while (headerNameEnum.hasNext()) {
				String headerName = headerNameEnum.next();
				response.addHeader(headerName, httpServletResponse.getHeader(headerName));
			}
			response.setStatus(httpServletResponse.getStatus());
		}
	}

	private static String dumpPayload(Object value) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return new String(mapper.writeValueAsBytes(value));
		} catch (JsonProcessingException jpe) {
			LOGGER.warn("An error occurred to convert Java object to JSON.", jpe);
		}
		return String.valueOf(value);
	}
	
	public static void dumpRequestPayload(HttpRequest request, Object payload) {
		if (DUMP_REQUEST_PAYLOAD) {
			request.setPayload(dumpPayload(payload));
		}
	}
	
	public static void dumpResponsePayload(HttpResponse response, Object payload) {
		if (DUMP_RESPONSE_PAYLOAD) {
			response.setPayload(dumpPayload(payload));
		}
	}
	
	public static HttpRequest dumpRequestInfo(org.glassfish.jersey.client.ClientRequest clientRequest) {
		HttpRequest request = new HttpRequest();
		request.setScheme(clientRequest.getUri().getScheme());
		request.setMethod(clientRequest.getMethod());
		request.setHost(clientRequest.getUri().getHost());
		request.setPort(clientRequest.getUri().getPort());
		request.setUri(clientRequest.getUri().getPath());

		if (DUMP_SENSITIVE_DATA) {
			// If has query parameters
			if (clientRequest.getUri().getQuery() != null) {
				dumpHttpQueryParams(request, clientRequest);
			}
			// If has cookies
			if (clientRequest.getCookies() != null) {
				dumpCookies(request, clientRequest);
			}
			// If has headers parameters
			if (clientRequest.getHeaders() != null) {
				dumpHeaderParams(request, clientRequest);
			}
		}
		return request;
	}
	
	private static void dumpHttpQueryParams(HttpRequest request, org.glassfish.jersey.client.ClientRequest clientRequest) {
		extractQueryParams(request, clientRequest.getUri().getQuery());
	}

	private static void dumpCookies(HttpRequest request, org.glassfish.jersey.client.ClientRequest clientRequest) {
		Iterator<String> cookies = clientRequest.getCookies().keySet().iterator();
		while (cookies.hasNext()) {
			javax.ws.rs.core.Cookie cookie = clientRequest.getCookies().get(cookies.next());
			request.addCookie(cookie.getName(), cookie.getValue());
		}
	}

	private static void dumpHeaderParams(HttpRequest request, org.glassfish.jersey.client.ClientRequest clientRequest) {
		Iterator<String> headers = clientRequest.getHeaders().keySet().iterator();
		while (headers.hasNext()) {
			String headerName = headers.next();
			request.addHeader(headerName, clientRequest.getHeaderString(headerName));
		}
	}

}
