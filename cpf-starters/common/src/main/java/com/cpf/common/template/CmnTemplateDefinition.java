package com.cpf.common.template;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** Immutable, versioned template contract shared by notification and document consumers. */
public record CmnTemplateDefinition(
        String templateCode,
        long version,
        String channel,
        String body,
        Set<String> allowedVariables,
        boolean active) {
    private static final Pattern CODE = Pattern.compile("[A-Za-z][A-Za-z0-9_.-]{0,99}");
    private static final Pattern CHANNEL = Pattern.compile("[A-Za-z][A-Za-z0-9_.-]{0,29}");
    private static final Pattern VARIABLE = Pattern.compile("[A-Za-z][A-Za-z0-9_.-]{0,99}");

    public CmnTemplateDefinition {
        templateCode = requirePattern(templateCode, CODE, "templateCode");
        if (version <= 0) {
            throw new IllegalArgumentException("version must be greater than zero");
        }
        channel = requirePattern(channel, CHANNEL, "channel");
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body must not be blank");
        }
        Set<String> validated = new LinkedHashSet<>();
        if (allowedVariables != null) {
            for (String variable : allowedVariables) {
                validated.add(requirePattern(variable, VARIABLE, "allowedVariable"));
            }
        }
        allowedVariables = Collections.unmodifiableSet(new LinkedHashSet<>(validated));
    }

    private static String requirePattern(String value, Pattern pattern, String field) {
        if (value == null || !pattern.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException(field + " has invalid format");
        }
        return value.trim();
    }
}
