package cpf.mbr.bse.controller;

import cpf.mbr.bse.service.MbrService;
import cpf.mbr.common.exception.GlobalExceptionHandler;
import cpf.pfw.common.exception.FpsGlobalExceptionHandler;
import cpf.pfw.common.filter.TransactionContextFilter;
import cpf.pfw.common.logging.TransactionIdGenerator;
import cpf.mbr.common.filter.SecurityHeaderFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.matchesPattern;

/**
 * MBR 而⑦듃濡ㅻ윭???낅젰媛?寃利앷낵 PFW 怨듯넻 ?ㅻ뜑 泥섎━瑜??뺤씤?섎뒗 MVC ?뚯뒪?몄엯?덈떎.
 *
 * <p>???뚯뒪?몃뒗 ?ㅼ젣 DB瑜??몄텧?섏? ?딄퀬, {@link MbrService}瑜?Mockito Bean?쇰줈 ?泥댄빀?덈떎.
 * ?곕씪??而⑦듃濡ㅻ윭 吏꾩엯 ?꾪썑??寃利? ?쒖? ?먮윭 ?묐떟, 嫄곕옒 ?ㅻ뜑 ?앹꽦/諛섑솚 ?숈옉留?鍮좊Ⅴ寃??뺤씤?????덉뒿?덈떎.</p>
 *
 * <p>湲덉쑖沅?API 湲곗??쇰줈 ?뚯썝 ID瑜?{@code /mbr/{id}}泥섎읆 寃쎈줈??臾살? ?딄퀬
 * {@code /mbr/detail?memberId=...}泥섎읆 紐낆떆 ?뚮씪誘명꽣濡?諛쏅뒗 ?뺤콉???④퍡 寃利앺빀?덈떎.</p>
 */
@WebMvcTest(
        controllers = MbrController.class,
        properties = {
                "cpf.cmn.cache.preload-enabled=false",
                "cpf.cmn.cache.event-poll-enabled=false"
        }
)
@Import({GlobalExceptionHandler.class, FpsGlobalExceptionHandler.class, TransactionContextFilter.class, TransactionIdGenerator.class, SecurityHeaderFilter.class})
class MbrControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MbrService mbrService;

    /**
     * ?뚯썝 ID瑜?寃쎈줈 蹂?섎줈 ?몄텧?섎뒗 API媛 ?대젮 ?덉? ?딆?吏 ?뺤씤?⑸땲??
     *
     * <p>???뚯뒪?멸? ?듦낵?섎㈃ {@code GET /mbr/1}? ?곸꽭議고쉶濡?泥섎━?섏? ?딄퀬 404濡??묐떟?⑸땲??
     * PFW ?꾪꽣??404 ?묐떟?먮룄 嫄곕옒ID ?ㅻ뜑瑜??대젮二쇰?濡??μ븷 異붿쟻??媛?ν빀?덈떎.</p>
     */
    @Test
    void detailDoesNotExposeMemberIdAsPathVariable() throws Exception {
        mockMvc.perform(get("/mbr/1"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Transaction-Id", matchesPattern("\\d{17}MBRlocal01\\d{7}")));
    }

    /**
     * ?뚯썝 ?곸꽭議고쉶?먯꽌 0 ?댄븯???뚯썝 ID瑜?嫄곕??섎뒗吏 ?뺤씤?⑸땲??
     *
     * <p>?붿껌???좏슚??{@code X-Transaction-Id}? {@code X-Trace-Id}瑜??ｌ쑝硫?     * ?묐떟 蹂몃Ц怨??묐떟 ?ㅻ뜑?먮룄 媛숈? 媛믪씠 ?좎??섏뼱???⑸땲??</p>
     */
    @Test
    void detailRejectsInvalidMemberIdQueryParameter() throws Exception {
        String transactionId = "20260611141234567MBRmbrAP010000001";

        mockMvc.perform(get("/mbr/detail")
                        .param("memberId", "0")
                        .header("X-Transaction-Id", transactionId)
                        .header("X-Trace-Id", "TRACE-TEST-001")
                        .headers(requiredBusinessHeaders()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("EMBR010005"))
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.traceId").value("TRACE-TEST-001"))
                .andExpect(header().string("X-Transaction-Id", transactionId))
                .andExpect(header().string("X-Trace-Id", "TRACE-TEST-001"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    /**
     * ?뚯썝 ID ??낆씠 ?レ옄媛 ?꾨땺 ???쒖? ?뚮씪誘명꽣 ?ㅻ쪟濡??묐떟?섎뒗吏 ?뺤씤?⑸땲??
     */
    @Test
    void detailRejectsWrongMemberIdType() throws Exception {
        mockMvc.perform(get("/mbr/detail")
                        .param("memberId", "abc")
                        .headers(requiredBusinessHeaders()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("EMBR010002"));
    }

    /**
     * PFW ?꾩닔 ?낅Т ?ㅻ뜑媛 ?놁쑝硫?而⑦듃濡ㅻ윭 ?낅Т 濡쒖쭅?쇰줈 吏꾩엯?섏? ?딅뒗吏 ?뺤씤?⑸땲??
     */
    @Test
    void detailRejectsMissingRequiredBusinessHeaders() throws Exception {
        mockMvc.perform(get("/mbr/detail")
                        .param("memberId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("EPFW900001"))
                .andExpect(jsonPath("$.messageCode").value("MPFW900001"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("필수 거래 헤더가 누락되었습니다.")));
    }

    /**
     * ?뚯썝 ?깅줉 ?붿껌?먯꽌 ?꾩닔 Body 媛믪씠 鍮꾩뼱 ?덉쑝硫?Bean Validation ?ㅻ쪟濡??묐떟?섎뒗吏 ?뺤씤?⑸땲??
     */
    @Test
    void createRejectsBlankMemberName() throws Exception {
        mockMvc.perform(post("/mbr/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(requiredBusinessHeaders())
                        .content("{\"memberName\":\"\",\"description\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("EMBR010005"));
    }

    /**
     * ?뚯뒪???붿껌??諛섎났?쇰줈 ?꾩슂??PFW ?낅Т ?ㅻ뜑瑜??앹꽦?⑸땲??
     *
     * <p>{@code X-Request-Type}, {@code X-Original-Channel-Code}, {@code X-Channel-Code}??     * 而⑦듃濡ㅻ윭 吏꾩엯 ?꾩뿉 寃利앸릺???꾩닔 ?ㅻ뜑?낅땲?? ?섎㉧吏 ?뚯썝踰덊샇, IP, ?덉빟 ?꾨뱶??     * 濡쒓렇 寃?됯낵 ?뺤옣 ?꾨뱶 ?곸옱瑜??뺤씤?섍린 ?꾪븳 ?좏깮 ?ㅻ뜑?낅땲??</p>
     *
     * @return MBR 而⑦듃濡ㅻ윭 ?붿껌???ъ슜???쒖? ?낅Т ?ㅻ뜑?낅땲??
     */
    private org.springframework.http.HttpHeaders requiredBusinessHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Request-Type", "INQUIRY");
        headers.add("X-Original-Channel-Code", "MBL");
        headers.add("X-Channel-Code", "MBR");
        headers.add("X-Member-No", "10000001");
        headers.add("X-Client-IP", "10.10.10.10");
        headers.add("X-Reserved-Field-1", "reserved-1");
        headers.add("X-Reserved-Field-2", "reserved-2");
        return headers;
    }
}

