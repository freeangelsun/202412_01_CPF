package cpf.acc.tst.controller;

import cpf.cmn.cde.service.CodeCacheService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ACC 테스트 영역에서 CMN 코드 캐시 조회를 확인하는 컨트롤러입니다.
 *
 * <p>업무 모듈이 CMN 공통 코드 API를 어떻게 호출하는지 확인하기 위한 얇은 예시이며,
 * 실제 코드 조회 로직은 {@link CodeCacheService}에 위임합니다.</p>
 */
@RestController
@RequestMapping("/cpf/codes")
@Tag(name = "ACC-TST CMN 코드조회", description = "ACC 테스트용 CMN 공통 코드 조회 API")
public class CmnCoderController {

    private final CodeCacheService codeCacheService;

    @Autowired
    public CmnCoderController(CodeCacheService codeCacheService) {
        this.codeCacheService = codeCacheService;
    }

    /**
     * 캐시에 적재된 전체 공통 코드를 조회합니다.
     */
    @GetMapping
    @CpfTransaction(id = "ACC09TST0003", name = "ACC 공통 코드 전체 조회")
    public List<Map<String, Object>> getAllCodes() {
        return codeCacheService.getAllCodes();
    }

    /**
     * 코드 키 기준으로 단건 공통 코드를 조회합니다.
     */
    @GetMapping("/detail")
    @CpfTransaction(id = "ACC09TST0004", name = "ACC 공통 코드 단건 조회")
    public Map<String, Object> getCodeByKey(@RequestParam("codeKey") String codeKey) {
        return codeCacheService.getCodeByKey(codeKey);
    }
}
