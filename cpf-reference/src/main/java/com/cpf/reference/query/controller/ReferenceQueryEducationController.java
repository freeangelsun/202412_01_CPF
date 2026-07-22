package com.cpf.reference.query.controller;

import com.cpf.core.common.header.CpfHeaderPropagator;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.core.common.logging.TransactionContext;
import com.cpf.reference.query.dto.ReferenceQueryEducationItem;
import com.cpf.reference.query.dto.ReferenceQueryKeysetResponse;
import com.cpf.reference.query.dto.ReferenceQueryPageResponse;
import com.cpf.reference.query.application.ReferenceQueryEducationService;
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
import java.util.Map;

/**
 * 조회 API 개발 표준을 학습하기 위한 EDU 컨트롤러입니다.
 *
 * <p>단건, 목록, offset 페이징, keyset 페이징, 안전한 정렬, 표준 헤더 컨텍스트 조회를 한 곳에서 확인합니다.
 * 실제 업무에서는 같은 구조로 Controller, Service, Repository, Mapper, SQL, 테스트를 함께 작성합니다.</p>
 */
@RestController
@RequestMapping({"/api/reference/query", "/reference/edu/query"})
@Tag(name = "REF Reference 09. 조회 표준", description = "단건, 목록, offset 페이징, keyset 페이징, 검색, 정렬, 표준 헤더 전파 샘플")
public class ReferenceQueryEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final ReferenceQueryEducationService queryEducationService;

    public ReferenceQueryEducationController(ReferenceQueryEducationService queryEducationService) {
        this.queryEducationService = queryEducationService;
    }

    /**
     * 단건 조회 샘플입니다.
     */
    @GetMapping("/items/{itemId}")
    @CpfOnlineTransaction(id = "OREFQR0001", name = "REF조회EDU단건조회")
    @Operation(operationId = "refQueryEducationGetItem", summary = "조회 EDU 단건 조회", description = "PathVariable, readOnly 트랜잭션, NotFound 예외 처리 기준을 확인합니다.")
    public ResponseEntity<ReferenceQueryEducationItem> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(queryEducationService.getItem(itemId));
    }

    /**
     * 목록 조회 샘플입니다.
     */
    @GetMapping("/items")
    @CpfOnlineTransaction(id = "OREFQR0002", name = "REF조회EDU목록조회")
    @Operation(operationId = "refQueryEducationFindItems", summary = "조회 EDU 목록 조회", description = "검색 조건 정규화, 정렬 whitelist, limit 제한 기준을 확인합니다.")
    public ResponseEntity<List<ReferenceQueryEducationItem>> findItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String statusCode,
            @RequestParam(defaultValue = "idAsc") String sort,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(queryEducationService.findItems(keyword, statusCode, sort, limit));
    }

    /**
     * offset 페이징 샘플입니다.
     */
    @GetMapping("/items/page")
    @CpfOnlineTransaction(id = "OREFQR0003", name = "REF조회EDU오프셋페이징")
    @Operation(operationId = "refQueryEducationFindOffsetPage", summary = "조회 EDU offset 페이징", description = "page, size, total, hasNext 응답 포맷을 확인합니다.")
    public ResponseEntity<ReferenceQueryPageResponse<ReferenceQueryEducationItem>> findOffsetPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String statusCode,
            @RequestParam(defaultValue = "idAsc") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(queryEducationService.findOffsetPage(keyword, statusCode, sort, page, size));
    }

    /**
     * keyset 페이징 샘플입니다.
     */
    @GetMapping("/items/keyset")
    @CpfOnlineTransaction(id = "OREFQR0004", name = "REF조회EDU키셋페이징")
    @Operation(operationId = "refQueryEducationFindKeysetPage", summary = "조회 EDU keyset 페이징", description = "cursorId, nextCursorId, hasNext 응답 포맷을 확인합니다.")
    public ResponseEntity<ReferenceQueryKeysetResponse<ReferenceQueryEducationItem>> findKeysetPage(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(queryEducationService.findKeysetPage(cursorId, size));
    }

    /**
     * 표준 헤더 컨텍스트와 하위 호출 전파 헤더를 확인하는 샘플입니다.
     */
    @GetMapping("/headers")
    @CpfOnlineTransaction(id = "OREFQR0005", name = "REF조회EDU헤더컨텍스트")
    @Operation(operationId = "refQueryEducationCurrentHeaders", summary = "조회 EDU 헤더 컨텍스트", description = "TransactionContext 조회 API와 하위 호출 전파 헤더를 확인합니다.")
    public ResponseEntity<Map<String, Object>> currentHeaders() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionId", TransactionContext.currentTransactionId());
        response.put("traceId", TransactionContext.currentTraceId());
        response.put("memberNo", TransactionContext.memberNo());
        response.put("customerNo", TransactionContext.customerNo());
        response.put("channelCode", TransactionContext.channelCode());
        response.put("outboundHeaders", CpfHeaderPropagator.outboundHeaders());
        return ResponseEntity.ok(response);
    }
}
