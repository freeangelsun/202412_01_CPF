package cpf.pfw.gateway.controller;

import cpf.pfw.common.header.CpfHeaderNames;
import cpf.pfw.gateway.service.PfwGatewayProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PfwGatewayControllerTest {

    @Test
    void URI와Header실행ID불일치를대상호출전에차단한다() {
        PfwGatewayController controller = new PfwGatewayController(mock(PfwGatewayProxyService.class));
        HttpHeaders headers = new HttpHeaders();
        headers.set(CpfHeaderNames.STANDARD_EXECUTION_ID, "OACCAC0001");

        assertThatThrownBy(() -> controller.executeByPath("OACCAC0002", headers, true, new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
