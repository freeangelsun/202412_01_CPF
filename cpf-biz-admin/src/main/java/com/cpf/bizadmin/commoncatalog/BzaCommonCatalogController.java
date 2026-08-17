package com.cpf.bizadmin.commoncatalog;


import org.springframework.web.bind.annotation.RestController;
import com.cpf.common.message.api.CpfCommonCatalogManagementService;
import com.cpf.common.message.dto.CommonMessageRequest;
import com.cpf.common.message.dto.CommonResponseCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Error/Response Code/Message Common Product Service의 BZA Consumer입니다.
 * BZA는 CMN owner table SQL을 직접 실행하지 않고 Common Management API만 호출합니다.
 */
@RestController
@RequestMapping("/api/bza/common-catalog")
@Tag(name = "BZA-Common-Catalog", description = "응답코드·다국어 메시지 관리 API")
public final class BzaCommonCatalogController extends com.cpf.bizadmin.common.base.BzaBaseController {
    private final CpfCommonCatalogManagementService common;
    public BzaCommonCatalogController(CpfCommonCatalogManagementService common) {
        this.common = common;
    }

    @GetMapping("/response-codes")    @Operation(operationId = "bzaCommonResponseCodeSearch", summary = "공통 응답코드 검색/Paging")
    /** responseCodes 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> responseCodes(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Boolean active,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(common.searchResponseCodes(keyword, active, page, size));
    }

    @GetMapping("/response-codes/{code}")    @Operation(operationId = "bzaCommonResponseCodeDetail", summary = "공통 응답코드 상세")
    /** responseCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> responseCode(@PathVariable String code) {
        return ResponseEntity.ok(common.getResponseCode(code));
    }

    @PostMapping("/response-codes")    @Operation(operationId = "bzaCommonResponseCodeCreate", summary = "공통 응답코드 등록")
    /** createResponseCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> createResponseCode(@Valid @RequestBody CommonResponseCodeRequest request,
                                                @RequestAttribute("bza.operatorId") String operator,
                                                @RequestHeader("X-CPF-Reason") String reason) {
        Object after = common.createResponseCode(request, requiredOperator(operator), requiredReason(reason));
        return ResponseEntity.ok(after);
    }

    @PutMapping("/response-codes/{code}")    @Operation(operationId = "bzaCommonResponseCodeUpdate", summary = "공통 응답코드 수정")
    /** updateResponseCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> updateResponseCode(@PathVariable String code,
                                                @RequestParam long expectedVersion,
                                                @Valid @RequestBody CommonResponseCodeRequest request,
                                                @RequestAttribute("bza.operatorId") String operator,
                                                @RequestHeader("X-CPF-Reason") String reason) {
        Object before = common.getResponseCode(code);
        Object after = common.updateResponseCode(code, expectedVersion, request, requiredOperator(operator), requiredReason(reason));
        return ResponseEntity.ok(after);
    }

    @DeleteMapping("/response-codes/{code}")    @Operation(operationId = "bzaCommonResponseCodeDisable", summary = "공통 응답코드 비활성화")
    /** disableResponseCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> disableResponseCode(@PathVariable String code,
                                                 @RequestParam long expectedVersion,
                                                 @RequestAttribute("bza.operatorId") String operator,
                                                 @RequestHeader("X-CPF-Reason") String reason) {
        Object before = common.getResponseCode(code);
        common.deleteResponseCode(code, expectedVersion, requiredOperator(operator), requiredReason(reason));
        Map<String, Object> after = Map.of("responseCode", code, "useYn", "N");
        return ResponseEntity.ok(after);
    }

    @GetMapping("/messages")    @Operation(operationId = "bzaCommonMessageSearch", summary = "공통 메시지 검색/Paging")
    /** messages 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> messages(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String locale,
                                      @RequestParam(required = false) Boolean active,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(common.searchMessages(keyword, locale, active, page, size));
    }

    @GetMapping("/messages/{id}")    @Operation(operationId = "bzaCommonMessageDetail", summary = "공통 메시지 상세")
    /** message 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> message(@PathVariable long id) {
        return ResponseEntity.ok(common.getMessage(id));
    }

    @PostMapping("/messages")    @Operation(operationId = "bzaCommonMessageCreate", summary = "공통 메시지 등록")
    /** createMessage 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> createMessage(@Valid @RequestBody CommonMessageRequest request,
                                           @RequestAttribute("bza.operatorId") String operator,
                                           @RequestHeader("X-CPF-Reason") String reason) {
        Object after = common.createMessage(request, requiredOperator(operator), requiredReason(reason));
        return ResponseEntity.ok(after);
    }

    @PutMapping("/messages/{id}")    @Operation(operationId = "bzaCommonMessageUpdate", summary = "공통 메시지 수정")
    /** updateMessage 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> updateMessage(@PathVariable long id,
                                           @RequestParam long expectedVersion,
                                           @Valid @RequestBody CommonMessageRequest request,
                                           @RequestAttribute("bza.operatorId") String operator,
                                           @RequestHeader("X-CPF-Reason") String reason) {
        Object before = common.getMessage(id);
        Object after = common.updateMessage(id, expectedVersion, request, requiredOperator(operator), requiredReason(reason));
        return ResponseEntity.ok(after);
    }

    @DeleteMapping("/messages/{id}")    @Operation(operationId = "bzaCommonMessageDisable", summary = "공통 메시지 비활성화")
    /** disableMessage 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> disableMessage(@PathVariable long id,
                                            @RequestParam long expectedVersion,
                                            @RequestAttribute("bza.operatorId") String operator,
                                            @RequestHeader("X-CPF-Reason") String reason) {
        Object before = common.getMessage(id);
        common.deleteMessage(id, expectedVersion, requiredOperator(operator), requiredReason(reason));
        Map<String, Object> after = Map.of("messageId", id, "useYn", "N");
        return ResponseEntity.ok(after);
    }

    @PostMapping("/cache/refresh")    @Operation(operationId = "bzaCommonCatalogRefresh", summary = "공통 Catalog cache refresh")
    /** refresh 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> refresh(@RequestAttribute("bza.operatorId") String operator,
                                     @RequestHeader("X-CPF-Reason") String reason) {
        common.refreshCaches(requiredOperator(operator), requiredReason(reason));
        Map<String, Object> after = Map.of("refreshed", true);
        return ResponseEntity.ok(after);
    }
}
