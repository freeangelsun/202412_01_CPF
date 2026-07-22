package cpf.pfw.common.base;

import cpf.pfw.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** PFW canonical Base가 주제영역에 제공하는 최소 계약을 검증합니다. */
class CpfBaseContractTest {

    private final TestController controller = new TestController();
    private final TestService service = new TestService();

    @Test
    void controller는표준성공응답을생성한다() {
        ResponseEntity<String> response = controller.success("ok");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("ok");
    }

    @Test
    void service는필수문자열을정규화하고빈값을차단한다() {
        assertThat(service.normalize("  member  ")).isEqualTo("member");
        assertThatThrownBy(() -> service.normalize("  "))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("memberId");
        assertThatThrownBy(() -> service.normalize(null))
                .isInstanceOf(CpfValidationException.class);
    }

    private static final class TestController extends CpfBaseController {
        private ResponseEntity<String> success(String body) {
            return ok(body);
        }
    }

    private static final class TestService extends CpfBaseService {
        private String normalize(String value) {
            return requireText(value, "memberId");
        }
    }
}
