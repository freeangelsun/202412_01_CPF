package com.cpf.foundation.id;

import com.cpf.core.api.transaction.CpfTransactionIds;
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
        // 거래ID issuer 의 source 는 **최초 신뢰 거래 기동점의 canonical ChannelCode** 다
        // (Harness 30.7). Business/Generated Domain 이 기동하면 그 Domain 의 SystemCode 값이
        // 내부 hop ChannelCode 값이므로 같은 값이 들어온다. Platform(ADM 등)은 운영 ChannelCode 다.
        //
        // 기동 시점에는 이 값이 없을 수 있다. 1-WAS 는 System 도 Channel 도 아닌 배치 topology 라
        // 자기 Identity 를 가지지 않으며(Harness 30.5), 그 안의 각 Domain 이 자기 Identity 로 거래를
        // 기동한다. 따라서 issuer 는 **거래를 실제로 발급하는 시점**에 확정하고, 그때 확정할 수
        // 없으면 fail-closed 한다. 축약/패딩/기본값으로 만들어내지 않는다.
        this.issuerCode = hasText(systemCode) ? requireIssuerChannelCode(systemCode) : null;
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
                + resolveIssuer(issuerCode)
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


    /**
     * 거래를 발급하는 시점에 issuer 를 확정합니다.
     *
     * <p>호출자가 넘긴 canonical ChannelCode 를 우선하고, 없으면 이 Runtime 이 기동 시 확정한 값을
     * 쓴다. 둘 다 없으면 이 Runtime 은 스스로 거래를 기동할 Identity 가 없다는 뜻이므로 fail-closed
     * 한다(Harness 30.7). 값을 만들어내지 않는다.</p>
     */
    private String resolveIssuer(String issuerCode) {
        if (hasText(issuerCode)) return requireIssuerChannelCode(issuerCode);
        if (hasText(this.issuerCode)) return this.issuerCode;
        throw new IllegalStateException(
                "CPF transactionId issuer is unresolved. This runtime has no canonical ChannelCode of its own"
                        + " (for example a same-JVM topology), so the initiating channel must supply one.");
    }

    /**
     * 거래ID issuer 로 쓸 canonical ChannelCode 를 검증합니다.
     *
     * <p>CPF 표준 거래ID 의 issuer 자리는 3자리다. 따라서 **거래를 기동할 수 있는 canonical
     * ChannelCode 자체가 3자리 규격을 만족해야 한다**(Harness 30.7). 규격에 맞지 않는 값을
     * 잘라내거나 채워서 issuer 를 만들지 않는다. issuer 전용 Identity Namespace 도 만들지 않는다.</p>
     */
    private static String requireIssuerChannelCode(String channelCode) {
        if (!hasText(channelCode)) {
            throw new IllegalStateException(
                    "CPF transactionId issuer requires the canonical ChannelCode of the initiating trusted channel.");
        }
        String normalized = channelCode.trim().toUpperCase(java.util.Locale.ROOT);
        if (normalized.length() != 3 || !normalized.chars().allMatch(Character::isLetterOrDigit)) {
            throw new IllegalStateException(
                    "CPF transactionId issuer must be a 3-character canonical ChannelCode; truncation and padding"
                            + " are not allowed. value=" + channelCode);
        }
        return normalized;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
