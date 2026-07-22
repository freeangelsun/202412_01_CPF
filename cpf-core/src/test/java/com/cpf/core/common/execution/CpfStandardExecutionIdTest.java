package cpf.pfw.common.execution;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfStandardExecutionIdTest {

    @Test
    void 온라인표준실행ID를구간별로해석한다() {
        CpfStandardExecutionId id = CpfStandardExecutionId.parse("OBZAUS0002");

        assertThat(id.type()).isEqualTo(CpfExecutionType.ONLINE);
        assertThat(id.domain()).isEqualTo("BZA");
        assertThat(id.feature()).isEqualTo("US");
        assertThat(id.sequence()).isEqualTo(2);
        assertThat(id.value()).isEqualTo("OBZAUS0002");
    }

    @Test
    void 유형과구간과일련번호오류를차단한다() {
        assertThatThrownBy(() -> CpfStandardExecutionId.parse("XBZAUS0001"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CpfStandardExecutionId.parse("OBZA-USR-QY-0001"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CpfStandardExecutionId.parse("OBZAUS0000"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 내부공유API유형과구형Alias형식을구분한다() {
        CpfStandardExecutionId shared = CpfStandardExecutionId.parse("SMBRMB0001");

        assertThat(shared.type()).isEqualTo(CpfExecutionType.SHARED);
        assertThat(CpfStandardExecutionId.isLegacy("OMBR-BSE-01-0001")).isTrue();
        assertThat(CpfStandardExecutionId.isValid("OMBR-BSE-01-0001")).isFalse();
    }

    @Test
    void 배치실행유형을표준형식으로해석한다() {
        CpfStandardExecutionId batch = CpfStandardExecutionId.parse("BBATOD0001");

        assertThat(batch.type()).isEqualTo(CpfExecutionType.BATCH);
        assertThat(batch.domain()).isEqualTo("BAT");
        assertThat(batch.feature()).isEqualTo("OD");
        assertThat(batch.sequence()).isEqualTo(1);
    }
}
