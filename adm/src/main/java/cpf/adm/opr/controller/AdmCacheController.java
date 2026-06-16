package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmCacheOperationService;
import cpf.pfw.common.logging.FpsTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/cache")
@Tag(name = "ADM-OPR Cache", description = "CMN cache summary and refresh APIs")
public class AdmCacheController {
    private final AdmCacheOperationService cacheOperationService;

    public AdmCacheController(AdmCacheOperationService cacheOperationService) {
        this.cacheOperationService = cacheOperationService;
    }

    @GetMapping("/summary")
    @FpsTransaction(id = "ADM01OPR0010", name = "ADMCacheSummary")
    @Operation(summary = "Cache summary", description = "Returns CMN cache counts and samples.")
    public ResponseEntity<Map<String, Object>> summary() {
        return safeResponse(cacheOperationService::summary);
    }

    @PostMapping("/refresh")
    @FpsTransaction(id = "ADM05OPR0011", name = "ADMCacheRefresh")
    @Operation(summary = "Refresh cache", description = "Refreshes CODE, MESSAGE, RESPONSE_CODE, CONFIG, or ALL cache targets.")
    public ResponseEntity<Map<String, Object>> refresh(@RequestParam(defaultValue = "ALL") String target) {
        return safeResponse(() -> cacheOperationService.refresh(target));
    }

    private ResponseEntity<Map<String, Object>> safeResponse(CacheAction action) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("available", true);
            response.put("result", action.run());
        } catch (DataAccessException ex) {
            response.put("available", false);
            response.put("result", Map.of());
            response.put("message", "CMN cache database is not available.");
            response.put("detail", ex.getMostSpecificCause().getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @FunctionalInterface
    private interface CacheAction {
        Map<String, Object> run();
    }
}