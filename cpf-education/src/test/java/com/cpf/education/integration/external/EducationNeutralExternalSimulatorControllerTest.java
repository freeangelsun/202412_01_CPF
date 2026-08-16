package com.cpf.education.integration.external;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class EducationNeutralExternalSimulatorControllerTest {
    private final EducationNeutralExternalSimulatorController controller = new EducationNeutralExternalSimulatorController();

    @Test
    void 정상과오류HTTP상태를결정적으로재현한다() {
        assertThat(controller.response(200, 0, "OK-1").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.response(503, 0, "FAIL-1").getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void 허용범위밖상태코드는400으로차단한다() {
        assertThat(controller.response(999, 0, "INVALID").getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
