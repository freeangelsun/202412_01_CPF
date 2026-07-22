package com.cpf.admin.opr.controller;

import com.cpf.core.common.exception.CpfNotFoundException;
import com.cpf.core.common.execution.CpfExecutionCatalogPort;
import com.cpf.core.common.execution.CpfExecutionDefinition;
import com.cpf.core.common.execution.CpfExecutionType;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** ADM에서 온라인·배치 표준 실행 카탈로그를 통합 조회합니다. */
@RestController
@RequestMapping("/adm/api/standard-executions")
@Tag(name = "ADM-OPR Standard Execution", description = "CPF 온라인·배치 표준 실행 ID 카탈로그")
public class AdmStandardExecutionController extends com.cpf.admin.common.base.AdmBaseController {
    private final CpfExecutionCatalogPort catalogPort;

    public AdmStandardExecutionController(CpfExecutionCatalogPort catalogPort) {
        this.catalogPort = catalogPort;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADMEX0001", name = "ADMStandardExecutionList")
    @Operation(operationId = "admStandardExecutionFindAll", summary = "표준 실행 목록 조회",
            description = "기동 시 source annotation에서 발견한 온라인·배치 표준 실행 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String ownerDomain,
            @RequestParam(required = false) String keyword) {
        List<CpfExecutionDefinition> items = catalogPort.findAll().stream()
                .filter(item -> matchesType(item, type))
                .filter(item -> matches(item.ownerDomain(), ownerDomain))
                .filter(item -> matchesKeyword(item, keyword))
                .toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", items.size());
        response.put("items", items);
        response.put("summary", summary(items));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{standardExecutionId}")
    @CpfOnlineTransaction(id = "OADMEX0002", name = "ADMStandardExecutionDetail")
    @Operation(operationId = "admStandardExecutionFindOne", summary = "표준 실행 상세 조회",
            description = "표준 실행 ID에 연결된 source, endpoint, OpenAPI operation 정보를 조회합니다.")
    public ResponseEntity<CpfExecutionDefinition> findOne(@PathVariable String standardExecutionId) {
        return ResponseEntity.ok(catalogPort.findById(standardExecutionId)
                .orElseThrow(() -> new CpfNotFoundException(
                        "표준 실행 정보를 찾을 수 없습니다. standardExecutionId=" + standardExecutionId)));
    }

    private boolean matchesType(CpfExecutionDefinition item, String type) {
        if (type == null || type.isBlank()) {
            return true;
        }
        try {
            return item.executionType() == CpfExecutionType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean matches(String actual, String expected) {
        return expected == null || expected.isBlank() || expected.equalsIgnoreCase(actual);
    }

    private boolean matchesKeyword(CpfExecutionDefinition item, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return List.of(item.standardExecutionId(), item.executionName(), item.sourceClass(), item.sourceMethod(), item.endpoint())
                .stream()
                .filter(value -> value != null)
                .anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(normalized));
    }

    private Map<String, Long> summary(List<CpfExecutionDefinition> items) {
        return Map.of(
                "online", items.stream().filter(item -> item.executionType() == CpfExecutionType.ONLINE).count(),
                "batch", items.stream().filter(item -> item.executionType() == CpfExecutionType.BATCH).count());
    }
}
