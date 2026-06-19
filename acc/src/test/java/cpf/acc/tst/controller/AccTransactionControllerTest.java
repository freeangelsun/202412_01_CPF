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
 * ACC 샘플 거래가 CPF 표준 거래 헤더와 로깅 AOP를 통과하는지 확인합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 정상 거래 요청이 성공 응답으로 끝나는지 확인합니다.
     */
    @Test
    void testSuccessfulTransaction() throws Exception {
        mockMvc.perform(get("/acc/tran/success")
                        .param("menuId", "MENU123")
                        .param("execUser", "testUser")
                        .headers(requiredBusinessHeaders())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    /**
     * 보상 거래 샘플도 동일한 표준 헤더를 사용해 성공하는지 확인합니다.
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
     * 인터셉터가 요구하는 공통 거래 헤더를 구성합니다.
     */
    private org.springframework.http.HttpHeaders requiredBusinessHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Transaction-Id", "20260611141234567ACCaccAP010000001");
        headers.add("X-Request-Type", "INQUIRY");
        headers.add("X-Original-Channel-Code", "TST");
        headers.add("X-Channel-Code", "ACC");
        headers.add("X-User-Id", "testUser");
        return headers;
    }
}
