package cpf.pfw.common.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class TransactionIdGenerator {

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
                environment.getProperty("cpf.framework.transaction-id.sequence-digits", Integer.class, DEFAULT_SEQUENCE_DIGITS),
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
        return Pattern.matches(pattern, transactionId);
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
            return configured.replace("cpf-", "");
        }

        String appName = environment.getProperty("spring.application.name", "PFW");
        return appName.replace("cpf-", "");
    }

    private static String normalizeModuleId(String value) {
        String normalized = normalizeAlphaNumeric(value, "PFW").toUpperCase(Locale.ROOT);
        if (normalized.length() >= MODULE_ID_LENGTH) {
            return normalized.substring(0, MODULE_ID_LENGTH);
        }
        return String.format("%-" + MODULE_ID_LENGTH + "s", normalized).replace(' ', 'X');
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
        if (value < 4 || value > 12) {
            return DEFAULT_SEQUENCE_DIGITS;
        }
        return value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

