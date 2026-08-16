package com.cpf.integration.fixedlength.api;

import java.util.Objects;

/**
 * 등록된 Layout Metadata가 명시된 경우에만 고정길이 로그를 분해합니다.
 * 임의 길이 추론을 금지하고 parser가 산출한 masked view만 운영 화면에 반환합니다.
 */
public final class CpfFixedLengthLogDecoder {
    private final CpfFixedLengthParser parser;
    private final CpfFixedLengthLayoutRegistry registry;

    /**
     * 고정길이 parser와 명시적 Layout registry를 결합한 로그 decoder를 생성합니다.
     *
     * @param parser 등록된 Layout에 따라 전문을 해석하고 민감 필드를 마스킹하는 parser
     * @param registry layoutId와 version으로 명시적 Layout을 조회하는 registry
     * @throws NullPointerException parser 또는 registry가 {@code null}인 경우
     */
    public CpfFixedLengthLogDecoder(CpfFixedLengthParser parser, CpfFixedLengthLayoutRegistry registry) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * 지정한 Layout으로 전문을 해석하여 운영 조회용 마스킹 결과를 반환합니다.
     *
     * @param message 해석할 원문 전문
     * @param layoutId 사전에 등록된 Layout 식별자
     * @param version 사전에 등록된 Layout 버전
     * @return 원문 민감정보를 노출하지 않는 마스킹된 고정길이 로그 View
     * @throws CpfFixedLengthException Layout 기준으로 전문이 유효하지 않은 경우
     * @throws IllegalArgumentException 등록되지 않은 Layout 식별자 또는 버전인 경우
     */
    public CpfFixedLengthLogView decode(String message, String layoutId, String version) {
        CpfFixedLengthLayout layout = registry.require(layoutId, version);
        CpfFixedLengthParseResult result = parser.parse(message, layout);
        if (!result.valid()) {
            throw new CpfFixedLengthException("로그 전문 해석에 실패했습니다.", result.errors());
        }
        return new CpfFixedLengthLogView(layoutId, version, result.byteLength(), result.maskedFields(), result.maskedGroups());
    }
}
