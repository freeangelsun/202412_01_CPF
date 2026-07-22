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
    void businessIdentityHeadersCanBeMutatedButSecurityPrincipalsCannot() {
        TransactionHeader source = TransactionHeader.builder()
                .userId("login-user")
                .operatorId("adm01")
                .build();

        TransactionHeader updated = CpfHeaderMutator.withAllowedHeader(source, CpfHeaderNames.CUSTOMER_NO, "CUST-001");
        updated = CpfHeaderMutator.withAllowedHeader(updated, CpfHeaderNames.MEMBER_NO, "MBR-001");
        updated = CpfHeaderMutator.withAllowedHeader(updated, CpfHeaderNames.TENANT_ID, "TENANT-A");
        updated = CpfHeaderMutator.withAllowedHeader(updated, CpfHeaderNames.ORGANIZATION_CODE, "ORG-A");
        updated = CpfHeaderMutator.withAllowedHeader(updated, CpfHeaderNames.BRANCH_CODE, "BR-001");

        assertThat(updated.getCustomerNo()).isEqualTo("CUST-001");
        assertThat(updated.getMemberNo()).isEqualTo("MBR-001");
        assertThat(updated.getTenantId()).isEqualTo("TENANT-A");
        assertThat(updated.getOrganizationCode()).isEqualTo("ORG-A");
        assertThat(updated.getBranchCode()).isEqualTo("BR-001");
        assertThat(updated.getUserId()).isEqualTo("login-user");
        assertThat(updated.getOperatorId()).isEqualTo("adm01");

        assertThatThrownBy(() -> CpfHeaderMutator.withAllowedHeader(source, CpfHeaderNames.USER_ID, "other-user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경할 수 없습니다");
        assertThatThrownBy(() -> CpfHeaderMutator.withAllowedHeader(source, CpfHeaderNames.OPERATOR_ID, "other-operator"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경할 수 없습니다");
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

    @Test
    void restrictedHeadersAreMatchedCaseInsensitively() {
        TransactionHeader source = TransactionHeader.builder()
                .userId("login-user")
                .operatorId("adm01")
                .build();

        assertThatThrownBy(() -> CpfHeaderMutator.withAllowedHeader(source, "x-user-id", "other-user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경할 수 없습니다");
        assertThatThrownBy(() -> CpfHeaderMutator.withAllowedHeader(source, "x-operator-id", "other-operator"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경할 수 없습니다");
    }

    @Test
    void failedMutationDoesNotReplaceCurrentTransactionContextHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        TransactionHeader original = TransactionHeader.builder()
                .channelCode("MBR")
                .userId("login-user")
                .operatorId("adm01")
                .build();
        TransactionContext.initialize(
                "20260615120000000MBRlocal010000001",
                "TRACE-1",
                null,
                "20260615120000000MBRlocal010000001",
                original);

        assertThatThrownBy(() -> CpfHeaderMutator.put(CpfHeaderNames.USER_ID, "other-user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경할 수 없습니다");

        assertThat(TransactionContext.currentHeader()).isSameAs(original);
        assertThat(TransactionContext.currentHeader().getUserId()).isEqualTo("login-user");
        assertThat(TransactionContext.currentHeader().getOperatorId()).isEqualTo("adm01");
    }
}
