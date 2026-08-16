package com.cpf.messaging.reliability.api.jdbc;

import com.cpf.messaging.api.CpfBrokerPublishRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Provider-neutral validation for user supplied broker headers.
 *
 * <p>The strictest common transport projection is used so one request cannot be accepted by one
 * provider and later fail or overwrite CPF metadata when routed to another provider. Validation is
 * performed before an outbox write and again immediately before provider routing.</p>
 */
final class CpfBrokerHeaderPolicy {
    private static final Pattern PORTABLE_NAME = Pattern.compile("[A-Za-z0-9._-]{1,128}");
    private static final int MAX_HEADER_COUNT = 64;
    private static final int MAX_HEADER_VALUE_LENGTH = 4096;
    private static final Set<String> RESERVED_NAMES = Set.of(
            "cpf-message-id", "cpf-transaction-id", "cpf-idempotency-key",
            "cpf-content-type", "cpf-segment-id", "cpf-producer-module",
            "cpf-consumer-module",
            "cpf_message_id", "cpf_transaction_id", "cpf_idempotency_key",
            "cpf_content_type", "cpf_segment_id", "cpf_producer_module",
            "cpf_consumer_module",
            "cpfmessageid", "cpftransactionid", "cpfidempotencykey",
            "cpfcontenttype", "cpfsegmentid", "cpfproducermodule",
            "cpfconsumermodule",
            "jmscorrelationid", "jmsmessageid", "jmstimestamp", "jmsdestination",
            "jmsdeliverymode", "jmsredelivered", "jmstype", "jmsexpiration",
            "jmspriority");

    private CpfBrokerHeaderPolicy() {
    }

    static CpfBrokerPublishRequest validatedRequest(CpfBrokerPublishRequest request) {
        Objects.requireNonNull(request, "request");
        Map<String, String> headers = validatedSnapshot(request.headers());
        return new CpfBrokerPublishRequest(
                request.messageId(),
                request.topic(),
                request.key(),
                request.payload(),
                request.contentType(),
                request.transactionId(),
                request.segmentId(),
                request.producerModule(),
                request.consumerModule(),
                request.idempotencyKey(),
                headers,
                request.attributes());
    }

    static Map<String, String> validatedSnapshot(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        if (source.size() > MAX_HEADER_COUNT) {
            throw new IllegalArgumentException(
                    "Broker header count exceeds portable limit: " + source.size());
        }
        Map<String, String> snapshot = new LinkedHashMap<>();
        Map<String, String> projectedNames = new LinkedHashMap<>();
        for (Map.Entry<String, String> header : source.entrySet()) {
            String name = Objects.requireNonNull(header.getKey(), "broker header name");
            String value = Objects.requireNonNull(
                    header.getValue(), "broker header value for " + name);
            validateValue(name, value);
            if (!name.equals(name.trim())) {
                throw new IllegalArgumentException(
                        "Broker header name must not contain surrounding whitespace: " + name);
            }
            if (!PORTABLE_NAME.matcher(name).matches()) {
                throw new IllegalArgumentException(
                        "Broker header name is not portable across CPF providers: " + name);
            }
            String lowerName = name.toLowerCase(Locale.ROOT);
            String projectedName = providerProjection(name);
            if (RESERVED_NAMES.contains(lowerName) || RESERVED_NAMES.contains(projectedName)) {
                throw new SecurityException(
                        "CPF reserved broker header cannot be overridden: " + name);
            }
            String previous = projectedNames.putIfAbsent(projectedName, name);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "Broker headers normalize to the same provider name: "
                                + previous + " / " + name);
            }
            snapshot.put(name, value);
        }
        return Collections.unmodifiableMap(snapshot);
    }

    private static void validateValue(String name, String value) {
        if (value.length() > MAX_HEADER_VALUE_LENGTH) {
            throw new IllegalArgumentException(
                    "Broker header value exceeds portable limit: " + name);
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '\r' || character == '\n' || Character.isISOControl(character)) {
                throw new IllegalArgumentException(
                        "Broker header value contains a control character: " + name);
            }
        }
    }

    private static String providerProjection(String name) {
        String projected = name.replaceAll("[^A-Za-z0-9_]", "_");
        if (projected.isEmpty() || Character.isDigit(projected.charAt(0))) {
            projected = "cpf_" + projected;
        }
        return projected.toLowerCase(Locale.ROOT);
    }
}
