package org.glassfish.jersey.client;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;

import org.glassfish.jersey.client.ClientRequest;

import br.com.ideotech.drawout.aspects.DrawOutAbstractAspect;
import br.com.ideotech.drawout.core.MetricManager;
import br.com.ideotech.drawout.model.HttpRequest;
import br.com.ideotech.drawout.model.Metric;
import br.com.ideotech.drawout.utils.HttpUtils;

public aspect DrawOutJerseyMetricsAspect extends DrawOutAbstractAspect {

	@Before("execution(* org.glassfish.jersey.client.ClientRuntime.invoke(..))")
	public void beforeJaxRsInvoke(ProceedingJoinPoint joinPoint) {
		Metric current = MetricManager.getCurrentMetric();
		if (current != null) {
			String currentFlowId = null;
			if (current != null) {
				currentFlowId = current.getFlowId();
			}
			Metric metric = MetricManager.allocateMetric(currentFlowId);
			ClientRequest clientRequest = ((ClientRequest) joinPoint.getArgs()[0]);
			// Keep correlation
			clientRequest.getHeaders().add(FLOW_ID, current.getFlowId());
			metric.setComponent(joinPoint.getSignature().toString());

			HttpRequest requestPayload = HttpUtils.dumpRequestInfo(clientRequest);
			// Keep correlated if has one flow id
			if (requestPayload.getHeaders() != null && requestPayload.getHeaders().containsKey(FLOW_ID)) {
				metric.setFlowId(requestPayload.getHeaders().get(FLOW_ID));
			} else if (requestPayload.getAttributes() != null && requestPayload.getAttributes().containsKey(FLOW_ID)) {
				metric.setFlowId(requestPayload.getAttributes().get(FLOW_ID));
			}
			metric.setRequest(requestPayload);
		}
	}

	@After("execution(* org.glassfish.jersey.client.ClientRuntime.invoke(..))")
	public void afterRsPost(ProceedingJoinPoint joinPoint) {
		MetricManager.deallocateMetric();
	}

}
