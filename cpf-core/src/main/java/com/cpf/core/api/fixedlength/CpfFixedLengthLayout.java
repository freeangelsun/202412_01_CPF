package com.cpf.core.api.fixedlength;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 버전과 charset을 포함하는 CPF 고정길이 전문 layout입니다.
 */
public record CpfFixedLengthLayout(
        String layoutId,
        String version,
        Charset charset,
        int totalLength,
        List<CpfFixedLengthFieldSpec> fields,
        List<CpfFixedLengthGroupSpec> groups) {

    public CpfFixedLengthLayout(
            Charset charset,
            int totalLength,
            List<CpfFixedLengthFieldSpec> fields) {
        this("anonymous", "1", charset, totalLength, fields, List.of());
    }

    public CpfFixedLengthLayout(
            Charset charset,
            int totalLength,
            List<CpfFixedLengthFieldSpec> fields,
            List<CpfFixedLengthGroupSpec> groups) {
        this("anonymous", "1", charset, totalLength, fields, groups);
    }

    public CpfFixedLengthLayout {
        layoutId = normalizeIdentity(layoutId, "anonymous");
        version = normalizeIdentity(version, "1");
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        if (totalLength < 1) {
            throw new IllegalArgumentException("고정길이 전문 전체 byte 길이는 1 이상이어야 합니다.");
        }
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("고정길이 전문 필드 정의가 필요합니다.");
        }
        fields = fields.stream()
                .sorted(Comparator.comparingInt(CpfFixedLengthFieldSpec::start))
                .toList();
        groups = groups == null ? List.of() : List.copyOf(groups);
        validate(totalLength, fields, groups);
    }

    public static CpfFixedLengthLayout utf8(
            int totalLength,
            List<CpfFixedLengthFieldSpec> fields) {
        return new CpfFixedLengthLayout(StandardCharsets.UTF_8, totalLength, fields);
    }

    public static CpfFixedLengthLayout utf8(
            int totalLength,
            List<CpfFixedLengthFieldSpec> fields,
            List<CpfFixedLengthGroupSpec> groups) {
        return new CpfFixedLengthLayout(StandardCharsets.UTF_8, totalLength, fields, groups);
    }

    public static CpfFixedLengthLayout eucKr(
            int totalLength,
            List<CpfFixedLengthFieldSpec> fields) {
        return new CpfFixedLengthLayout(Charset.forName("EUC-KR"), totalLength, fields);
    }

    public static CpfFixedLengthLayout eucKr(
            int totalLength,
            List<CpfFixedLengthFieldSpec> fields,
            List<CpfFixedLengthGroupSpec> groups) {
        return new CpfFixedLengthLayout(Charset.forName("EUC-KR"), totalLength, fields, groups);
    }

    private static void validate(
            int totalLength,
            List<CpfFixedLengthFieldSpec> fields,
            List<CpfFixedLengthGroupSpec> groups) {
        int previousEnd = 0;
        Set<String> fieldNames = new HashSet<>();
        for (CpfFixedLengthFieldSpec field : fields) {
            if (!fieldNames.add(field.name())) {
                throw new IllegalArgumentException("고정길이 필드명이 중복되었습니다. field=" + field.name());
            }
            if (field.zeroBasedStart() < previousEnd) {
                throw new IllegalArgumentException("고정길이 필드가 겹칩니다. field=" + field.name());
            }
            if (field.zeroBasedEndExclusive() > totalLength) {
                throw new IllegalArgumentException("고정길이 필드가 전체 길이를 초과합니다. field=" + field.name());
            }
            previousEnd = field.zeroBasedEndExclusive();
        }

        int fallbackStart = previousEnd;
        int previousGroupEnd = previousEnd;
        Set<String> groupNames = new HashSet<>();
        for (CpfFixedLengthGroupSpec group : groups) {
            if (!groupNames.add(group.name())) {
                throw new IllegalArgumentException("고정길이 반복부 이름이 중복되었습니다. group=" + group.name());
            }
            if (!fieldNames.contains(group.countFieldName())) {
                throw new IllegalArgumentException(
                        "고정길이 반복부 count 필드가 layout에 없습니다. group=" + group.name()
                                + ", field=" + group.countFieldName());
            }
            int groupStart = group.zeroBasedStart(fallbackStart);
            int groupEnd = groupStart + (group.itemLength() * group.maxCount());
            if (groupStart < previousEnd || groupStart < previousGroupEnd) {
                throw new IllegalArgumentException("고정길이 반복부가 앞 구간과 겹칩니다. group=" + group.name());
            }
            if (groupEnd > totalLength) {
                throw new IllegalArgumentException("고정길이 반복부가 전체 길이를 초과합니다. group=" + group.name());
            }
            fallbackStart = groupEnd;
            previousGroupEnd = groupEnd;
        }
    }

    private static String normalizeIdentity(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
