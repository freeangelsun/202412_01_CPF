package cpf.xyz.edu.controller;

import cpf.pfw.common.logging.DynamicLogLevelRequest;
import cpf.pfw.common.logging.DynamicLogLevelRule;
import cpf.pfw.common.logging.DynamicTransactionLogLevelService;
import cpf.pfw.common.logging.FpsLogLevel;
import cpf.pfw.common.logging.FpsTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ?뱀젙 嫄곕옒留??댁쁺 以??꾩떆 ?곸꽭 濡쒓렇?덈꺼濡??щ━??諛⑸쾿??蹂댁뿬二쇰뒗 援먯쑁??而⑦듃濡ㅻ윭?낅땲??
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 10. ?숈쟻 濡쒓렇?덈꺼", description = "?뱀젙 嫄곕옒 DEBUG/TRACE ?꾩떆 ?꾪솚 ?섑뵆")
public class XyzDynamicLogEducationController {
    private final DynamicTransactionLogLevelService dynamicLogLevelService;

    public XyzDynamicLogEducationController(DynamicTransactionLogLevelService dynamicLogLevelService) {
        this.dynamicLogLevelService = dynamicLogLevelService;
    }

    /**
     * ?숈쟻 濡쒓렇?덈꺼 洹쒖튃???깅줉?⑸땲??
     *
     * @param businessTransactionId ?낅Т 嫄곕옒ID
     * @param transactionId         湲濡쒕쾶 嫄곕옒ID
     * @param logLevel              ?곸슜 濡쒓렇?덈꺼
     * @param ttlSeconds            ?좎? ?쒓컙
     * @param reason                ?깅줉 ?ъ쑀
     * @param requestUser           ?깅줉??     * @return ?깅줉??洹쒖튃
     */
    @PutMapping("/admin/log-level")
    @FpsTransaction(id = "XYZ09EDU0005", name = "XYZ援먯쑁?숈쟻濡쒓렇?덈꺼?깅줉")
    @Operation(summary = "?숈쟻 濡쒓렇?덈꺼 ?깅줉", description = "?댁쁺 以??뱀젙 嫄곕옒ID ?먮뒗 ?낅Т 嫄곕옒ID留??꾩떆 DEBUG/TRACE濡??щ━??洹쒖튃???깅줉?⑸땲??")
    public ResponseEntity<DynamicLogLevelRule> registerDynamicLogLevel(
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(defaultValue = "DEBUG") FpsLogLevel logLevel,
            @RequestParam(defaultValue = "600") long ttlSeconds,
            @RequestParam(defaultValue = "XYZ 援먯쑁???숈쟻 濡쒓렇?덈꺼 ?섑뵆") String reason,
            @RequestParam(defaultValue = "SYSTEM") String requestUser) {

        DynamicLogLevelRequest request = new DynamicLogLevelRequest();
        request.setBusinessTransactionId(businessTransactionId);
        request.setTransactionId(transactionId);
        request.setModuleId("XYZ");
        request.setLogLevel(logLevel);
        request.setTtl(Duration.ofSeconds(ttlSeconds));
        request.setReason(reason);
        request.setRequestUser(requestUser);
        return ResponseEntity.ok(dynamicLogLevelService.register(request));
    }

    /**
     * ?꾩옱 WAS 硫붾え由ъ뿉 ?깅줉???숈쟻 濡쒓렇?덈꺼 洹쒖튃??議고쉶?⑸땲??
     *
     * @return ?숈쟻 濡쒓렇?덈꺼 洹쒖튃 ⑸줉
     */
    @GetMapping("/admin/log-level")
    @FpsTransaction(id = "XYZ09EDU0006", name = "XYZ援먯쑁?숈쟻濡쒓렇?덈꺼議고쉶")
    @Operation(summary = "?숈쟻 濡쒓렇?덈꺼 議고쉶", description = "?꾩옱 WAS 硫붾え由ъ뿉 ?깅줉???숈쟻 濡쒓렇?덈꺼 洹쒖튃??議고쉶?⑸땲??")
    public ResponseEntity<List<DynamicLogLevelRule>> findDynamicLogLevelRules() {
        return ResponseEntity.ok(dynamicLogLevelService.findActiveRules());
    }

    /**
     * ?숈쟻 濡쒓렇?덈꺼 洹쒖튃????젣?⑸땲??
     *
     * @param ruleId 洹쒖튃 ID
     * @return ??젣 寃곌낵
     */
    @DeleteMapping("/admin/log-level")
    @FpsTransaction(id = "XYZ09EDU0007", name = "XYZ援먯쑁?숈쟻濡쒓렇?덈꺼??젣")
    @Operation(summary = "?숈쟻 濡쒓렇?덈꺼 ??젣", description = "?숈쟻 濡쒓렇?덈꺼 洹쒖튃 ID濡??꾩떆 吏꾨떒 ?ㅼ젙????젣?⑸땲??")
    public ResponseEntity<Map<String, Object>> removeDynamicLogLevelRule(@RequestParam String ruleId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("removed", dynamicLogLevelService.remove(ruleId));
        response.put("ruleId", ruleId);
        return ResponseEntity.ok(response);
    }
}

