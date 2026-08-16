package com.cpf.education.data.query.controller;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionContext;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.education.data.query.dto.EducationQueryEducationItem;
import com.cpf.education.data.query.dto.EducationQueryKeysetResponse;
import com.cpf.education.data.query.dto.EducationQueryPageResponse;
import com.cpf.education.data.query.application.EducationQueryEducationService;
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
@RequestMapping({"/api/education/query", "/education/edu/query"})
@Tag(name = "EDU Education 09. 조회 표준", description = "단건, 목록, offset 페이징, keyset 페이징, 검색, 정렬, 표준 헤더 전파 샘플")
public class EducationQueryEducationController extends com.cpf.education.base.EducationBaseController {
    private final EducationQueryEducationService queryEducationService;

    public EducationQueryEducationController(EducationQueryEducationService queryEducationService) {
        this.queryEducationService = queryEducationService;
    }

    /**
     * 단건 조회 샘플입니다.
     */
    @GetMapping("/items/{itemId}")
    @CpfOnlineTransaction(id = "OEDUQR0001", name = "EDU조회EDU단건조회", ownerDomain="EDU")
    @Operation(operationId = "refQueryEducationGetItem", summary = "조회 EDU 단건 조회", description = "PathVariable, readOnly 트랜잭션, NotFound 예외 처리 기준을 확인합니다.")
    public ResponseEntity<EducationQueryEducationItem> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(queryEducationService.getItem(itemId));
    }

    /**
     * 목록 조회 샘플입니다.
     */
    @GetMapping("/items")
    @CpfOnlineTransaction(id = "OEDUQR0002", name = "EDU조회EDU목록조회", ownerDomain="EDU")
    @Operation(operationId = "refQueryEducationFindItems", summary = "조회 EDU 목록 조회", description = "검색 조건 정규화, 정렬 whitelist, limit 제한 기준을 확인합니다.")
    public ResponseEntity<List<EducationQueryEducationItem>> findItems(
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
    @CpfOnlineTransaction(id = "OEDUQR0003", name = "EDU조회EDU오프셋페이징", ownerDomain="EDU")
    @Operation(operationId = "refQueryEducationFindOffsetPage", summary = "조회 EDU offset 페이징", description = "page, size, total, hasNext 응답 포맷을 확인합니다.")
    public ResponseEntity<EducationQueryPageResponse<EducationQueryEducationItem>> findOffsetPage(
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
    @CpfOnlineTransaction(id = "OEDUQR0004", name = "EDU조회EDU키셋페이징", ownerDomain="EDU")
    @Operation(operationId = "refQueryEducationFindKeysetPage", summary = "조회 EDU keyset 페이징", description = "cursorId, nextCursorId, hasNext 응답 포맷을 확인합니다.")
    public ResponseEntity<EducationQueryKeysetResponse<EducationQueryEducationItem>> findKeysetPage(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(queryEducationService.findKeysetPage(cursorId, size));
    }

    /**
     * 표준 헤더 컨텍스트와 하위 호출 전파 헤더를 확인하는 샘플입니다.
     */
    @GetMapping("/headers")
    @CpfOnlineTransaction(id = "OEDUQR0005", name = "EDU조회EDU헤더컨텍스트", ownerDomain="EDU")
    @Operation(operationId = "refQueryEducationCurrentHeaders", summary = "조회 EDU 헤더 컨텍스트", description = "TransactionContext 조회 API와 하위 호출 전파 헤더를 확인합니다.")
    public ResponseEntity<Map<String, Object>> currentHeaders() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionId", CpfContexts.currentTransactionId());
        response.put("traceId", CpfTransactionContext.currentTraceId());
        response.put("memberNo", CpfTransactionContext.memberNo());
        response.put("customerNo", CpfTransactionContext.customerNo());
        response.put("channelCode", CpfTransactionContext.channelCode());
        response.put("outboundHeaders", CpfTransactionContext.outboundHeaders());
        return ResponseEntity.ok(response);
    }
}
