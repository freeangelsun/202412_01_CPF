package com.cpf.integration.fixedlength.api;

import java.util.Map;
import java.util.Objects;

/**
 * 업무 Domain이 Layout ID/version만으로 고정길이 전문을 읽고 쓰는 Golden Path입니다.
 * Parser/Writer/Registry를 각각 찾아 조립하지 않아도 되며 로그 조회는 항상 마스킹 View를 사용합니다.
 */
public final class CpfFixedLengthOperations {
    private final CpfFixedLengthParser parser;
    private final CpfFixedLengthWriter writer;
    private final CpfFixedLengthLayoutRegistry layouts;
    private final CpfFixedLengthLogDecoder logDecoder;

    /** CpfFixedLengthOperations는 CPF 공개 계약의 입력·결과 의미를 보존해 호출자가 동일한 방식으로 사용할 수 있게 합니다. */
    public CpfFixedLengthOperations(
            CpfFixedLengthParser parser,
            CpfFixedLengthWriter writer,
            CpfFixedLengthLayoutRegistry layouts) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.writer = Objects.requireNonNull(writer, "writer");
        this.layouts = Objects.requireNonNull(layouts, "layouts");
        this.logDecoder = new CpfFixedLengthLogDecoder(parser, layouts);
    }

    /** parse는 고정길이 전문을 byte-length·layout 계약에 맞춰 검증하고 변환합니다. */
    public CpfFixedLengthParseResult parse(String message, String layoutId, String version) {
        return parser.parse(message, layouts.require(layoutId, version));
    }

    /** parse는 고정길이 전문을 byte-length·layout 계약에 맞춰 검증하고 변환합니다. */
    public CpfFixedLengthParseResult parse(byte[] message, String layoutId, String version) {
        return parser.parse(message, layouts.require(layoutId, version));
    }

    /** write는 고정길이 전문을 byte-length·layout 계약에 맞춰 검증하고 변환합니다. */
    public CpfFixedLengthWriteResult write(Map<String, ?> values, String layoutId, String version) {
        return writer.write(values, layouts.require(layoutId, version));
    }

    /** ADM/로그 화면에는 원문 대신 parser가 생성한 마스킹 완료 View만 전달합니다. */
    public CpfFixedLengthLogView logView(String message, String layoutId, String version) {
        return logDecoder.decode(message, layoutId, version);
    }
}
