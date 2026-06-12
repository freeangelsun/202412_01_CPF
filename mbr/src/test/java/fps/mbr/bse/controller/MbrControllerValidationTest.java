package fps.mbr.bse.controller;

import fps.mbr.bse.service.MbrService;
import fps.mbr.common.exception.GlobalExceptionHandler;
import fps.pfw.common.exception.FpsGlobalExceptionHandler;
import fps.pfw.common.filter.TransactionContextFilter;
import fps.pfw.common.logging.TransactionIdGenerator;
import fps.mbr.common.filter.SecurityHeaderFilter;
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
 * MBR 컨트롤러의 입력값 검증과 PFW 공통 헤더 처리를 확인하는 MVC 테스트입니다.
 *
 * <p>이 테스트는 실제 DB를 호출하지 않고, {@link MbrService}를 Mockito Bean으로 대체합니다.
 * 따라서 컨트롤러 진입 전후의 검증, 표준 에러 응답, 거래 헤더 생성/반환 동작만 빠르게 확인할 수 있습니다.</p>
 *
 * <p>금융권 API 기준으로 회원 ID를 {@code /mbr/{id}}처럼 경로에 묻지 않고
 * {@code /mbr/detail?memberId=...}처럼 명시 파라미터로 받는 정책도 함께 검증합니다.</p>
 */
@WebMvcTest(
        controllers = MbrController.class,
        properties = {
                "fps.cmn.cache.preload-enabled=false",
                "fps.cmn.cache.event-poll-enabled=false"
        }
)
@Import({GlobalExceptionHandler.class, FpsGlobalExceptionHandler.class, TransactionContextFilter.class, TransactionIdGenerator.class, SecurityHeaderFilter.class})
class MbrControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MbrService mbrService;

    /**
     * 회원 ID를 경로 변수로 노출하는 API가 열려 있지 않은지 확인합니다.
     *
     * <p>이 테스트가 통과하면 {@code GET /mbr/1}은 상세조회로 처리되지 않고 404로 응답합니다.
     * PFW 필터는 404 응답에도 거래ID 헤더를 내려주므로 장애 추적이 가능합니다.</p>
     */
    @Test
    void detailDoesNotExposeMemberIdAsPathVariable() throws Exception {
        mockMvc.perform(get("/mbr/1"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Transaction-Id", matchesPattern("\\d{17}MBRlocal01\\d{7}")));
    }

    /**
     * 회원 상세조회에서 0 이하의 회원 ID를 거부하는지 확인합니다.
     *
     * <p>요청에 유효한 {@code X-Transaction-Id}와 {@code X-Trace-Id}를 넣으면
     * 응답 본문과 응답 헤더에도 같은 값이 유지되어야 합니다.</p>
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
                .andExpect(jsonPath("$.statusCode").value("2004"))
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.traceId").value("TRACE-TEST-001"))
                .andExpect(header().string("X-Transaction-Id", transactionId))
                .andExpect(header().string("X-Trace-Id", "TRACE-TEST-001"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    /**
     * 회원 ID 타입이 숫자가 아닐 때 표준 파라미터 오류로 응답하는지 확인합니다.
     */
    @Test
    void detailRejectsWrongMemberIdType() throws Exception {
        mockMvc.perform(get("/mbr/detail")
                        .param("memberId", "abc")
                        .headers(requiredBusinessHeaders()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("2001"));
    }

    /**
     * PFW 필수 업무 헤더가 없으면 컨트롤러 업무 로직으로 진입하지 않는지 확인합니다.
     */
    @Test
    void detailRejectsMissingRequiredBusinessHeaders() throws Exception {
        mockMvc.perform(get("/mbr/detail")
                        .param("memberId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("9001"))
                .andExpect(jsonPath("$.messageCode").value("PFW.MISSING_TRANSACTION_HEADER"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("필수 거래 헤더")));
    }

    /**
     * 회원 등록 요청에서 필수 Body 값이 비어 있으면 Bean Validation 오류로 응답하는지 확인합니다.
     */
    @Test
    void createRejectsBlankMemberName() throws Exception {
        mockMvc.perform(post("/mbr/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(requiredBusinessHeaders())
                        .content("{\"memberName\":\"\",\"description\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value("2004"));
    }

    /**
     * 테스트 요청에 반복으로 필요한 PFW 업무 헤더를 생성합니다.
     *
     * <p>{@code X-Request-Type}, {@code X-Original-Channel-Code}, {@code X-Channel-Code}는
     * 컨트롤러 진입 전에 검증되는 필수 헤더입니다. 나머지 회원번호, IP, 예약 필드는
     * 로그 검색과 확장 필드 적재를 확인하기 위한 선택 헤더입니다.</p>
     *
     * @return MBR 컨트롤러 요청에 사용할 표준 업무 헤더입니다.
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
