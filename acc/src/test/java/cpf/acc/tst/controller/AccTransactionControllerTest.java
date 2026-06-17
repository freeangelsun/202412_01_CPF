package cpf.acc.tst.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ACC 嫄곕옒 濡쒓렇 ?섑뵆 而⑦듃濡ㅻ윭??MVC ?뚯뒪?몄엯?덈떎.
 *
 * <p>?꾩옱 ACC ?뚯뒪???쒖뒪?щ뒗 Gradle ?ㅼ젙?먯꽌 鍮꾪솢?깊솕?섏뼱 ?덉?留?
 * ???뚯뒪??肄붾뱶??媛쒕컻?먭? 濡쒖뺄?먯꽌 ACC 濡쒓렇 ?섑뵆??寃利앺븷 ??湲곗??쇰줈 ?ъ슜?????덈룄濡??좎??⑸땲??</p>
 *
 * <p>?붿껌?먮뒗 PFW ?꾩닔 ?낅Т ?ㅻ뜑瑜??④퍡 ?ｌ뒿?덈떎.
 * ?ㅼ젣 ?댁쁺 ?붿껌?????ㅻ뜑?ㅼ씠 ?덉뼱??而⑦듃濡ㅻ윭 吏꾩엯 ?④퀎?먯꽌 ?쒖? 寃利앹쓣 ?듦낵?⑸땲??</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * AOP 諛??대깽??湲곕컲 ?깃났 ?몃옖??뀡 濡쒓렇瑜?寃利앺빀?덈떎.
     *
     * <p>???붿껌???깃났?섎㈃ PFW 濡쒓렇?먮뒗 {@code LOG_TYPE=SUCCESS},
     * {@code WORKFLOW_STATUS=COMPLETED}媛 ?⑤뒗 寃껋씠 ?섎룄?낅땲??</p>
     */
    @Test
    void testSuccessfulTransaction() throws Exception {
        mockMvc.perform(get("/acc/tran/success")
                        // menuId???섑뵆 ?붾㈃/硫붾돱 ?앸퀎媛믪씠硫?濡쒓렇??MENU_ID 寃곗젙???ъ슜?????덉뒿?덈떎.
                        .param("menuId", "MENU123")
                        // execUser??嫄곕옒 ?섑뻾?먮? ?쒗쁽?섎뒗 ?섑뵆 媛믪엯?덈떎.
                        .param("execUser", "testUser")
                        // PFW ?꾩닔 ?낅Т ?ㅻ뜑瑜?異붽????ㅼ젣 而⑦듃濡ㅻ윭 吏꾩엯 議곌굔怨?留욎땅?덈떎.
                        .headers(requiredBusinessHeaders())
                        // GET ?붿껌?댁?留??뚯뒪???붿껌??而⑦뀗痢???낆쓣 낆떆??MockMvc ?붿껌???쇨??섍쾶 留뚮벊?덈떎.
                        .contentType(MediaType.APPLICATION_JSON))
                // ?뺤긽 ?섑뵆 嫄곕옒??HTTP 200??諛섑솚?댁빞 ?⑸땲??
                .andExpect(status().isOk());

    }

    /**
     * 蹂댁긽 嫄곕옒 ?섑뵆???뺤긽 醫낅즺?섎뒗吏 寃利앺빀?덈떎.
     *
     * <p>???붿껌???깃났?섎㈃ PFW 濡쒓렇?먮뒗 {@code COMPENSATION_YN=Y},
     * {@code COMPENSATION_STATUS=COMPENSATED}媛 ?⑤뒗 寃껋씠 ?섎룄?낅땲??</p>
     */
    @Test
    void testCompensationTransaction() throws Exception {
        mockMvc.perform(get("/acc/tran/compensate")
                        .param("menuId", "MENU123")
                        .param("execUser", "testUser")
                        .headers(requiredBusinessHeaders())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * ?뚯뒪???붿껌??諛섎났?쇰줈 ?ъ슜?섎뒗 PFW ?꾩닔 ?낅Т ?ㅻ뜑瑜??앹꽦?⑸땲??
     *
     * @return ?붿껌 援щ텇, 理쒖큹 梨꾨꼸, ?꾩옱 梨꾨꼸???ы븿??HTTP ?ㅻ뜑?낅땲??
     */
    private org.springframework.http.HttpHeaders requiredBusinessHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Request-Type", "INQUIRY");
        headers.add("X-Original-Channel-Code", "TST");
        headers.add("X-Channel-Code", "ACC");
        headers.add("X-User-Id", "testUser");
        return headers;
    }
}

