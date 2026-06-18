package cpf.acc.tst.controller;

import cpf.pfw.common.logging.CpfTransaction;
import cpf.cmn.cde.service.CodeCacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@RestController
@RequestMapping("/cpf/codes")
@Tag(name = "ACC-TST CMN 肄붾뱶議고쉶", description = "CPF 처리 기준입니다.")
public class CmnCoderController {

    private final CodeCacheService codeCacheService;

    @Autowired
    public CmnCoderController(CodeCacheService codeCacheService) {
        // CPF 기능 설명입니다.
        this.codeCacheService = codeCacheService;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @GetMapping
    @CpfTransaction(id = "ACC09TST0003", name = "CPF 처리 기준입니다.")
    public List<Map<String, Object>> getAllCodes() {
        // CPF 기능 설명입니다.
        return codeCacheService.getAllCodes();
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @GetMapping("/detail")
    @CpfTransaction(id = "ACC09TST0004", name = "CPF 처리 기준입니다.")
    public Map<String, Object> getCodeByKey(@RequestParam("codeKey") String codeKey) {
        // CPF 기능 설명입니다.
        return codeCacheService.getCodeByKey(codeKey);
    }
}

