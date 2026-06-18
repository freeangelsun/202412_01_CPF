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
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @Test
    void testSuccessfulTransaction() throws Exception {
        mockMvc.perform(get("/acc/tran/success")
                        // CPF 기능 설명입니다.
                        .param("menuId", "MENU123")
                        // CPF 기능 설명입니다.
                        .param("execUser", "testUser")
                        // CPF 기능 설명입니다.
                        .headers(requiredBusinessHeaders())
                        // CPF 기능 설명입니다.
                        .contentType(MediaType.APPLICATION_JSON))
                // CPF 기능 설명입니다.
                .andExpect(status().isOk());

    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
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
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
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

