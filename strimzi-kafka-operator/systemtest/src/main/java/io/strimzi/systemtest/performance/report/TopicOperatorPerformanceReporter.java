/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.systemtest.performance.report;

import io.strimzi.systemtest.performance.PerformanceConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;


/**
 * Extends the {@link BasePerformanceReporter} to report on performance metrics specific to the Topic Operator.
 * <p>
 * This implementation customizes the path resolution for performance logs by incorporating Topic Operator-specific
 * attributes such as maximum batch size, maximum linger milliseconds, and whether client instances are enabled.
 */
public class TopicOperatorPerformanceReporter extends BasePerformanceReporter {
    private static final Logger LOGGER = LogManager.getLogger(TopicOperatorPerformanceReporter.class);

    @Override
    protected Path resolveComponentUseCasePathDir(Path performanceLogDir, String useCaseName, Map<String, Object> performanceAttributes) {
        final String maxBatchSize = performanceAttributes.getOrDefault(PerformanceConstants.TOPIC_OPERATOR_IN_MAX_BATCH_SIZE, "").toString();
        final String maxBatchLingerMs = performanceAttributes.getOrDefault(PerformanceConstants.TOPIC_OPERATOR_IN_MAX_BATCH_LINGER_MS, "").toString();
        final String numberOfTopics = performanceAttributes.getOrDefault(PerformanceConstants.TOPIC_OPERATOR_IN_NUMBER_OF_TOPICS, 0).toString();
        final boolean clientsEnabled = !performanceAttributes.getOrDefault(PerformanceConstants.TOPIC_OPERATOR_IN_NUMBER_OF_CLIENT_INSTANCES, "0").equals("0");

        final StringBuilder dirPathBuilder = new StringBuilder();
        dirPathBuilder.append(useCaseName)
            .append("/max-batch-size-").append(maxBatchSize)
            .append("-max-linger-time-").append(maxBatchLingerMs)
            .append("-with-clients-").append(clientsEnabled);

        if (!numberOfTopics.equals("0")) {
            dirPathBuilder.append("-number-of-topics-").append(numberOfTopics);
        }

        // Defines the strategy for Kafka topic processing, either concurrently by topic or by batch.
        String processType = null;

        if (useCaseName.equals(PerformanceConstants.TOPIC_OPERATOR_FIXED_SIZE_OF_EVENTS_USE_CASE)) {
            processType = performanceAttributes.get(PerformanceConstants.TOPIC_OPERATOR_IN_PROCESS_TYPE).toString().toLowerCase(Locale.ROOT);


            // it would be either "topic-concurrent" or "batch-concurrent"
            dirPathBuilder.append("-process-type-").append(processType);
        }

        final Path topicOperatorUseCasePathDir = performanceLogDir.resolve(dirPathBuilder.toString());

        LOGGER.info("Resolved performance log directory: {} for use case '{}'. Max batch size: {}, Max linger time: {}, " +
                "Clients enabled: {}, Number of topics: {}, Process type: {}",
            topicOperatorUseCasePathDir, useCaseName, maxBatchSize, maxBatchLingerMs, clientsEnabled,
            numberOfTopics.equals("0") ? "not included" : numberOfTopics, processType == null ? "not included" : processType);

        return topicOperatorUseCasePathDir;
    }
}
