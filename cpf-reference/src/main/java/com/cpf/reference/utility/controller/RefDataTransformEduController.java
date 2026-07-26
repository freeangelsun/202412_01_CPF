package com.cpf.reference.utility.controller;

import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.util.CpfJson;
import com.cpf.core.api.util.CpfMaps;
import com.cpf.core.api.util.CpfValues;
import com.cpf.reference.common.base.ReferenceBaseController;
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
        "/api/reference/data-transform",
        "/reference/edu/data-transform",
        "/ref/api/edu/data-transform"
})
@Tag(name="REF Reference 15. Data Transform", description="CPF 자료구조/JSON/형변환 표준 사용 예제")
public class RefDataTransformEduController extends ReferenceBaseController {

    @PostMapping("/json-map")
    @CpfOnlineTransaction(id="OREFUT0003", name="REFJsonMapTransform", ownerDomain="REF")
    @Operation(operationId="refJsonMapTransform", summary="JSON ↔ Map/List 변환 예제")
    public ResponseEntity<Map<String,Object>> jsonMap(@RequestBody String json) {
        Map<String,Object> values = CpfJson.map(json);
        return ResponseEntity.ok(Map.of(
                "map", values,
                "json", CpfJson.write(values),
                "keys", List.copyOf(values.keySet())));
    }

    @PostMapping("/typed-values")
    @CpfOnlineTransaction(id="OREFUT0004", name="REFTypedValueTransform", ownerDomain="REF")
    @Operation(operationId="refTypedValueTransform", summary="Map 값의 안전한 자료형 변환 예제")
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
