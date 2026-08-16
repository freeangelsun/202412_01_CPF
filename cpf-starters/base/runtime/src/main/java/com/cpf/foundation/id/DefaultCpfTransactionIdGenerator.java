package com.cpf.foundation.id;

import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.context.system.CpfSystemCodes;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.core.env.Environment;

/**
 * CPF 34자리 transactionId의 Foundation 기본 생성 구현입니다.
 *
 * <p>Core는 transactionId의 공개 의미와 검증 계약만 소유하고 실제 시간/sequence 기반 발급은
 * Foundation Runtime이 담당합니다. 동일 {@code wasId} 안에서는 일자별 sequence를 직렬화하여
 * 중복 발급을 방지하며, 다중 인스턴스 환경에서는 인스턴스마다 고유한 7자리 {@code wasId}를
 * 설정해야 합니다.</p>
 */
public class DefaultCpfTransactionIdGenerator
        implements CpfTransactionIdGenerator, com.cpf.core.api.transaction.CpfTransactionIdGenerator {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int SEQUENCE_DIGITS = 7;

    private final String systemCode;
    private final String wasId;
    private final Clock clock;
    private LocalDate sequenceDate;
    private long sequence;

    public DefaultCpfTransactionIdGenerator(Environment environment, Clock clock) {
        this(resolveSystemCode(environment), environment.getProperty("cpf.framework.was-id", "local01"),
                SEQUENCE_DIGITS, clock);
    }

    public DefaultCpfTransactionIdGenerator(String systemCode, String wasId, Clock clock) {
        this(systemCode, wasId, SEQUENCE_DIGITS, clock);
    }

    public DefaultCpfTransactionIdGenerator(String systemCode, String wasId, int sequenceDigits, Clock clock) {
        if (sequenceDigits != SEQUENCE_DIGITS) {
            throw new IllegalArgumentException("CPF 표준 transactionId sequence는 7자리로 고정입니다.");
        }
        this.systemCode = CpfSystemCodes.normalize(systemCode, CpfSystemCodes.CORE);
        this.wasId = requireWasId(wasId);
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    public synchronized String newTransactionId() {
        return generate();
    }

    @Override
    public synchronized String generate() {
        return generate(systemCode, wasId);
    }

    public synchronized String generate(String targetSystemCode, String targetWasId) {
        LocalDateTime now = LocalDateTime.now(clock);
        long next = nextSequence(now.toLocalDate());
        String value = now.format(TIMESTAMP)
                + CpfSystemCodes.normalize(targetSystemCode, systemCode)
                + requireWasId(targetWasId)
                + String.format("%07d", next);
        return CpfTransactionIds.requireCanonical(value);
    }

    @Override
    public String generateOrUse(String incomingTransactionId) {
        return isValid(incomingTransactionId) ? incomingTransactionId : generate();
    }

    @Override
    public boolean isValid(String transactionId) {
        return CpfTransactionIds.isCanonical(transactionId);
    }

    @Override
    public String getModuleId() {
        return systemCode;
    }

    @Override
    public String getWasId() {
        return wasId;
    }

    private long nextSequence(LocalDate date) {
        if (!date.equals(sequenceDate)) {
            sequenceDate = date;
            sequence = 0L;
        }
        long next = ++sequence;
        if (next > 9_999_999L) {
            throw new IllegalStateException("transactionId 일자 sequence가 7자리 한도를 초과했습니다.");
        }
        return next;
    }

    private static String resolveSystemCode(Environment environment) {
        String generated = environment.getProperty("cpf.generated-domain.system-code");
        if (hasText(generated)) {
            return generated;
        }
        String configured = environment.getProperty("cpf.framework.module-id");
        if (hasText(configured)) {
            return configured;
        }
        return environment.getProperty("spring.application.name", CpfSystemCodes.CORE);
    }

    private static String requireWasId(String value) {
        String normalized = hasText(value) ? value.trim().replaceAll("[^A-Za-z0-9]", "") : "local01";
        if (normalized.length() != 7) {
            throw new IllegalArgumentException("CPF wasId는 영문/숫자 7자리여야 합니다. wasId=" + normalized);
        }
        return normalized;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
