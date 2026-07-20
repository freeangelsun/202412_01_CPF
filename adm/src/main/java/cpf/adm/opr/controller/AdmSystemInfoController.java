package cpf.adm.opr.controller;

import cpf.adm.common.base.AdmBaseController;
import cpf.pfw.common.version.CpfPlatformVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** CPF 제품과 구성요소 버전을 조회하는 운영 API입니다. */
@RestController
@RequestMapping("/adm/api/v1/system")
@Tag(name = "ADM 시스템 정보", description = "CPF platform 및 구성요소 버전 정보")
public class AdmSystemInfoController extends AdmBaseController {

    /**
     * 현재 실행 산출물의 버전 정보를 반환합니다.
     *
     * @return platform, component와 호환 범위
     */
    @GetMapping("/version")
    @Operation(operationId = "getAdmSystemVersion", summary = "CPF 시스템 버전 조회")
    public ResponseEntity<Map<String, String>> version() {
        return ok(Map.of(
                "platformVersion", CpfPlatformVersion.current(),
                "componentVersion", CpfPlatformVersion.componentVersion(),
                "compatibleRange", CpfPlatformVersion.compatibleRange(),
                "component", "adm"));
    }
}
