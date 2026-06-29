package cpf.pfw.common.header;

import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfHeaderMutatorTest {
    @AfterEach
    void tearDown() {
        TransactionContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void businessHeadersCanBeMutatedInCurrentContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        TransactionContext.initialize(
                "20260615120000000MBRlocal010000001",
                "TRACE-1",
                null,
                "20260615120000000MBRlocal010000001",
                TransactionHeader.builder().channelCode("MBR").build());

        TransactionHeader updated = CpfHeaderMutator.put(CpfHeaderNames.CHANNEL_DETAIL_CODE, "MOBILE");

        assertThat(updated.getChannelDetailCode()).isEqualTo("MOBILE");
        assertThat(TransactionContext.channelDetailCode()).isEqualTo("MOBILE");
    }

    @Test
    void systemAndSensitiveHeadersCannotBeMutatedByBusinessCode() {
        assertThatThrownBy(() -> CpfHeaderMutator.withAllowedHeader(
                TransactionHeader.builder().build(),
                CpfHeaderNames.TRANSACTION_ID,
                "20260615120000000MBRlocal010000001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경할 수 없습니다");

        assertThatThrownBy(() -> CpfHeaderMutator.withAllowedHeader(
                TransactionHeader.builder().build(),
                CpfHeaderNames.AUTHORIZATION,
                "Bearer token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경할 수 없습니다");
    }
}
