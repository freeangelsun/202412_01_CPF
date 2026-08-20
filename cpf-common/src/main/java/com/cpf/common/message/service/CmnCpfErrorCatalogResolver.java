package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfErrorCatalogResolver;
import com.cpf.common.message.api.CpfErrorCatalogSignalSink;
import com.cpf.common.message.api.CpfMessageRecord;
import com.cpf.common.message.api.CpfResolvedError;
import com.cpf.common.message.api.CpfResponseCodeRecord;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfErrorDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** CMN_RESPONSE_CODE → CMN_MESSAGE를 해석하는 Common Product Service입니다. */
@Primary
@Component
@ConditionalOnProperty(prefix = "cpf.common", name = "runtime-mode", havingValue = "product", matchIfMissing = true)
public final class CmnCpfErrorCatalogResolver implements CpfErrorCatalogResolver {
    private static final Locale DEFAULT_LOCALE = Locale.KOREAN;
    private static final Pattern TOKEN = Pattern.compile("\\{([A-Za-z0-9_.-]+)}");

    private final CmnErrorCatalogStore cache;
    private final CpfErrorCatalogSignalSink signals;
    private final CmnMessageArgumentPolicy argumentPolicy;
    private final Clock clock;

    public CmnCpfErrorCatalogResolver(CmnErrorCatalogStore cache, CpfErrorCatalogSignalSink signals, CmnMessageArgumentPolicy argumentPolicy) {
        this(cache, signals, argumentPolicy, Clock.systemUTC());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public CmnCpfErrorCatalogResolver(CmnErrorCatalogStore cache, CpfErrorCatalogSignalSink signals, CmnMessageArgumentPolicy argumentPolicy, Clock clock) {
        this.cache = cache; this.signals = signals; this.argumentPolicy = argumentPolicy;
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CpfResolvedError resolve(String errorReference, CpfErrorDefinition fallback, Locale locale,
                                    Map<String, Object> arguments) {
        if (fallback == null) throw new IllegalArgumentException("fallback is required");
        String reference = normalizeReference(errorReference, fallback.statusCode());
        Locale requested = normalizeLocale(locale);
        Instant now = clock.instant();

        CpfResponseCodeRecord response = cache.response(reference);
        if (response == null || !response.activeAt(now)) {
            signals.catalogFallback(response == null ? "RESPONSE_NOT_FOUND" : "RESPONSE_INACTIVE", reference);
            return fallback(reference, fallback, requested, arguments);
        }

        CpfMessageRecord message = cache.message(response.messageCode(), requested);
        Locale resolvedLocale = requested;
        if (message == null || !message.activeAt(now)) {
            if (!requested.getLanguage().equals(DEFAULT_LOCALE.getLanguage())) {
                message = cache.message(response.messageCode(), DEFAULT_LOCALE);
                resolvedLocale = DEFAULT_LOCALE;
            }
        }
        if (message == null || !message.activeAt(now)) {
            signals.catalogFallback("MESSAGE_NOT_FOUND_OR_INACTIVE", reference);
            return fallback(reference, fallback, requested, arguments);
        }

        CmnMessageArgumentPolicy.Prepared prepared = argumentPolicy.prepare(
                arguments, message.parameterSchemaJson(), "Y".equalsIgnoreCase(message.escapeHtmlYn()),
                "Y".equalsIgnoreCase(message.maskArgumentsYn()));
        if (!prepared.valid()) {
            signals.catalogFallback(prepared.reason(), reference);
            return fallback(reference, fallback, requested, arguments);
        }

        CpfErrorDefinition definition;
        try {
            definition = effectiveDefinition(reference, response, fallback);
        } catch (RuntimeException invalidCatalog) {
            signals.catalogFallback("INVALID_RESPONSE_SEMANTICS", reference);
            return fallback(reference, fallback, requested, arguments);
        }

        String externalTemplate = blank(message.externalMessage()) ? fallback.defaultExternalMessage() : message.externalMessage();
        String internalTemplate = blank(message.internalMessage()) ? fallback.defaultInternalMessage() : message.internalMessage();
        String external = safeExternal(format(externalTemplate, prepared.arguments()));
        String internal = safeInternal(format(internalTemplate, prepared.arguments()));
        return new CpfResolvedError(response.responseCode(), response.messageCode(), definition,
                external, internal, resolvedLocale, true);
    }

    private CpfErrorDefinition effectiveDefinition(String reference, CpfResponseCodeRecord row, CpfErrorDefinition fallback) {
        CpfErrorCode reserved = reserved(reference);
        if (reserved != null) {
            // DB는 Reserved code의 다국어 message만 override할 수 있고 의미(category/retry/exposure)는 변경하지 못합니다.
            return reserved;
        }
        var category = CpfErrorDefinition.Category.valueOf(row.category().toUpperCase(Locale.ROOT));
        var retry = CpfErrorDefinition.RetryDisposition.valueOf(row.retryDisposition().toUpperCase(Locale.ROOT));
        var exposure = CpfErrorDefinition.Exposure.valueOf(row.exposure().toUpperCase(Locale.ROOT));
        return new CpfErrorDefinition.Dynamic(
                row.responseCode(), row.messageCode(), row.messageCode(), category, retry, exposure,
                fallback.defaultExternalMessage(), fallback.defaultInternalMessage());
    }

    private CpfErrorCode reserved(String reference) {
        for (CpfErrorCode code : CpfErrorCode.values()) {
            if (code.statusCode().equalsIgnoreCase(reference)) return code;
        }
        return null;
    }

    private CpfResolvedError fallback(String reference, CpfErrorDefinition fallback, Locale locale,
                                      Map<String, Object> arguments) {
        CmnMessageArgumentPolicy.Prepared prepared = argumentPolicy.prepare(arguments, null, true, true);
        Map<String,Object> safeArguments = prepared.valid() ? prepared.arguments() : Map.of();
        return new CpfResolvedError(fallback.statusCode(), fallback.messageCode(), fallback,
                safeExternal(format(fallback.defaultExternalMessage(), safeArguments)),
                safeInternal(format(fallback.defaultInternalMessage(), safeArguments)), locale, false);
    }

    private String format(String template, Map<String, Object> arguments) {
        if (template == null || template.isEmpty() || arguments.isEmpty()) return template;
        Matcher matcher = TOKEN.matcher(template);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            Object value = arguments.get(matcher.group(1));
            matcher.appendReplacement(out, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private String safeExternal(String value) {
        if (value == null || value.isBlank()) return "요청을 처리할 수 없습니다.";
        String v = value.replace('\r', ' ').replace('\n', ' ');
        return v.length() > 2000 ? v.substring(0, 2000) : v;
    }
    private String safeInternal(String value) {
        if (value == null || value.isBlank()) return "CPF catalog fallback";
        String v = value.replace('\r', ' ').replace('\n', ' ');
        return v.length() > 4000 ? v.substring(0, 4000) : v;
    }
    private String normalizeReference(String reference, String fallback) {
        String value = blank(reference) ? fallback : reference;
        return value.trim().toUpperCase(Locale.ROOT);
    }
    private Locale normalizeLocale(Locale locale) {
        if (locale == null || blank(locale.getLanguage())) return DEFAULT_LOCALE;
        return Locale.forLanguageTag(locale.toLanguageTag());
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
