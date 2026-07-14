package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmAuditLogService;
import cpf.cmn.msg.dto.CommonResponseCodeRequest;
import cpf.cmn.msg.service.ResponseCodeCacheService;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
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
import java.util.Map;

/**
 * ADM response-code catalog API.
 */
@RestController
@RequestMapping("/adm/api/response-codes")
@Tag(name = "ADM-OPR Response Codes", description = "pfw_response_code management API")
public class AdmResponseCodeController {
    private final ResponseCodeCacheService responseCodeCacheService;
    private final AdmAuditLogService auditLogService;

    public AdmResponseCodeController(ResponseCodeCacheService responseCodeCacheService, AdmAuditLogService auditLogService) {
        this.responseCodeCacheService = responseCodeCacheService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfTransaction(id = "ADM01OPR0040", name = "ADMResponseCodeList")
    @Operation(operationId = "admResponseCodeFindAll", summary = "List response codes", description = "Lists active response codes from pfw_response_code.")
    public ResponseEntity<Map<String, Object>> findAll() {
        return safeResponse(() -> responseCodeCacheService.getAllResponseCodes());
    }

    @GetMapping("/{responseCode}")
    @CpfTransaction(id = "ADM01OPR0041", name = "ADMResponseCodeDetail")
    @Operation(operationId = "admResponseCodeFindOne", summary = "Get response code", description = "Gets one active response code from pfw_response_code.")
    public ResponseEntity<Map<String, Object>> findOne(@PathVariable String responseCode) {
        return safeResponse(() -> responseCodeCacheService.getResponseCode(responseCode));
    }

    @PostMapping
    @CpfTransaction(id = "ADM02OPR0042", name = "ADMResponseCodeCreate")
    @Operation(operationId = "admResponseCodeCreate", summary = "Create response code", description = "Creates a response code and refreshes responseCodeCache.")
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody CommonResponseCodeRequest request,
            @RequestParam String reason,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        ResponseEntity<Map<String, Object>> response = safeResponse(() -> responseCodeCacheService.createResponseCode(request));
        recordAudit(servletRequest, request.getRequestUser(), "RESPONSE_CODE_CREATE", request.getResponseCode(), auditReason);
        return response;
    }

    @PutMapping("/{responseCode}")
    @CpfTransaction(id = "ADM03OPR0043", name = "ADMResponseCodeUpdate")
    @Operation(operationId = "admResponseCodeUpdate", summary = "Update response code", description = "Updates a response code and refreshes responseCodeCache.")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String responseCode,
            @Valid @RequestBody CommonResponseCodeRequest request,
            @RequestParam String reason,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        ResponseEntity<Map<String, Object>> response = safeResponse(() -> responseCodeCacheService.updateResponseCode(responseCode, request));
        recordAudit(servletRequest, request.getRequestUser(), "RESPONSE_CODE_UPDATE", responseCode, auditReason);
        return response;
    }

    @DeleteMapping("/{responseCode}")
    @CpfTransaction(id = "ADM04OPR0044", name = "ADMResponseCodeDelete")
    @Operation(operationId = "admResponseCodeDelete", summary = "Delete response code", description = "Deletes a response code and refreshes responseCodeCache.")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable String responseCode,
            @RequestParam String reason,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        ResponseEntity<Map<String, Object>> response = safeResponse(() -> responseCodeCacheService.deleteResponseCode(responseCode));
        recordAudit(servletRequest, requestUser, "RESPONSE_CODE_DELETE", responseCode, auditReason);
        return response;
    }

    private ResponseEntity<Map<String, Object>> safeResponse(ResponseCodeAction action) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("available", true);
            response.put("result", action.run());
        } catch (IllegalArgumentException ex) {
            response.put("available", false);
            response.put("message", ex.getMessage());
        } catch (DataAccessException ex) {
            response.put("available", false);
            response.put("result", Map.of());
            response.put("message", "pfw_response_code operation failed. Check pfwDB schema and seed data.");
            response.put("detail", ex.getMostSpecificCause().getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @FunctionalInterface
    private interface ResponseCodeAction {
        Object run();
    }

    private void recordAudit(
            HttpServletRequest servletRequest,
            String requestUser,
            String actionType,
            String responseCode,
            String reason) {

        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, requestUser),
                actionType,
                "pfw_response_code",
                responseCode,
                reason,
                servletRequest.getRemoteAddr());
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}

