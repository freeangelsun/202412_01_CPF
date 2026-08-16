package com.cpf.core.api.error;

import java.util.Objects;

/**
 * Transport/DB/Broker/Batch 기술상태를 포함하지 않는 CPF 오류 의미 계약입니다.
 *
 * <p>Core는 상태코드, 메시지코드, 기술중립 분류, 외부 노출 범위와 재처리 의미만 보유합니다.
 * HTTP status, SQL vendor code, DLQ/Retry 정책, Batch ExitStatus는 각 Capability Owner가 매핑합니다.</p>
 */
public interface CpfErrorDefinition {
    String statusCode();
    String messageCode();
    Category category();
    String defaultExternalMessage();
    String defaultInternalMessage();

    default RetryDisposition retryDisposition() { return RetryDisposition.NEVER; }
    default boolean retryable() { return retryDisposition() == RetryDisposition.SAFE; }
    default Exposure exposure() {
        return switch (category()) {
            case VALIDATION, NOT_FOUND, CONFLICT, RATE_LIMIT, AUTHENTICATION, AUTHORIZATION, BUSINESS -> Exposure.SAFE_MESSAGE_ONLY;
            case EXTERNAL, INFRASTRUCTURE, INTERNAL -> Exposure.GENERIC_MESSAGE_ONLY;
        };
    }

    default String getStatusCode() { return statusCode(); }
    default String getMessageCode() { return messageCode(); }
    default String getDefaultExternalMessage() { return defaultExternalMessage(); }
    default String getDefaultInternalMessage() { return defaultInternalMessage(); }
    default String getExternalMessageKey() { return messageCode() + ".external"; }
    default String getInternalMessageKey() { return messageCode() + ".internal"; }

    /** 독립 top-level 타입을 늘리지 않는 기술중립 오류 분류입니다. */
    enum Category {
        VALIDATION, NOT_FOUND, CONFLICT, RATE_LIMIT, AUTHENTICATION, AUTHORIZATION,
        BUSINESS, EXTERNAL, INFRASTRUCTURE, INTERNAL
    }

    /** 상위 Owner가 선택할 수 있는 기술중립 후속 처리 의미입니다. */
    enum RetryDisposition { NEVER, SAFE, RECONCILE, UNKNOWN }

    /** 외부 응답에 허용되는 메시지 노출 범위입니다. */
    enum Exposure { SAFE_MESSAGE_ONLY, GENERIC_MESSAGE_ONLY }

    /**
     * 업무/기관별 동적 오류 정의입니다. Enum 추가 없이 안정된 Core contract를 확장합니다.
     * Message catalog lookup 자체는 Common Message/Boundary Owner가 담당합니다.
     */
    record Dynamic(
            String statusCode,
            String messageCode,
            String messageKeyPrefix,
            Category category,
            RetryDisposition retryDisposition,
            Exposure exposure,
            String defaultExternalMessage,
            String defaultInternalMessage) implements CpfErrorDefinition {
        public Dynamic {
            statusCode = require(statusCode, "statusCode");
            messageCode = require(messageCode, "messageCode");
            messageKeyPrefix = require(messageKeyPrefix, "messageKeyPrefix");
            category = Objects.requireNonNull(category, "category");
            retryDisposition = retryDisposition == null ? RetryDisposition.NEVER : retryDisposition;
            exposure = exposure == null ? defaultExposure(category) : exposure;
            defaultExternalMessage = require(defaultExternalMessage, "defaultExternalMessage");
            defaultInternalMessage = require(defaultInternalMessage, "defaultInternalMessage");
        }

        /** Dynamic 작업을 CPF 표준 계약에 따라 수행한다. */
        public Dynamic(String statusCode, String messageCode, String messageKeyPrefix,
                       Category category, RetryDisposition retryDisposition,
                       String external, String internal) {
            this(statusCode, messageCode, messageKeyPrefix, category, retryDisposition,
                    defaultExposure(category), external, internal);
        }

        /** Dynamic 작업을 CPF 표준 계약에 따라 수행한다. */
        public Dynamic(String statusCode, String messageCode, String messageKeyPrefix,
                       Category category, boolean retryable,
                       String external, String internal) {
            this(statusCode, messageCode, messageKeyPrefix, category,
                    retryable ? RetryDisposition.SAFE : RetryDisposition.NEVER,
                    defaultExposure(category), external, internal);
        }

        @Override public Exposure exposure() { return exposure; }

        public static Dynamic business(String key, String external, String internal) {
            return from(CpfErrorCode.BUSINESS_RULE_VIOLATION, key, external, internal);
        }

        /** duplicate 작업을 CPF 표준 계약에 따라 수행한다. */
        public static Dynamic duplicate(String key, String external, String internal) {
            return from(CpfErrorCode.DUPLICATE, key, external, internal);
        }

        public static Dynamic from(CpfErrorDefinition base, String key, String external, String internal) {
            Objects.requireNonNull(base, "base");
            return new Dynamic(base.statusCode(), base.messageCode(), key, base.category(),
                    base.retryDisposition(), base.exposure(), external, internal);
        }

        private static Exposure defaultExposure(Category category) {
            return switch (Objects.requireNonNull(category, "category")) {
                case VALIDATION, NOT_FOUND, CONFLICT, RATE_LIMIT, AUTHENTICATION, AUTHORIZATION, BUSINESS -> Exposure.SAFE_MESSAGE_ONLY;
                case EXTERNAL, INFRASTRUCTURE, INTERNAL -> Exposure.GENERIC_MESSAGE_ONLY;
            };
        }

        private static String require(String value, String name) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
            return value.trim();
        }
    }
}
