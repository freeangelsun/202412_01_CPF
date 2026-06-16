package cpf.adm.opr.controller;

import cpf.cmn.msg.dto.CommonResponseCodeRequest;
import cpf.cmn.msg.service.ResponseCodeCacheService;
import cpf.pfw.common.logging.FpsTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM response-code catalog API.
 */
@RestController
@RequestMapping("/adm/api/response-codes")
@Tag(name = "ADM-OPR Response Codes", description = "response_code_table management API")
public class AdmResponseCodeController {
    private final ResponseCodeCacheService responseCodeCacheService;

    public AdmResponseCodeController(ResponseCodeCacheService responseCodeCacheService) {
        this.responseCodeCacheService = responseCodeCacheService;
    }

    @GetMapping
    @FpsTransaction(id = "ADM01OPR0040", name = "ADMResponseCodeList")
    @Operation(summary = "List response codes", description = "Lists active response codes from response_code_table.")
    public ResponseEntity<Map<String, Object>> findAll() {
        return safeResponse(() -> responseCodeCacheService.getAllResponseCodes());
    }

    @GetMapping("/{responseCode}")
    @FpsTransaction(id = "ADM01OPR0041", name = "ADMResponseCodeDetail")
    @Operation(summary = "Get response code", description = "Gets one active response code from response_code_table.")
    public ResponseEntity<Map<String, Object>> findOne(@PathVariable String responseCode) {
        return safeResponse(() -> responseCodeCacheService.getResponseCode(responseCode));
    }

    @PostMapping
    @FpsTransaction(id = "ADM02OPR0042", name = "ADMResponseCodeCreate")
    @Operation(summary = "Create response code", description = "Creates a response code and refreshes responseCodeCache.")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CommonResponseCodeRequest request) {
        return safeResponse(() -> responseCodeCacheService.createResponseCode(request));
    }

    @PutMapping("/{responseCode}")
    @FpsTransaction(id = "ADM03OPR0043", name = "ADMResponseCodeUpdate")
    @Operation(summary = "Update response code", description = "Updates a response code and refreshes responseCodeCache.")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String responseCode,
            @Valid @RequestBody CommonResponseCodeRequest request) {
        return safeResponse(() -> responseCodeCacheService.updateResponseCode(responseCode, request));
    }

    @DeleteMapping("/{responseCode}")
    @FpsTransaction(id = "ADM04OPR0044", name = "ADMResponseCodeDelete")
    @Operation(summary = "Delete response code", description = "Deletes a response code and refreshes responseCodeCache.")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String responseCode) {
        return safeResponse(() -> responseCodeCacheService.deleteResponseCode(responseCode));
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
            response.put("message", "response_code_table operation failed. Check cmnDB schema and seed data.");
            response.put("detail", ex.getMostSpecificCause().getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @FunctionalInterface
    private interface ResponseCodeAction {
        Object run();
    }
}

