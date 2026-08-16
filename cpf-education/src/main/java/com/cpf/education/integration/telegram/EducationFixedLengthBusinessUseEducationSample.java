package com.cpf.education.integration.telegram;
import com.cpf.integration.fixedlength.api.CpfFixedLengthFieldSpec;
import com.cpf.integration.fixedlength.api.CpfFixedLengthLayout;
import com.cpf.integration.fixedlength.api.CpfFixedLengthParser;
import com.cpf.integration.fixedlength.api.CpfFixedLengthTransforms;
import com.cpf.integration.fixedlength.api.CpfFixedLengthWriter;

import java.util.List;
import java.util.Map;

/**
 * EDU 업무가 CPF 공개 고정길이 Layout과 자료구조 변환 API를 함께 사용하는 예제입니다.
 * 업무 코드는 byte offset 계산/Map 변환/JSON 변환을 직접 다시 구현하지 않습니다.
 */
public class EducationFixedLengthBusinessUseEducationSample {

    public CpfFixedLengthLayout layout() {
        return CpfFixedLengthLayout.utf8(12, List.of(
                CpfFixedLengthFieldSpec.of("bankCode", 1, 3),
                CpfFixedLengthFieldSpec.of("userNo", 4, 9)
        ));
    }

    /** parseToMap 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String,Object> parseToMap(CpfFixedLengthParser parser, String message) {
        return CpfFixedLengthTransforms.toMap(parser, message, layout());
    }

    public String parseToJson(CpfFixedLengthParser parser, String message) {
        return CpfFixedLengthTransforms.toJson(parser, message, layout());
    }

    /** writeFromMap 작업을 CPF 표준 계약에 따라 수행한다. */
    public String writeFromMap(CpfFixedLengthWriter writer, Map<String,?> values) {
        return CpfFixedLengthTransforms.fromMap(writer, values, layout());
    }

    public String writeFromJson(CpfFixedLengthWriter writer, String json) {
        return CpfFixedLengthTransforms.fromJson(writer, json, layout());
    }
}
