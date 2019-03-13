package br.com.ideotech.drawout.kinesis;

import java.util.concurrent.ExecutionException;

import com.amazonaws.kinesis.agg.AggRecord;
import com.amazonaws.kinesis.agg.RecordAggregator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.ideotech.drawout.utils.PropertiesUtil;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

/**
 * A helper to aggregate and flush records to a Kinesis Stream.
 * <p/>
 * Details are covered on {@see <a href="https://github.com/awslabs/kinesis-aggregation">AWS Lambda KPL</a>}
 * 
 * @author Adauto Martins <adauto.martins@ideotech.com.br>
 */
public class KinesisRecordAggregation {
	
	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(KinesisRecordAggregation.class);

	private static final PropertiesUtil PROPERTIES = new PropertiesUtil();
	private final RecordAggregator RECORD_AGG;
	private final KinesisAsyncClient KINESIS_CLIENT;
	private PutRecordRequest.Builder kinesisPutRecordBuilder;

	private final String KINESIS_STREAM = PROPERTIES.getValue("drawout.kinesis.stream");
	private final String KINESIS_PARTITION_NAME = PROPERTIES.getValue("drawout.kinesis.partition.name");

	public KinesisRecordAggregation() {
		super();
		RECORD_AGG = new RecordAggregator();
		RECORD_AGG.onRecordComplete((aggRecord) -> {
			putRecord(aggRecord);
		});
		String partitionName = "drawout";
		if (KINESIS_PARTITION_NAME != null && !KINESIS_PARTITION_NAME.isEmpty())
			partitionName += "-" + KINESIS_PARTITION_NAME;
		kinesisPutRecordBuilder = PutRecordRequest.builder().streamName(KINESIS_STREAM).partitionKey(partitionName);
		KINESIS_CLIENT = KinesisAsyncClient.builder()
				.httpClientBuilder(
						NettyNioAsyncHttpClient.builder().maxConcurrency(100).maxPendingConnectionAcquires(10_000))
				.build();
	}

	private void putRecord(AggRecord aggRecord) {
		if (KINESIS_STREAM == null || KINESIS_STREAM.isEmpty()) {
			LOGGER.error("The parameter drawout.kinesis.stream wasn't defined, flush to Kinesis will be disabled");
		} else {
			PutRecordRequest putRecordRequest = kinesisPutRecordBuilder
					.data(SdkBytes.fromByteArray(aggRecord.toRecordBytes())).build();
			// Updates 
			PutRecordResponse putRecordResponse;
			try {
				putRecordResponse = KINESIS_CLIENT.putRecord(putRecordRequest).get();
				kinesisPutRecordBuilder = kinesisPutRecordBuilder.sequenceNumberForOrdering(putRecordResponse.sequenceNumber());
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.warn("Getting error to send records to kinesis.", e);
			}
		}
	}

	public void addRecord(Object value) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			RECORD_AGG.addUserRecord("metric", mapper.writeValueAsBytes(value));
		} catch (JsonProcessingException jpe) {
			LOGGER.warn("Error to convert Java object to JSON.", jpe);
		} catch (Exception e) {
			LOGGER.warn("An unexpected error occurred to insert data into KPL aggregated records.", e);
		}
	}

	public void clearAndFlush() {
		putRecord(RECORD_AGG.clearAndGet());
	}
}
