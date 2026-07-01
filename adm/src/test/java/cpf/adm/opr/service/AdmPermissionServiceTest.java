package cpf.adm.opr.service;

import cpf.adm.opr.dto.AdmApiPermissionSaveRequest;
import cpf.pfw.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmPermissionServiceTest {

    private final JdbcTemplate admJdbcTemplate = mock(JdbcTemplate.class);
    private final AdmPermissionService service = new AdmPermissionService(admJdbcTemplate);

    @Test
    void createApiPermissionRejectsUnsupportedMethodBeforeDbUpdate() {
        // API 권한은 실제 HTTP 메서드와 연결되므로 등록 전에 메서드 값을 먼저 검증해야 합니다.
        AdmApiPermissionSaveRequest request = new AdmApiPermissionSaveRequest(
                "API_TEST",
                "PERMISSION",
                "TRACE",
                "/adm/api/test",
                "테스트 API",
                "READ",
                "PERMISSION",
                "PERMISSION_READ",
                "Y",
                "tester",
                "API 권한 등록 테스트");

        assertThatThrownBy(() -> service.createApiPermission(request))
                .isInstanceOf(CpfValidationException.class);

        verifyNoInteractions(admJdbcTemplate);
    }

    @Test
    void updateRoleApiPermissionStoresNormalizedAllowYnAndRequester() {
        // 역할별 API 권한 변경은 Y/N 표준값과 요청자를 DB에 남겨야 감사 추적이 가능합니다.
        when(admJdbcTemplate.queryForMap(anyString(), eq("ADM_VIEWER"), eq("API_LOG_READ")))
                .thenReturn(Map.of("ROLE_ID", "ADM_VIEWER", "API_PERMISSION_ID", "API_LOG_READ", "ALLOW_YN", "Y"));

        Map<String, Object> result = service.updateRoleApiPermission(
                "ADM_VIEWER",
                "API_LOG_READ",
                "Y",
                "tester");

        assertThat(result).containsEntry("ALLOW_YN", "Y");
        verify(admJdbcTemplate).update(
                contains("adm_role_api_permission"),
                eq("ADM_VIEWER"),
                eq("API_LOG_READ"),
                eq("Y"),
                eq("tester"),
                eq("tester"));
    }

    @Test
    void findApiPermissionMatrixReturnsEmptyWhenDbIsUnavailable() {
        // 로컬 초반에 ADM DB가 아직 준비되지 않아도 화면 조회 API는 실패 대신 빈 목록으로 내려가야 합니다.
        when(admJdbcTemplate.queryForList(contains("adm_api_permission")))
                .thenThrow(new DataAccessResourceFailureException("down"));

        assertThat(service.findApiPermissionMatrix()).isEqualTo(List.of());
    }
}
