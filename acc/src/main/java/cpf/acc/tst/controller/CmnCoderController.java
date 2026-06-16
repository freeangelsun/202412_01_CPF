package cpf.acc.tst.controller;

import cpf.pfw.common.logging.FpsTransaction;
import cpf.cmn.cde.service.CodeCacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ACC ?뚯뒪??二쇱젣?곸뿭?먯꽌 CMN 怨듯넻 肄붾뱶 罹먯떆瑜?議고쉶?섎뒗 ?섑뵆 而⑦듃濡ㅻ윭?낅땲??
 *
 * <p>CMN? 蹂꾨룄 WAS濡??⑤뒗 ?쒕퉬?ㅺ? ?꾨땲??媛쒕컻 怨듯넻 ?쇱씠釉뚮윭由?紐⑤뱢?낅땲??
 * ?곕씪??ACC ?좏뵆由ъ??댁뀡??鍮뚮뱶????CMN 肄붾뱶媛 ?④퍡 ?ы븿?섍퀬,
 * ??而⑦듃濡ㅻ윭???ы븿??{@link CodeCacheService}瑜?吏곸젒 二쇱엯諛쏆븘 ?몄텧?⑸땲??</p>
 *
 * <p>PFW 濡쒓렇 愿?먯뿉?쒕뒗 ??而⑦듃濡ㅻ윭???쇰컲 而⑦듃濡ㅻ윭? ?숈씪?섍쾶
 * {@link FpsTransaction}???낅Т 嫄곕옒ID? 嫄곕옒紐낆씠 TRAN_LOG???곸옱?⑸땲??</p>
 */
@RestController
@RequestMapping("/fps/codes")
@Tag(name = "ACC-TST CMN 肄붾뱶議고쉶", description = "CMN 怨듯넻 肄붾뱶 罹먯떆 議고쉶 ?섑뵆 API")
public class CmnCoderController {

    private final CodeCacheService codeCacheService;

    @Autowired
    public CmnCoderController(CodeCacheService codeCacheService) {
        // 怨듯넻 肄붾뱶 議고쉶/罹먯떆 ?ъ쟻??湲곕뒫???쒓났?섎뒗 CMN ?쒕퉬?ㅼ엯?덈떎.
        this.codeCacheService = codeCacheService;
    }

    /**
     * CMN 怨듯넻 肄붾뱶 ?꾩껜 紐⑸줉??諛섑솚?⑸땲??
     *
     * @return CMN 肄붾뱶 ?뚯씠釉붿뿉??議고쉶?덇굅??罹먯떆?먯꽌 媛?몄삩 肄붾뱶 紐⑸줉?낅땲??
     */
    @GetMapping
    @FpsTransaction(id = "ACC09TST0003", name = "怨듯넻肄붾뱶紐⑸줉議고쉶?섑뵆")
    public List<Map<String, Object>> getAllCodes() {
        // ?쒕퉬?ㅺ? ?대? 罹먯떆瑜??ъ슜?섎?濡?諛섎났 議고쉶 ??DB 遺?섎? 以꾩씪 ???덉뒿?덈떎.
        return codeCacheService.getAllCodes();
    }

    /**
     * ?뱀젙 肄붾뱶 ?ㅼ뿉 ?대떦?섎뒗 CMN 怨듯넻 肄붾뱶 ?뺣낫瑜?諛섑솚?⑸땲??
     *
     * @param codeKey 議고쉶??肄붾뱶 ?ㅼ엯?덈떎. ?? USER_ROLE, USER_STATUS
     * @return 肄붾뱶 ?ㅼ뿉 ?대떦?섎뒗 肄붾뱶 ?곗씠?곗엯?덈떎.
     */
    @GetMapping("/detail")
    @FpsTransaction(id = "ACC09TST0004", name = "怨듯넻肄붾뱶?곸꽭議고쉶?섑뵆")
    public Map<String, Object> getCodeByKey(@RequestParam("codeKey") String codeKey) {
        // 紐낆떆 荑쇰━ ?뚮씪誘명꽣濡?諛쏆? codeKey瑜?罹먯떆 ?쒕퉬?ㅼ뿉 ?꾨떖???④굔 肄붾뱶瑜?議고쉶?⑸땲??
        return codeCacheService.getCodeByKey(codeKey);
    }
}

