package com.cpf.education.common.utility.controller;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.foundation.util.CpfJson;
import com.cpf.foundation.util.CpfMaps;
import com.cpf.foundation.util.CpfValues;
import com.cpf.education.base.EducationBaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Map/List/JSON/자료형 변환 Public API를 실제 업무 Controller 형태로 보여주는 EDU입니다.
 * 임의 ObjectMapper/형변환 helper를 업무 도메인마다 다시 만들지 않는 것이 목적입니다.
 */
@RestController
@RequestMapping({
        "/api/education/data-transform",
        "/education/edu/data-transform",
        "/ref/api/edu/data-transform"
})
@Tag(name="EDU Education 15. Data Transform", description="CPF 자료구조/JSON/형변환 표준 사용 예제")
public class EduDataTransformEduController extends EducationBaseController {

    @PostMapping("/json-map")
    @CpfOnlineTransaction(id="OEDUUT0003", name="EDUJsonMapTransform", ownerDomain="EDU")
    @Operation(operationId="refJsonMapTransform", summary="JSON ↔ Map/List 변환 예제")
    /** jsonMap 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> jsonMap(@RequestBody String json) {
        Map<String,Object> values = CpfJson.map(json);
        return ResponseEntity.ok(Map.of(
                "map", values,
                "json", CpfJson.write(values),
                "keys", List.copyOf(values.keySet())));
    }

    @PostMapping("/typed-values")
    @CpfOnlineTransaction(id="OEDUUT0004", name="EDUTypedValueTransform", ownerDomain="EDU")
    @Operation(operationId="refTypedValueTransform", summary="Map 값의 안전한 자료형 변환 예제")
    /** typedValues 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> typedValues(@RequestBody Map<String,Object> body) {
        Integer count = CpfValues.integer(body.get("count"));
        BigDecimal amount = CpfValues.decimal(body.get("amount"));
        Boolean enabled = CpfValues.bool(body.get("enabled"));
        Map<String,Object> result = new java.util.LinkedHashMap<>();
        result.put("count", count);
        result.put("amount", amount);
        result.put("enabled", enabled);
        return ResponseEntity.ok(CpfMaps.mutableCopy(result));
    }
}
