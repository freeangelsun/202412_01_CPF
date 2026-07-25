package com.cpf.reference.telegram;

import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayout;
import com.cpf.core.api.fixedlength.CpfFixedLengthParser;
import com.cpf.core.api.fixedlength.CpfFixedLengthTransforms;
import com.cpf.core.api.fixedlength.CpfFixedLengthWriter;

import java.util.List;
import java.util.Map;

/**
 * REF 업무가 CPF Core 고정길이 Layout과 자료구조 변환 API를 함께 사용하는 EDU입니다.
 * 업무 코드는 byte offset 계산/Map 변환/JSON 변환을 직접 다시 구현하지 않습니다.
 */
public class ReferenceFixedLengthBusinessUseEducationSample {

    public CpfFixedLengthLayout layout() {
        return CpfFixedLengthLayout.utf8(12, List.of(
                CpfFixedLengthFieldSpec.of("bankCode", 1, 3),
                CpfFixedLengthFieldSpec.of("userNo", 4, 9)
        ));
    }

    public Map<String,Object> parseToMap(CpfFixedLengthParser parser, String message) {
        return CpfFixedLengthTransforms.toMap(parser, message, layout());
    }

    public String parseToJson(CpfFixedLengthParser parser, String message) {
        return CpfFixedLengthTransforms.toJson(parser, message, layout());
    }

    public String writeFromMap(CpfFixedLengthWriter writer, Map<String,?> values) {
        return CpfFixedLengthTransforms.fromMap(writer, values, layout());
    }

    public String writeFromJson(CpfFixedLengthWriter writer, String json) {
        return CpfFixedLengthTransforms.fromJson(writer, json, layout());
    }
}
