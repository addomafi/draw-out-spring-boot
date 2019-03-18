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
 * A helper class to aggregate and flush records to a Kinesis Stream.
 * 
 * Details are covered on {<a href="https://github.com/awslabs/kinesis-aggregation">AWS Lambda KPL</a>}
 *
 * @author Adauto Martins adauto.martins@ideotech.com.br
 */
public class KinesisRecordAggregation {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(KinesisRecordAggregation.class);

	private static final String KINESIS_STREAM = PropertiesUtil.getInstance().getValue("drawout.kinesis.stream");
	private static final String KINESIS_PARTITION_NAME = PropertiesUtil.getInstance().getValue("drawout.kinesis.partition.name");

	private final RecordAggregator recordAgg;
	private final KinesisAsyncClient kinesisClient;
	private PutRecordRequest.Builder kinesisRecordBuilder;

	public KinesisRecordAggregation() {
		super();
		// An record aggregator for Kinesis KPL, should be an instance per thread
		recordAgg = new RecordAggregator();
		recordAgg.onRecordComplete((aggRecord) -> {
			putRecord(aggRecord);
		});
		// Prefix to compose partition name
		String partitionName = "drawout";
		// If necessary append custom data to the partition name
		if (KINESIS_PARTITION_NAME != null && !KINESIS_PARTITION_NAME.isEmpty())
			partitionName += "-" + KINESIS_PARTITION_NAME;
		// Kinesis record builder, should be an instance per thread
		kinesisRecordBuilder = PutRecordRequest.builder().streamName(KINESIS_STREAM).partitionKey(partitionName);
		// Kinesis async client, should be an instance per thread
		kinesisClient = KinesisAsyncClient.builder()
				.httpClientBuilder(
						NettyNioAsyncHttpClient.builder().maxConcurrency(100).maxPendingConnectionAcquires(10_000))
				.build();
	}

	/**
	 * Put record into Kinesis Stream
	 *
	 * @param aggRecord An aggregated KPL record
	 */
	private void putRecord(AggRecord aggRecord) {
		if (KINESIS_STREAM == null || KINESIS_STREAM.isEmpty()) {
			LOGGER.error("The parameter drawout.kinesis.stream wasn't defined, flush to Kinesis will be disabled");
		} else {
			PutRecordRequest putRecordRequest = kinesisRecordBuilder
					.data(SdkBytes.fromByteArray(aggRecord.toRecordBytes())).build();
			// Updates
			PutRecordResponse putRecordResponse;
			try {
				putRecordResponse = kinesisClient.putRecord(putRecordRequest).get();
				// Updates record builder to keep records ordered on stream
				kinesisRecordBuilder = kinesisRecordBuilder.sequenceNumberForOrdering(putRecordResponse.sequenceNumber());
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.warn("Getting error to send records to kinesis.", e);
			}
		}
	}

	/**
	 * Adds a single data into a record aggregator (KPL)
	 *
	 * @param value Return value
	 */
	public void addRecord(Object value) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			recordAgg.addUserRecord("metric", mapper.writeValueAsBytes(value));
		} catch (JsonProcessingException jpe) {
			LOGGER.warn("Error to convert Java object to JSON.", jpe);
		} catch (Exception e) {
			LOGGER.warn("An unexpected error occurred to insert data into KPL aggregated records.", e);
		}
	}

	/**
	 * Clear the record aggregator and flush data to Kinesis Stream
	 */
	public void clearAndFlush() {
		putRecord(recordAgg.clearAndGet());
	}
}
