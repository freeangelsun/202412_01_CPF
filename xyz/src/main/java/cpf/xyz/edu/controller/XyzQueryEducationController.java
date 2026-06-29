package cpf.xyz.edu.controller;

import cpf.pfw.common.header.CpfHeaderPropagator;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
import cpf.xyz.edu.dto.XyzQueryEducationItem;
import cpf.xyz.edu.dto.XyzQueryKeysetResponse;
import cpf.xyz.edu.dto.XyzQueryPageResponse;
import cpf.xyz.edu.service.XyzQueryEducationService;
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
 * <p>각 메서드는 실무에서 자주 필요한 조회 패턴을 하나씩 분리합니다. 운영 코드에서는 주석을 이렇게 길게 쓰지 않지만,
 * EDU 샘플은 개발자가 따라 하며 배울 수 있도록 의도적으로 상세한 한글 주석을 둡니다.</p>
 */
@RestController
@RequestMapping("/xyz/edu/query")
@Tag(name = "XYZ-EDU 09. 조회 표준", description = "단건, 목록, offset 페이징, keyset 페이징, 검색, 정렬, 표준 헤더 전파 샘플")
public class XyzQueryEducationController {
    private final XyzQueryEducationService queryEducationService;

    public XyzQueryEducationController(XyzQueryEducationService queryEducationService) {
        this.queryEducationService = queryEducationService;
    }

    /**
     * 단건 조회 샘플입니다.
     *
     * <p>PathVariable로 받은 식별자를 Service로 넘기고, Service는 미존재 예외를 표준 예외로 변환합니다.</p>
     */
    @GetMapping("/items/{itemId}")
    @CpfTransaction(id = "XYZ01QRY0001", name = "XYZ조회EDU단건조회")
    @Operation(summary = "조회 EDU 단건 조회", description = "PathVariable, readOnly 트랜잭션, NotFound 예외 처리 기준을 확인합니다.")
    public ResponseEntity<XyzQueryEducationItem> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(queryEducationService.getItem(itemId));
    }

    /**
     * 목록 조회 샘플입니다.
     *
     * <p>검색어, 상태, 정렬, limit을 받되 정렬 값은 Service에서 whitelist로 제한합니다.</p>
     */
    @GetMapping("/items")
    @CpfTransaction(id = "XYZ01QRY0002", name = "XYZ조회EDU목록조회")
    @Operation(summary = "조회 EDU 목록 조회", description = "검색 조건 정규화, 정렬 whitelist, limit 제한 기준을 확인합니다.")
    public ResponseEntity<List<XyzQueryEducationItem>> findItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String statusCode,
            @RequestParam(defaultValue = "idAsc") String sort,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(queryEducationService.findItems(keyword, statusCode, sort, limit));
    }

    /**
     * offset 페이징 샘플입니다.
     *
     * <p>관리자성 조회나 작은 목록에서는 offset 페이징을 사용하고, 대용량 실시간 목록은 keyset 페이징을 우선 검토합니다.</p>
     */
    @GetMapping("/items/page")
    @CpfTransaction(id = "XYZ01QRY0003", name = "XYZ조회EDU오프셋페이징")
    @Operation(summary = "조회 EDU offset 페이징", description = "page, size, total, hasNext 응답 포맷을 확인합니다.")
    public ResponseEntity<XyzQueryPageResponse<XyzQueryEducationItem>> findOffsetPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String statusCode,
            @RequestParam(defaultValue = "idAsc") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(queryEducationService.findOffsetPage(keyword, statusCode, sort, page, size));
    }

    /**
     * keyset 페이징 샘플입니다.
     *
     * <p>마지막으로 본 <code>itemId</code>를 cursor로 넘겨 다음 묶음을 조회합니다.</p>
     */
    @GetMapping("/items/keyset")
    @CpfTransaction(id = "XYZ01QRY0004", name = "XYZ조회EDU키셋페이징")
    @Operation(summary = "조회 EDU keyset 페이징", description = "cursorId, nextCursorId, hasNext 응답 포맷을 확인합니다.")
    public ResponseEntity<XyzQueryKeysetResponse<XyzQueryEducationItem>> findKeysetPage(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(queryEducationService.findKeysetPage(cursorId, size));
    }

    /**
     * 표준 헤더 컨텍스트와 하위 호출 전파 헤더를 확인하는 샘플입니다.
     */
    @GetMapping("/headers")
    @CpfTransaction(id = "XYZ01QRY0005", name = "XYZ조회EDU헤더컨텍스트")
    @Operation(summary = "조회 EDU 헤더 컨텍스트", description = "TransactionContext 조회 API와 하위 호출 전파 헤더를 확인합니다.")
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
