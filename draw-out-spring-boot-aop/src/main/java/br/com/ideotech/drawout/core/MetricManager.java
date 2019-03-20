package br.com.ideotech.drawout.core;

import java.util.LinkedList;
import java.util.UUID;

import br.com.ideotech.drawout.kinesis.KinesisRecordAggregation;
import br.com.ideotech.drawout.model.Metric;

public class MetricManager {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MetricManager.class);

	private static ThreadLocal<LinkedList<Metric>> local = ThreadLocal.withInitial(() -> new LinkedList<Metric>());
	private static ThreadLocal<KinesisRecordAggregation> recAgg = ThreadLocal.withInitial(() -> new KinesisRecordAggregation());

	public static Metric allocateMetric() {
		return allocateMetric(null);
	}

	public static Metric allocateMetric(String currentFlowId) {
		Metric metric = new Metric();
		try {
			metric.setDeepLevel(local.get().getLast().getDeepLevel() + 1);
		} catch (Exception e) {
			metric.setDeepLevel(0);
		}

		if (currentFlowId == null || currentFlowId.isEmpty()) {
			metric.setFlowId(UUID.randomUUID().toString());
		} else {
			metric.setFlowId(currentFlowId);
		}

		LOGGER.info("Allocating a new metric, with uuid: {} and deepLevel: {}", metric.getFlowId(),
				metric.getDeepLevel());

		local.get().add(metric);
		metric.startCounting();

		return metric;
	}

	public static Metric getCurrentMetric() {
		if (local.get() != null && !local.get().isEmpty()) {
			return local.get().getLast();
		}
		return null;
	}

	public static void deallocateMetric() {
		if (local.get() != null && !local.get().isEmpty()) {
			Metric metric = local.get().removeLast();
			metric.stopCounting();
			recAgg.get().addRecord(metric);
			LOGGER.info("Deallocating metric, with uuid: {} and deepLevel: {}", metric.getFlowId(),
					metric.getDeepLevel());
		}
	}

}
