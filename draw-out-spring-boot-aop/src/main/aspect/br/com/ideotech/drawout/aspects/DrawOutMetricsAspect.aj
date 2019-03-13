package br.com.ideotech.drawout.aspects;

import java.util.LinkedList;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Invocation.Builder;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.ideotech.drawout.kinesis.KinesisRecordAggregation;
import br.com.ideotech.drawout.model.HttpRequest;
import br.com.ideotech.drawout.model.HttpResponse;
import br.com.ideotech.drawout.model.Metric;
import br.com.ideotech.drawout.utils.HttpUtils;
import br.com.ideotech.drawout.utils.ReflectionUtils;

/**
 * Aspect that covers all required pointcuts to extract metrics.
 * 
 * @author Adauto Martins <adauto.martins@ideotech.com.br>
 */
public aspect DrawOutMetricsAspect {
	
	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DrawOutMetricsAspect.class);

	private static final String FLOW_ID = "br.com.ideotech.drawout.flowid";

	ThreadLocal<LinkedList<Metric>> local = ThreadLocal.withInitial(() -> new LinkedList<Metric>());
	ThreadLocal<KinesisRecordAggregation> recAgg = ThreadLocal.withInitial(() -> new KinesisRecordAggregation());

	@Before("execution(* org.springframework.web.servlet.DispatcherServlet.doService(..))")
	public void beforeDoService(ProceedingJoinPoint joinPoint) throws Exception {
		Metric metric = allocateMetric();
		
		HttpServletRequest request = null;
		if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
			request = (HttpServletRequest) joinPoint.getArgs()[0];
			HttpRequest requestPayload = HttpUtils.dumpRequestInfo(request);
			// Keep correlated if has one flow id
			if (requestPayload.getHeaders().containsKey(FLOW_ID)) {
				metric.setFlowId(requestPayload.getHeaders().get(FLOW_ID));
			} else if (requestPayload.getAttributes().containsKey(FLOW_ID)) {
				metric.setFlowId(requestPayload.getAttributes().get(FLOW_ID));
			}
			metric.setRequest(requestPayload);
			request.setAttribute(FLOW_ID, metric.getFlowId());
		}
		
		LOGGER.info("Allocating a new metric, with uuid: {} and deepLevel: {}", metric.getFlowId(), metric.getDeepLevel());
	}

	@After("execution(* org.springframework.web.servlet.DispatcherServlet.doService(..))")
	public void afterDoService(JoinPoint joinPoint) {
		HttpServletResponse response = null;
		Metric metric = getCurrentMetric();
		if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 1) {
			response = (HttpServletResponse) joinPoint.getArgs()[1];
			if (metric.getResponse() == null) {
				metric.setResponse(HttpUtils.dumpResponseInfo(response));
			}
		}
		deallocateMetric();
	}

	@Before("(@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) ) && execution(* *(..))")
	public void beforeControllerMethod(JoinPoint joinPoint) {
		Metric metric = getCurrentMetric();
		// try {
		if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
			metric.getRequest()
					.setPayload(HttpUtils.stringify(ReflectionUtils.getParameterByAnnotationClass(joinPoint.getArgs(),
							((MethodSignature) joinPoint.getSignature()).getMethod(), RequestBody.class)));
		}
		metric.setComponent(joinPoint.getSignature().toString());
	}

	@Before("execution(* javax.ws.rs.client.SyncInvoker.post(..))")
	public void beforeRsPost(ProceedingJoinPoint joinPoint) {
		Metric current = getCurrentMetric();
		Metric metric = allocateMetric();
		metric.setFlowId(current.getFlowId());

		Builder builder = (Builder) joinPoint.getTarget();
		builder.header(FLOW_ID, current.getFlowId());
	}

	@After("execution(* javax.ws.rs.client.SyncInvoker.post(..))")
	public void afterRsPost(ProceedingJoinPoint joinPoint) {
		deallocateMetric();
	}

	@AfterReturning(pointcut = "@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))", returning = "retValue")
	public void afterReturningControllerMethod(Object retValue) {
		Metric metric = getCurrentMetric();
		if (metric.getResponse() == null) {
			metric.setResponse(new HttpResponse());
		}
		metric.getResponse().setPayload(HttpUtils.stringify(retValue));
	}

	@AfterThrowing(pointcut = "@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* *(..))", throwing = "exception")
	public void afterThrowingControllerMethod(Throwable exception) {
		Metric metric = getCurrentMetric();
		if (metric.getResponse() == null) {
			metric.setResponse(new HttpResponse());
		}
		metric.getResponse().setPayload(ExceptionUtils.getFullStackTrace(exception));
	}

	private Metric allocateMetric() {
		Metric metric = new Metric();
		try {
			metric.setDeepLevel(local.get().getLast().getDeepLevel() + 1);
		} catch (Exception e) {
			metric.setDeepLevel(0);
		}
		metric.setFlowId(UUID.randomUUID().toString());
		local.get().add(metric);
		metric.startCounting();
		return metric;
	}

	private Metric getCurrentMetric() {
		return local.get().getLast();
	}

	private void deallocateMetric() {
		if (!local.get().isEmpty()) {
			Metric metric = local.get().removeLast();
			metric.stopCounting();
			recAgg.get().addRecord(metric);
			LOGGER.info("Deallocating metric, with uuid: {} and deepLevel: {}", metric.getFlowId(), metric.getDeepLevel());
		}

		if (local.get().isEmpty()) {
			recAgg.get().clearAndFlush();
		}
	}
}