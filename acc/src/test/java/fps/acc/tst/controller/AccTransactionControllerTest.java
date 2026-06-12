package fps.acc.tst.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ACC 거래 로그 샘플 컨트롤러의 MVC 테스트입니다.
 *
 * <p>현재 ACC 테스트 태스크는 Gradle 설정에서 비활성화되어 있지만,
 * 이 테스트 코드는 개발자가 로컬에서 ACC 로그 샘플을 검증할 때 기준으로 사용할 수 있도록 유지합니다.</p>
 *
 * <p>요청에는 PFW 필수 업무 헤더를 함께 넣습니다.
 * 실제 운영 요청도 이 헤더들이 있어야 컨트롤러 진입 단계에서 표준 검증을 통과합니다.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * AOP 및 이벤트 기반 성공 트랜잭션 로그를 검증합니다.
     *
     * <p>이 요청이 성공하면 PFW 로그에는 {@code LOG_TYPE=SUCCESS},
     * {@code WORKFLOW_STATUS=COMPLETED}가 남는 것이 의도입니다.</p>
     */
    @Test
    void testSuccessfulTransaction() throws Exception {
        mockMvc.perform(get("/acc/tran/success")
                        // menuId는 샘플 화면/메뉴 식별값이며 로그의 MENU_ID 결정에 사용될 수 있습니다.
                        .param("menuId", "MENU123")
                        // execUser는 거래 수행자를 표현하는 샘플 값입니다.
                        .param("execUser", "testUser")
                        // PFW 필수 업무 헤더를 추가해 실제 컨트롤러 진입 조건과 맞춥니다.
                        .headers(requiredBusinessHeaders())
                        // GET 요청이지만 테스트 요청의 컨텐츠 타입을 명시해 MockMvc 요청을 일관되게 만듭니다.
                        .contentType(MediaType.APPLICATION_JSON))
                // 정상 샘플 거래는 HTTP 200을 반환해야 합니다.
                .andExpect(status().isOk());

    }

    /**
     * 보상 거래 샘플이 정상 종료되는지 검증합니다.
     *
     * <p>이 요청이 성공하면 PFW 로그에는 {@code COMPENSATION_YN=Y},
     * {@code COMPENSATION_STATUS=COMPENSATED}가 남는 것이 의도입니다.</p>
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
     * 테스트 요청에 반복으로 사용하는 PFW 필수 업무 헤더를 생성합니다.
     *
     * @return 요청 구분, 최초 채널, 현재 채널이 포함된 HTTP 헤더입니다.
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
