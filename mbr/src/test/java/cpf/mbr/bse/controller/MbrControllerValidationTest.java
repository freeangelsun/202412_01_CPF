package cpf.mbr.bse.controller;

import cpf.mbr.bse.service.MbrService;
import cpf.mbr.common.exception.GlobalExceptionHandler;
import cpf.mbr.common.filter.SecurityHeaderFilter;
import cpf.pfw.common.exception.CpfGlobalExceptionHandler;
import cpf.pfw.common.filter.TransactionContextFilter;
import cpf.pfw.common.logging.TransactionIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MBR Controller의 요청 경로, 필수 거래 헤더, 입력값 검증 응답을 확인합니다.
 */
@WebMvcTest(
        controllers = MbrController.class,
        properties = {
                "cpf.cmn.cache.preload-enabled=false",
                "cpf.cmn.cache.event-poll-enabled=false"
        }
)
@Import({
        GlobalExceptionHandler.class,
        CpfGlobalExceptionHandler.class,
        TransactionContextFilter.class,
        TransactionIdGenerator.class,
        SecurityHeaderFilter.class
})
class MbrControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MbrService mbrService;

    /**
     * 회원 ID를 path variable로 받는 과거 형식이 노출되지 않는지 확인합니다.
     */
    @Test
    void detailDoesNotExposeMemberIdAsPathVariable() throws Exception {
        mockMvc.perform(get("/mbr/1"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Transaction-Id", matchesPattern("\\d{17}MBRlocal01\\d{7}")));
    }

    /**
     * 양수가 아닌 회원 ID 요청은 validation 오류로 응답합니다.
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
     * 숫자가 아닌 회원 ID 요청은 타입 변환 오류로 응답합니다.
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
     * CPF 필수 거래 헤더가 없으면 PFW 공통 오류로 응답합니다.
     */
    @Test
    void detailRejectsMissingRequiredBusinessHeaders() throws Exception {
        mockMvc.perform(get("/mbr/detail")
                        .param("memberId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("EPFW900001"))
                .andExpect(jsonPath("$.messageCode").value("MPFW900001"))
                .andExpect(jsonPath("$.message").value(containsString("필수 거래 헤더가 누락되었습니다.")));
    }

    /**
     * 회원명은 공백으로 등록할 수 없습니다.
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
     * Controller 테스트에서 공통 헤더 통과가 필요한 요청에 사용할 표준 거래 헤더입니다.
     */
    private org.springframework.http.HttpHeaders requiredBusinessHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Transaction-Id", "20260611141234567MBRlocal010000001");
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
