package cpf.pfw.common.execution;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfStandardExecutionIdTest {

    @Test
    void 온라인표준실행ID를구간별로해석한다() {
        CpfStandardExecutionId id = CpfStandardExecutionId.parse("OBZA-USR-QY-0001");

        assertThat(id.type()).isEqualTo(CpfExecutionType.ONLINE);
        assertThat(id.domain()).isEqualTo("BZA");
        assertThat(id.business()).isEqualTo("USR");
        assertThat(id.sub()).isEqualTo("QY");
        assertThat(id.sequence()).isEqualTo(1);
        assertThat(id.value()).isEqualTo("OBZA-USR-QY-0001");
    }

    @Test
    void 유형과구간과일련번호오류를차단한다() {
        assertThatThrownBy(() -> CpfStandardExecutionId.parse("XBZA-USR-QY-0001"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CpfStandardExecutionId.parse("OBZA-USR-QY-0000"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
