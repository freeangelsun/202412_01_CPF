package com.cpf.core.common.logging;

import com.cpf.core.api.transaction.CpfTransactionIdGenerator;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.core.common.system.CpfSystemCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * CPF transactionId의 기본 발급 구현입니다.
 *
 * <p>Public 계약은 {@link CpfTransactionIdGenerator}이며 이 구현은 Core 내부에서
 * SystemCode/wasId/일자별 sequence를 결합해 반드시 34자리 ID를 발급합니다.</p>
 */
@Component
public class TransactionIdGenerator implements CpfTransactionIdGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int MODULE_ID_LENGTH = 3;
    private static final int WAS_ID_LENGTH = 7;
    private static final int DEFAULT_SEQUENCE_DIGITS = 7;

    private final String moduleId;
    private final String wasId;
    private final int sequenceDigits;
    private final Clock clock;

    private LocalDate currentDate;
    private long sequence;

    @Autowired
    public TransactionIdGenerator(Environment environment) {
        this(
                resolveModuleId(environment),
                environment.getProperty("cpf.framework.was-id", "local01"),
                canonicalSequenceDigits(environment),
                Clock.systemDefaultZone());
    }

    public TransactionIdGenerator(String moduleId, String wasId, int sequenceDigits, Clock clock) {
        this.moduleId = normalizeModuleId(moduleId);
        this.wasId = normalizeWasId(wasId);
        this.sequenceDigits = normalizeSequenceDigits(sequenceDigits);
        this.clock = clock;
    }

    public synchronized String generate() {
        return generate(moduleId, wasId);
    }

    public synchronized String generateOrUse(String incomingTransactionId) {
        return isValid(incomingTransactionId) ? incomingTransactionId : generate();
    }

    public synchronized String generate(String moduleId, String wasId) {
        LocalDateTime now = LocalDateTime.now(clock);
        long nextSequence = nextSequence(now.toLocalDate());
        return now.format(TIMESTAMP_FORMAT)
                + normalizeModuleId(moduleId)
                + normalizeWasId(wasId)
                + String.format("%0" + sequenceDigits + "d", nextSequence);
    }

    public boolean isValid(String transactionId) {
        return isValid(transactionId, sequenceDigits);
    }

    public static boolean isValid(String transactionId, int sequenceDigits) {
        if (!hasText(transactionId)) {
            return false;
        }

        int normalizedSequenceDigits = normalizeSequenceDigits(sequenceDigits);
        String pattern = "^\\d{17}[A-Z0-9]{" + MODULE_ID_LENGTH + "}[A-Za-z0-9]{"
                + WAS_ID_LENGTH + "}\\d{" + normalizedSequenceDigits + "}$";
        return normalizedSequenceDigits == DEFAULT_SEQUENCE_DIGITS
                ? CpfTransactionIds.isCanonical(transactionId)
                : Pattern.matches(pattern, transactionId);
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getWasId() {
        return wasId;
    }

    private long nextSequence(LocalDate date) {
        if (!date.equals(currentDate)) {
            currentDate = date;
            sequence = 0;
        }

        long next = ++sequence;
        long max = (long) Math.pow(10, sequenceDigits) - 1;
        if (next > max) {
            throw new IllegalStateException("Transaction ID daily sequence overflow. sequenceDigits=" + sequenceDigits);
        }
        return next;
    }

    private static String resolveModuleId(Environment environment) {
        String configured = environment.getProperty("cpf.framework.module-id");
        if (hasText(configured)) {
            return CpfSystemCodes.normalize(configured, CpfSystemCodes.CORE);
        }

        String appName = environment.getProperty("spring.application.name", CpfSystemCodes.CORE);
        return CpfSystemCodes.normalize(appName, CpfSystemCodes.CORE);
    }

    private static String normalizeModuleId(String value) {
        return CpfSystemCodes.normalize(value, CpfSystemCodes.CORE);
    }

    private static String normalizeWasId(String value) {
        String normalized = normalizeAlphaNumeric(value, "local01");
        if (normalized.length() != WAS_ID_LENGTH) {
            throw new IllegalArgumentException("CPF wasId는 영문/숫자 7자리여야 합니다. wasId=" + normalized);
        }
        return normalized;
    }

    private static String normalizeAlphaNumeric(String value, String fallback) {
        String target = hasText(value) ? value : fallback;
        String normalized = target.replaceAll("[^A-Za-z0-9]", "");
        return hasText(normalized) ? normalized : fallback;
    }

    private static int normalizeSequenceDigits(int value) {
        if (value != DEFAULT_SEQUENCE_DIGITS) {
            throw new IllegalArgumentException("CPF 표준 transactionId sequence는 7자리로 고정입니다. sequenceDigits=" + value);
        }
        return DEFAULT_SEQUENCE_DIGITS;
    }

    private static int canonicalSequenceDigits(Environment environment) {
        Integer configured = environment.getProperty(
                "cpf.framework.transaction-id.sequence-digits", Integer.class, DEFAULT_SEQUENCE_DIGITS);
        if (configured == null || configured != DEFAULT_SEQUENCE_DIGITS) {
            throw new IllegalStateException(
                    "cpf.framework.transaction-id.sequence-digits는 표준 34자리 transactionId를 위해 7이어야 합니다.");
        }
        return DEFAULT_SEQUENCE_DIGITS;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
