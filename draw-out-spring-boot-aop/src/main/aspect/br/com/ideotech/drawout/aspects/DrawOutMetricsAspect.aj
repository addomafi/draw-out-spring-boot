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
package br.com.ideotech.drawout.aspects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.ideotech.drawout.core.MetricManager;
import br.com.ideotech.drawout.model.HttpRequest;
import br.com.ideotech.drawout.model.HttpResponse;
import br.com.ideotech.drawout.model.Metric;
import br.com.ideotech.drawout.utils.HttpUtils;
import br.com.ideotech.drawout.utils.ReflectionUtils;

/**
 * Aspect that covers all required pointcuts to extract metrics.
 *
 * @author Adauto Martins adauto.martins@ideotech.com.br
 */
public aspect DrawOutMetricsAspect extends DrawOutAbstractAspect {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DrawOutMetricsAspect.class);

	/**
	 * A pointcut to intercept the HTTP request at first into Spring Web
	 * framework, it enable us to getting some data from HTTP Requests
	 *
	 * @param joinPoint
	 * @throws Exception
	 */
	@Before("execution(* org.springframework.web.servlet.DispatcherServlet.doService(..))")
	public void beforeDoService(ProceedingJoinPoint joinPoint) throws Exception {
		String currentFlowId = null;
		HttpServletRequest request = null;
		if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
			request = (HttpServletRequest) joinPoint.getArgs()[0];
			HttpRequest requestPayload = HttpUtils.dumpRequestInfo(request);
			// Keep correlated if has one flow id
			if (requestPayload.getHeaders() != null && requestPayload.getHeaders().containsKey(FLOW_ID)) {
				currentFlowId = requestPayload.getHeaders().get(FLOW_ID);
			}
			Metric metric = MetricManager.allocateMetric(currentFlowId);
			metric.setRequest(requestPayload);
			// Define the flowid for internal request attributes
			request.setAttribute(FLOW_ID, metric.getFlowId());
		} else {
			MetricManager.allocateMetric();
		}
	}

	@After("execution(* org.springframework.web.servlet.DispatcherServlet.doService(..))")
	public void afterDoService(JoinPoint joinPoint) {
		HttpServletResponse response = null;
		Metric metric = MetricManager.getCurrentMetric();
    if (metric != null) {
  		if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 1) {
  			response = (HttpServletResponse) joinPoint.getArgs()[1];
  			if (metric.getResponse() == null) {
  				metric.setResponse(HttpUtils.dumpResponseInfo(response));
  			} else {
  				HttpUtils.dumpResponseInfo(metric.getResponse(), response);
  			}
  		}
  		MetricManager.deallocateMetric();
    }
	}

	@Before("(@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping)  || @annotation(org.springframework.web.bind.annotation.PutMapping)  || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) ) && execution(* *(..))")
	public void beforeControllerMethod(JoinPoint joinPoint) {
		Metric metric = MetricManager.getCurrentMetric();
		if (metric != null) {
  		if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
  			HttpUtils.dumpRequestPayload(metric.getRequest(), ReflectionUtils.getParameterByAnnotationClass(
  					joinPoint.getArgs(), ((MethodSignature) joinPoint.getSignature()).getMethod(), RequestBody.class));
  		}
  		metric.setComponent(joinPoint.getSignature().toString());
    }
	}

	@AfterReturning(pointcut = "@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))", returning = "retValue")
	public void afterReturningControllerMethod(Object retValue) {
		Metric metric = MetricManager.getCurrentMetric();
    if (metric != null) {
  		if (metric.getResponse() == null) {
  			metric.setResponse(new HttpResponse());
  		}
      HttpUtils.dumpResponsePayload(metric.getResponse(), retValue);
    }
	}

	@AfterThrowing(pointcut = "@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))", throwing = "exception")
	public void afterThrowingControllerMethod(Throwable exception) {
		Metric metric = MetricManager.getCurrentMetric();
    if (metric != null) {
  		if (metric.getResponse() == null) {
  			metric.setResponse(new HttpResponse());
  		}
      metric.getResponse().setPayload(ExceptionUtils.getFullStackTrace(exception));
    }
	}
}
