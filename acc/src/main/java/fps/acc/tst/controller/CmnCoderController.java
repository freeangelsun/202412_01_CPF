package fps.acc.tst.controller;

import fps.pfw.common.logging.FpsTransaction;
import fps.cmn.cde.service.CodeCacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ACC 테스트 주제영역에서 CMN 공통 코드 캐시를 조회하는 샘플 컨트롤러입니다.
 *
 * <p>CMN은 별도 WAS로 뜨는 서비스가 아니라 개발 공통 라이브러리 모듈입니다.
 * 따라서 ACC 애플리케이션이 빌드될 때 CMN 코드가 함께 포함되고,
 * 이 컨트롤러는 포함된 {@link CodeCacheService}를 직접 주입받아 호출합니다.</p>
 *
 * <p>PFW 로그 관점에서는 이 컨트롤러도 일반 컨트롤러와 동일하게
 * {@link FpsTransaction}의 업무 거래ID와 거래명이 TRAN_LOG에 적재됩니다.</p>
 */
@RestController
@RequestMapping("/fps/codes")
@Tag(name = "ACC-TST CMN 코드조회", description = "CMN 공통 코드 캐시 조회 샘플 API")
public class CmnCoderController {

    private final CodeCacheService codeCacheService;

    @Autowired
    public CmnCoderController(CodeCacheService codeCacheService) {
        // 공통 코드 조회/캐시 재적재 기능을 제공하는 CMN 서비스입니다.
        this.codeCacheService = codeCacheService;
    }

    /**
     * CMN 공통 코드 전체 목록을 반환합니다.
     *
     * @return CMN 코드 테이블에서 조회했거나 캐시에서 가져온 코드 목록입니다.
     */
    @GetMapping
    @FpsTransaction(id = "ACC09TST0003", name = "공통코드목록조회샘플")
    public List<Map<String, Object>> getAllCodes() {
        // 서비스가 내부 캐시를 사용하므로 반복 조회 시 DB 부하를 줄일 수 있습니다.
        return codeCacheService.getAllCodes();
    }

    /**
     * 특정 코드 키에 해당하는 CMN 공통 코드 정보를 반환합니다.
     *
     * @param codeKey 조회할 코드 키입니다. 예: USER_ROLE, USER_STATUS
     * @return 코드 키에 해당하는 코드 데이터입니다.
     */
    @GetMapping("/detail")
    @FpsTransaction(id = "ACC09TST0004", name = "공통코드상세조회샘플")
    public Map<String, Object> getCodeByKey(@RequestParam("codeKey") String codeKey) {
        // 명시 쿼리 파라미터로 받은 codeKey를 캐시 서비스에 전달해 단건 코드를 조회합니다.
        return codeCacheService.getCodeByKey(codeKey);
    }
}
