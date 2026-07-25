package com.cpf.core.api.fixedlength;

import java.util.Objects;

/**
 * 등록된 Layout Metadata가 명시된 경우에만 고정길이 로그를 분해합니다.
 * 임의 길이 추론을 금지하고 parser가 산출한 masked view만 운영 화면에 반환합니다.
 */
public final class CpfFixedLengthLogDecoder {
    private final CpfFixedLengthParser parser;
    private final CpfFixedLengthLayoutRegistry registry;

    public CpfFixedLengthLogDecoder(CpfFixedLengthParser parser, CpfFixedLengthLayoutRegistry registry) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public CpfFixedLengthLogView decode(String message, String layoutId, String version) {
        CpfFixedLengthLayout layout = registry.require(layoutId, version);
        CpfFixedLengthParseResult result = parser.parse(message, layout);
        if (!result.valid()) {
            throw new CpfFixedLengthException("로그 전문 해석에 실패했습니다.", result.errors());
        }
        return new CpfFixedLengthLogView(layoutId, version, result.byteLength(), result.maskedFields(), result.maskedGroups());
    }
}
