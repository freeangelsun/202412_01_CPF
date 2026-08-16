package com.cpf.web.openapi.webmvc.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Neutral document metadata exposed to customer contributors without leaking Swagger/OpenAPI OSS types. */
/** CpfOpenApiDocument 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class CpfOpenApiDocument {
    private String title;
    private String version;
    private String description;
    private final Map<String, Object> extensions = new LinkedHashMap<>();

    public CpfOpenApiDocument(String title, String version, String description) {
        this.title = required(title, "title");
        this.version = required(version, "version");
        this.description = Objects.requireNonNullElse(description, "").trim();
    }

    /** title 작업을 CPF 표준 계약에 따라 수행한다. */
    public String title() { return title; }
    public String version() { return version; }
    public String description() { return description; }
    public Map<String, Object> extensions() { return Map.copyOf(extensions); }

    public CpfOpenApiDocument title(String value) { this.title = required(value, "title"); return this; }
    public CpfOpenApiDocument version(String value) { this.version = required(value, "version"); return this; }
    /** description 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfOpenApiDocument description(String value) { this.description = Objects.requireNonNullElse(value, "").trim(); return this; }
    public CpfOpenApiDocument extension(String name, Object value) {
        String key = required(name, "extension name");
        if (!key.startsWith("x-")) throw new IllegalArgumentException("extension name must start with x-");
        extensions.put(key, value);
        return this;
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }
}
