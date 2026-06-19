package cpf.bizadm.operation.service;

import cpf.bizadm.operation.repository.BizAdmOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BizAdmOperationServiceTest {

    private final BizAdmOperationRepository repository = mock(BizAdmOperationRepository.class);
    private final BizAdmOperationService service = new BizAdmOperationService(repository);

    @Test
    void findCustomersMasksSensitiveFields() {
        // 고객 목록 조회는 운영 화면 기본 조회이므로 이메일과 휴대폰을 기본 마스킹 상태로 반환해야 합니다.
        when(repository.findCustomers()).thenReturn(List.of(customer("CUST-001", "customer01@example.com", "01012345678")));

        List<Map<String, Object>> rows = service.findCustomers();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("customerNo")).isEqualTo("CUST-001");
        assertThat(rows.get(0).get("email")).isEqualTo("cu****@example.com");
        assertThat(rows.get(0).get("mobileNo")).isEqualTo("010-****-5678");
    }

    @Test
    void unmaskCustomersRequiresReasonBeforeAuditWrite() {
        // 원문 조회는 감사 사유가 없으면 저장소 접근과 감사 로그 적재를 모두 막아야 합니다.
        assertThatThrownBy(() -> service.unmaskCustomers(" ", "biz-operator"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");

        verify(repository, never()).findCustomers();
        verify(repository, never()).insertMaskingAudit(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void unmaskCustomersWritesAuditAndReturnsReasonMetadata() {
        // 사유가 있는 원문 조회는 고객별 감사 로그를 남기고 응답에도 요청자와 사유를 붙입니다.
        when(repository.findCustomers()).thenReturn(List.of(customer("CUST-002", "customer02@example.com", "01087654321")));

        List<Map<String, Object>> rows = service.unmaskCustomers("민원 원문 확인", "biz-operator");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("email")).isEqualTo("customer02@example.com");
        assertThat(rows.get(0).get("unmaskAuditReason")).isEqualTo("민원 원문 확인");
        assertThat(rows.get(0).get("unmaskRequestUser")).isEqualTo("biz-operator");
        verify(repository).insertMaskingAudit("CUST-002", "biz-operator", "민원 원문 확인", "SUCCESS");
    }

    private Map<String, Object> customer(String customerNo, String email, String mobileNo) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("customerNo", customerNo);
        row.put("customerName", "홍길동");
        row.put("email", email);
        row.put("mobileNo", mobileNo);
        row.put("customerStatus", "ACTIVE");
        return row;
    }
}
