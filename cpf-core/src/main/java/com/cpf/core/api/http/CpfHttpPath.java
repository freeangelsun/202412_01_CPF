package com.cpf.core.api.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Pure-Java relative path/query builder for provider-neutral HTTP consumers. */
public final class CpfHttpPath {
    private final String path;
    private final List<String> query = new ArrayList<>();

    private CpfHttpPath(String path) {
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("relative HTTP path must start with '/'");
        }
        this.path = path;
    }

    public static CpfHttpPath of(String path) { return new CpfHttpPath(path); }

    public CpfHttpPath queryParam(String name, Object value) {
        if (name == null || name.isBlank() || value == null) return this;
        query.add(encode(name) + "=" + encode(String.valueOf(value)));
        return this;
    }

    public String build() { return query.isEmpty() ? path : path + "?" + String.join("&", query); }

    public static String segment(Object value) {
        if (value == null) throw new IllegalArgumentException("path segment is required");
        return encode(String.valueOf(value));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
