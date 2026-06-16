package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmLogQueryService;
import cpf.pfw.common.logging.FpsTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/logs")
@Tag(name = "ADM-OPR Logs", description = "PFW transaction log query APIs")
public class AdmLogController {
    private final AdmLogQueryService logQueryService;

    public AdmLogController(AdmLogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping
    @FpsTransaction(id = "ADM01OPR0001", name = "ADMTransactionLogList")
    @Operation(summary = "List transaction logs", description = "Searches PFW transaction logs by transaction, business transaction, member, or customer.")
    public ResponseEntity<Map<String, Object>> findLogs(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String memberNo,
            @RequestParam(required = false) String customerNo,
            @RequestParam(defaultValue = "50") int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("available", true);
            response.put("items", logQueryService.findLogs(transactionId, businessTransactionId, memberNo, customerNo, limit));
        } catch (DataAccessException ex) {
            response.put("available", false);
            response.put("items", java.util.List.of());
            response.put("message", "PFW transaction log database is not available.");
            response.put("detail", ex.getMostSpecificCause().getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{logIdx}")
    @FpsTransaction(id = "ADM01OPR0002", name = "ADMTransactionLogDetail")
    @Operation(summary = "Transaction log detail", description = "Returns a TRAN_LOG row and related detail rows.")
    public ResponseEntity<Map<String, Object>> getLogDetail(@PathVariable Long logIdx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("available", true);
            response.put("item", logQueryService.getLogDetail(logIdx));
        } catch (DataAccessException ex) {
            response.put("available", false);
            response.put("item", null);
            response.put("message", "PFW transaction log detail is not available.");
            response.put("detail", ex.getMostSpecificCause().getMessage());
        }
        return ResponseEntity.ok(response);
    }
}