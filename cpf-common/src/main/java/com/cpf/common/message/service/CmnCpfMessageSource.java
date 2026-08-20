package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfMessageRecord;
import com.cpf.common.message.api.CpfMessageSource;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** CMN_MESSAGE를 직접 소비하는 Common Message Product Service입니다. */
@Service
public final class CmnCpfMessageSource implements CpfMessageSource {
    private static final Locale DEFAULT_LOCALE = Locale.KOREAN;
    private static final Pattern TOKEN = Pattern.compile("\\{([A-Za-z0-9_.-]+)}");
    private final CmnErrorCatalogStore store;
    private final CmnMessageArgumentPolicy argumentPolicy;
    private final Clock clock;

    CmnCpfMessageSource(CmnErrorCatalogStore store, CmnMessageArgumentPolicy argumentPolicy) {
        this(store, argumentPolicy, Clock.systemUTC());
    }

    @org.springframework.beans.factory.annotation.Autowired
    CmnCpfMessageSource(CmnErrorCatalogStore store, CmnMessageArgumentPolicy argumentPolicy, Clock clock) {
        this.store = store; this.argumentPolicy = argumentPolicy; this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    public String getMessage(String messageCode, Locale locale, Map<String, Object> arguments) {
        if (messageCode == null || messageCode.isBlank()) throw new IllegalArgumentException("messageCode is required");
        Locale requested = locale == null || locale.getLanguage().isBlank() ? DEFAULT_LOCALE : locale;
        CpfMessageRecord message = active(store.message(messageCode, requested));
        if (message == null && !DEFAULT_LOCALE.getLanguage().equals(requested.getLanguage())) {
            message = active(store.message(messageCode, DEFAULT_LOCALE));
        }
        if (message == null) throw new IllegalArgumentException("CPF message is missing, disabled or not effective");
        var prepared = argumentPolicy.prepare(arguments, message.parameterSchemaJson(),
                "Y".equalsIgnoreCase(message.escapeHtmlYn()), "Y".equalsIgnoreCase(message.maskArgumentsYn()));
        if (!prepared.valid()) throw new IllegalArgumentException("CPF message arguments violate the registered schema");
        return format(message.externalMessage(), prepared.arguments());
    }

    private CpfMessageRecord active(CpfMessageRecord value) {
        return value != null && value.activeAt(clock.instant()) ? value : null;
    }

    private String format(String template, Map<String, Object> arguments) {
        if (template == null) return "";
        Matcher matcher = TOKEN.matcher(template);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            Object value = arguments == null ? null : arguments.get(matcher.group(1));
            matcher.appendReplacement(out, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(out);
        return out.length() > 2000 ? out.substring(0, 2000) : out.toString();
    }
}
