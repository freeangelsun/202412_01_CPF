package com.cpf.reference.cmn.controller;

import com.cpf.common.sample.CmnSampleItem;
import com.cpf.common.sample.CmnSampleItemRequest;
import com.cpf.common.sample.CmnSampleItemService;
import com.cpf.common.sample.CmnSampleSlice;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DB-less가 기본인 cpf-common에서 선택형 cmnDB Sample 연결을 검증하는 교육 API입니다.
 *
 * <p>고객 업무 원장이 아니라 단일 {@code cmn_sample_item} 테이블만 사용해 연결,
 * CRUD, 검색, offset/slice/cursor, 중복, 낙관적 잠금과 rollback을 검증합니다.</p>
 */
@Validated
@RestController
@RequestMapping({"/api/reference/cmn-sample", "/reference/edu/cmn-sample"})
@Tag(name = "REF Reference 12. CMN Sample", description = "cmnDB 단일 선택형 Sample 테이블 검증")
public class ReferenceCmnSampleEducationController
        extends com.cpf.reference.common.base.ReferenceBaseController {

    private final CmnSampleItemService sampleItemService;

    public ReferenceCmnSampleEducationController(CmnSampleItemService sampleItemService) {
        this.sampleItemService = sampleItemService;
    }

    @GetMapping("/status")
    @CpfOnlineTransaction(id = "OREFAA0044", name = "REFCMNSample상태")
    @Operation(operationId = "refCmnSampleStatus", summary = "CMN Sample 연결 상태",
            description = "명시적 Sample datasource와 transaction 설정이 활성화되었는지 확인합니다.")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(statusResponse());
    }

    @GetMapping("/items/{sampleItemId}")
    @CpfOnlineTransaction(id = "OREFAA0045", name = "REFCMNSample상세")
    @Operation(operationId = "refCmnSampleFind", summary = "CMN Sample 상세 조회")
    public ResponseEntity<CmnSampleItem> find(@PathVariable @Min(1) long sampleItemId) {
        requireEnabled();
        return sampleItemService.find(sampleItemId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/items")
    @CpfOnlineTransaction(id = "OREFAA0046", name = "REFCMNSampleOffset조회")
    @Operation(operationId = "refCmnSampleOffsetPage", summary = "CMN Sample 검색·offset 조회")
    public ResponseEntity<List<CmnSampleItem>> offsetPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String statusCode,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        requireEnabled();
        return ResponseEntity.ok(sampleItemService.offsetPage(keyword, statusCode, offset, limit));
    }

    @GetMapping("/items/cursor")
    @CpfOnlineTransaction(id = "OREFAA0047", name = "REFCMNSampleCursor조회")
    @Operation(operationId = "refCmnSampleCursorPage", summary = "CMN Sample count 없는 cursor slice 조회")
    public ResponseEntity<CmnSampleSlice> cursorPage(
            @RequestParam(required = false) @Min(0) Long afterId,
            @RequestParam(required = false) String statusCode,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        requireEnabled();
        return ResponseEntity.ok(sampleItemService.cursorPage(afterId, statusCode, limit));
    }

    @PostMapping("/items")
    @CpfOnlineTransaction(id = "OREFAA0048", name = "REFCMNSample등록")
    @Operation(operationId = "refCmnSampleCreate", summary = "CMN Sample 등록·중복 검증")
    public ResponseEntity<CmnSampleItem> create(@Valid @RequestBody CmnSampleItemRequest request) {
        requireEnabled();
        return ResponseEntity.ok(sampleItemService.create(request));
    }

    @PutMapping("/items/{sampleItemId}")
    @CpfOnlineTransaction(id = "OREFAA0049", name = "REFCMNSample수정")
    @Operation(operationId = "refCmnSampleUpdate", summary = "CMN Sample 낙관적 잠금 수정")
    public ResponseEntity<CmnSampleItem> update(
            @PathVariable @Min(1) long sampleItemId,
            @RequestParam @Min(0) long expectedVersion,
            @Valid @RequestBody CmnSampleItemRequest request) {
        requireEnabled();
        return ResponseEntity.ok(sampleItemService.update(sampleItemId, expectedVersion, request));
    }

    @DeleteMapping("/items/{sampleItemId}")
    @CpfOnlineTransaction(id = "OREFAA0050", name = "REFCMNSample삭제")
    @Operation(operationId = "refCmnSampleDelete", summary = "CMN Sample 낙관적 잠금 논리 삭제")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable @Min(1) long sampleItemId,
            @RequestParam @Min(0) long expectedVersion,
            @RequestParam(defaultValue = "REF_EDU") String requestUser) {
        requireEnabled();
        sampleItemService.delete(sampleItemId, expectedVersion, requestUser);
        return ResponseEntity.ok(Map.of(
                "sampleItemId", sampleItemId,
                "logicalDelete", true));
    }

    @PostMapping("/transaction/rollback-verify")
    @CpfOnlineTransaction(id = "OREFAA0051", name = "REFCMNSampleRollback")
    @Operation(operationId = "refCmnSampleRollbackVerify", summary = "CMN Sample transaction rollback 검증",
            description = "등록을 수행한 뒤 rollbackOnly로 되돌리고 같은 key의 row 수가 유지되는지 확인합니다.")
    public ResponseEntity<Map<String, Object>> verifyRollback(
            @Valid @RequestBody CmnSampleItemRequest request) {
        requireEnabled();
        return ResponseEntity.ok(Map.of(
                "rolledBack", sampleItemService.verifyRollback(request),
                "sampleKey", request.sampleKey()));
    }

    private void requireEnabled() {
        if (!sampleItemService.isEnabled()) {
            throw new IllegalStateException(
                    "CMN Sample DB가 비활성화되어 있습니다. cpf.cmn.sample-db.enabled=true와 "
                            + "spring.datasource.cmn-sample.* 설정을 확인하세요.");
        }
    }

    private Map<String, Object> statusResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean enabled = sampleItemService.isEnabled();
        response.put("enabled", enabled);
        response.put("dbLessDefault", true);
        response.put("schema", "cmnDB");
        response.put("table", "cmn_sample_item");
        response.put("tableCount", 1);
        response.put("enableGuide",
                "cpf.cmn.sample-db.enabled=true와 spring.datasource.cmn-sample.* 설정을 사용합니다.");
        return response;
    }
}
