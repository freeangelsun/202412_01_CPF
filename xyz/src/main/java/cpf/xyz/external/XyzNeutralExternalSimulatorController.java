package cpf.xyz.external;

import cpf.xyz.common.base.XyzBaseController;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * timeout, 5xx와 정상 응답을 재현하는 로컬 전용 중립 외부 시스템 시뮬레이터입니다.
 *
 * <p>운영 profile에는 등록되지 않으며 지연 시간도 5초로 제한해 EDU 오용을 방지합니다.</p>
 */
@RestController
@Profile({"local", "dev", "test"})
@RequestMapping({"/api/xyz/reference/external-simulator", "/xyz/edu/external-simulator"})
@Tag(name = "XYZ 외부연계 시뮬레이터", description = "PFW 외부 호출 복원력 교육을 위한 로컬 전용 fake server")
public class XyzNeutralExternalSimulatorController extends XyzBaseController {

    @GetMapping("/response")
    @CpfOnlineTransaction(
            id = "OXYZEX0001", name = "XYZ 외부연계 시뮬레이터", ownerDomain = "XYZ",
            description = "로컬 교육 환경에서 정상·지연·오류 응답을 재현합니다.",
            visibility = "INTERNAL", gatewayAllowed = false)
    @Operation(operationId = "simulateNeutralExternalResponse", summary = "중립 외부 시스템 응답 시뮬레이션")
    public ResponseEntity<Map<String, Object>> response(
            @RequestParam(defaultValue = "200") int status,
            @RequestParam(defaultValue = "0") long delayMillis,
            @RequestParam(defaultValue = "EDU") String externalKey) {
        if (status < 200 || status > 599) {
            return ResponseEntity.badRequest().body(Map.of("code", "INVALID_STATUS"));
        }
        sleep(Math.max(0, Math.min(delayMillis, 5000)));
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(Map.of(
                "externalKey", externalKey,
                "status", status,
                "processedAt", Instant.now().toString()));
    }

    private void sleep(long delayMillis) {
        if (delayMillis == 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("외부 시스템 시뮬레이션 대기가 중단되었습니다.", ex);
        }
    }
}
