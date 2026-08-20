package com.cpf.backoffice.online.catalog.controller;

import com.cpf.web.api.CpfRestController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;


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
 * Error/Response Code/Message Common Product Service의 MBW Consumer입니다.
 * MBW는 CMN owner table SQL을 직접 실행하지 않고 Common Management API만 호출합니다.
 */
@CpfRestController
@RequestMapping("/api/v1/backoffice/common-catalog")
@Tag(name = "MBW-Common-Catalog", description = "응답코드·다국어 메시지 관리 API")
public final class BackofficeCommonCatalogController extends com.cpf.backoffice.online.base.BackofficeBaseController {
    private final CpfCommonCatalogManagementService common;
    public BackofficeCommonCatalogController(CpfCommonCatalogManagementService common) {
        this.common = common;
    }

    @GetMapping("/response-codes")    @Operation(operationId = "MBW_COMMON_RESPONSE_CODE_SEARCH", summary = "공통 응답코드 검색/Paging")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_RESPONSE_CODE_SEARCH", name = "공통 응답코드 검색/Paging", description = "공통 응답코드 검색/Paging 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** responseCodes 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> responseCodes(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Boolean active,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(common.searchResponseCodes(keyword, active, page, size));
    }

    @GetMapping("/response-codes/{code}")    @Operation(operationId = "MBW_COMMON_RESPONSE_CODE_DETAIL", summary = "공통 응답코드 상세")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_RESPONSE_CODE_DETAIL", name = "공통 응답코드 상세", description = "공통 응답코드 상세 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** responseCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> responseCode(@PathVariable String code) {
        return ResponseEntity.ok(common.getResponseCode(code));
    }

    @PostMapping("/response-codes")    @Operation(operationId = "MBW_COMMON_RESPONSE_CODE_CREATE", summary = "공통 응답코드 등록")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_RESPONSE_CODE_CREATE", name = "공통 응답코드 등록", description = "공통 응답코드 등록 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** createResponseCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> createResponseCode(@Valid @RequestBody CommonResponseCodeRequest request,
                                                @RequestAttribute("backoffice.operatorId") String operator,
                                                @RequestHeader("X-CPF-Reason") String reason) {
        Object after = common.createResponseCode(request, requiredOperator(operator), requiredReason(reason));
        return ResponseEntity.ok(after);
    }

    @PutMapping("/response-codes/{code}")    @Operation(operationId = "MBW_COMMON_RESPONSE_CODE_UPDATE", summary = "공통 응답코드 수정")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_RESPONSE_CODE_UPDATE", name = "공통 응답코드 수정", description = "공통 응답코드 수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** updateResponseCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> updateResponseCode(@PathVariable String code,
                                                @RequestParam long expectedVersion,
                                                @Valid @RequestBody CommonResponseCodeRequest request,
                                                @RequestAttribute("backoffice.operatorId") String operator,
                                                @RequestHeader("X-CPF-Reason") String reason) {
        Object before = common.getResponseCode(code);
        Object after = common.updateResponseCode(code, expectedVersion, request, requiredOperator(operator), requiredReason(reason));
        return ResponseEntity.ok(after);
    }

    @DeleteMapping("/response-codes/{code}")    @Operation(operationId = "MBW_COMMON_RESPONSE_CODE_DISABLE", summary = "공통 응답코드 비활성화")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_RESPONSE_CODE_DISABLE", name = "공통 응답코드 비활성화", description = "공통 응답코드 비활성화 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** disableResponseCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> disableResponseCode(@PathVariable String code,
                                                 @RequestParam long expectedVersion,
                                                 @RequestAttribute("backoffice.operatorId") String operator,
                                                 @RequestHeader("X-CPF-Reason") String reason) {
        Object before = common.getResponseCode(code);
        common.deleteResponseCode(code, expectedVersion, requiredOperator(operator), requiredReason(reason));
        Map<String, Object> after = Map.of("responseCode", code, "useYn", "N");
        return ResponseEntity.ok(after);
    }

    @GetMapping("/messages")    @Operation(operationId = "MBW_COMMON_MESSAGE_SEARCH", summary = "공통 메시지 검색/Paging")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_MESSAGE_SEARCH", name = "공통 메시지 검색/Paging", description = "공통 메시지 검색/Paging 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** messages 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> messages(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String locale,
                                      @RequestParam(required = false) Boolean active,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(common.searchMessages(keyword, locale, active, page, size));
    }

    @GetMapping("/messages/{id}")    @Operation(operationId = "MBW_COMMON_MESSAGE_DETAIL", summary = "공통 메시지 상세")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_MESSAGE_DETAIL", name = "공통 메시지 상세", description = "공통 메시지 상세 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** message 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> message(@PathVariable long id) {
        return ResponseEntity.ok(common.getMessage(id));
    }

    @PostMapping("/messages")    @Operation(operationId = "MBW_COMMON_MESSAGE_CREATE", summary = "공통 메시지 등록")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_MESSAGE_CREATE", name = "공통 메시지 등록", description = "공통 메시지 등록 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** createMessage 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> createMessage(@Valid @RequestBody CommonMessageRequest request,
                                           @RequestAttribute("backoffice.operatorId") String operator,
                                           @RequestHeader("X-CPF-Reason") String reason) {
        Object after = common.createMessage(request, requiredOperator(operator), requiredReason(reason));
        return ResponseEntity.ok(after);
    }

    @PutMapping("/messages/{id}")    @Operation(operationId = "MBW_COMMON_MESSAGE_UPDATE", summary = "공통 메시지 수정")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_MESSAGE_UPDATE", name = "공통 메시지 수정", description = "공통 메시지 수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** updateMessage 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> updateMessage(@PathVariable long id,
                                           @RequestParam long expectedVersion,
                                           @Valid @RequestBody CommonMessageRequest request,
                                           @RequestAttribute("backoffice.operatorId") String operator,
                                           @RequestHeader("X-CPF-Reason") String reason) {
        Object before = common.getMessage(id);
        Object after = common.updateMessage(id, expectedVersion, request, requiredOperator(operator), requiredReason(reason));
        return ResponseEntity.ok(after);
    }

    @DeleteMapping("/messages/{id}")    @Operation(operationId = "MBW_COMMON_MESSAGE_DISABLE", summary = "공통 메시지 비활성화")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_MESSAGE_DISABLE", name = "공통 메시지 비활성화", description = "공통 메시지 비활성화 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** disableMessage 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> disableMessage(@PathVariable long id,
                                            @RequestParam long expectedVersion,
                                            @RequestAttribute("backoffice.operatorId") String operator,
                                            @RequestHeader("X-CPF-Reason") String reason) {
        Object before = common.getMessage(id);
        common.deleteMessage(id, expectedVersion, requiredOperator(operator), requiredReason(reason));
        Map<String, Object> after = Map.of("messageId", id, "useYn", "N");
        return ResponseEntity.ok(after);
    }

    @PostMapping("/cache/refresh")    @Operation(operationId = "MBW_COMMON_CATALOG_REFRESH", summary = "공통 Catalog cache refresh")
    @CpfOnlineTransaction(operationId = "MBW_COMMON_CATALOG_REFRESH", name = "공통 Catalog cache refresh", description = "공통 Catalog cache refresh 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    /** refresh 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<?> refresh(@RequestAttribute("backoffice.operatorId") String operator,
                                     @RequestHeader("X-CPF-Reason") String reason) {
        common.refreshCaches(requiredOperator(operator), requiredReason(reason));
        Map<String, Object> after = Map.of("refreshed", true);
        return ResponseEntity.ok(after);
    }
}
