package com.cpf.foundation.id;

import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.context.system.CpfSystemCodes;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import org.springframework.core.env.Environment;
import com.cpf.foundation.runtime.CpfInstanceIdentity;
import com.cpf.foundation.runtime.CpfRuntimeMetadata;
import com.cpf.foundation.runtime.CpfRuntimeSystemCode;

/**
 * CPF 34자리 transactionId의 Foundation 기본 발급 구현입니다.
 *
 * <p>Core는 transactionId의 공개 의미와 검증 계약만 소유하고 실제 시간/sequence 기반 발급은
 * Foundation Runtime이 담당합니다. 동일 runtime instance 안에서는 일자별 sequence를 직렬화하여
 * 중복 발급을 방지합니다. 34자리 포맷의 7자리 {@code instanceToken}은 명시된 {@code cpf.runtime.instance-id}/{@code CPF_RUNTIME_INSTANCE_ID},
 * 미지정 시 Runtime hostname에서 결정적으로 파생하며 실제 instanceId 자체를 대체하는 별도 운영 ID가 아닙니다.</p>
 */
public class DefaultCpfTransactionIdGenerator
        implements CpfTransactionIdGenerator, com.cpf.core.api.transaction.CpfTransactionIdGenerator {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int SEQUENCE_DIGITS = 7;

    private final String issuerCode;
    private final String instanceToken;
    private final Clock clock;
    private LocalDate sequenceDate;
    private long sequence;

    public DefaultCpfTransactionIdGenerator(Environment environment, Clock clock) {
        this(CpfRuntimeMetadata.from(environment), clock);
    }

    public DefaultCpfTransactionIdGenerator(CpfRuntimeMetadata runtime, Clock clock) {
        this(runtime.systemCode(), runtime.instanceId(), SEQUENCE_DIGITS, clock);
    }

    public DefaultCpfTransactionIdGenerator(String systemCode, Clock clock) {
        this(systemCode, CpfInstanceIdentity.instanceId(), SEQUENCE_DIGITS, clock);
    }

    public DefaultCpfTransactionIdGenerator(String systemCode, String instanceId, Clock clock) {
        this(systemCode, instanceId, SEQUENCE_DIGITS, clock);
    }

    public DefaultCpfTransactionIdGenerator(String systemCode, String instanceId, int sequenceDigits, Clock clock) {
        if (sequenceDigits != SEQUENCE_DIGITS) {
            throw new IllegalArgumentException("CPF 표준 transactionId sequence는 7자리로 고정입니다.");
        }
        this.issuerCode = CpfSystemCodes.normalize(systemCode, CpfSystemCodes.CORE);
        this.instanceToken = instanceToken(instanceId);
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    public synchronized String newTransactionId() {
        return generate();
    }

    @Override
    public synchronized String generate() {
        return generate(issuerCode, instanceToken);
    }

    public synchronized String generate(String issuerCode, String targetInstanceId) {
        LocalDateTime now = LocalDateTime.now(clock);
        long next = nextSequence(now.toLocalDate());
        String value = now.format(TIMESTAMP)
                + CpfSystemCodes.normalize(issuerCode, this.issuerCode)
                + instanceToken(targetInstanceId)
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
        return issuerCode;
    }

    @Override
    public String getInstanceToken() {
        return instanceToken;
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

    private static String instanceToken(String value) {
        if (!hasText(value)) throw new IllegalArgumentException("CPF instanceId는 비어 있을 수 없습니다.");
        String normalized = value.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (!normalized.isBlank() && normalized.length() <= 7) {
            return normalized + "0".repeat(7 - normalized.length());
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.trim().getBytes(StandardCharsets.UTF_8));
            BigInteger space = BigInteger.valueOf(36L).pow(7);
            String token = new BigInteger(1, digest).mod(space).toString(36).toUpperCase(Locale.ROOT);
            return "0".repeat(7 - token.length()) + token;
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }


    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
