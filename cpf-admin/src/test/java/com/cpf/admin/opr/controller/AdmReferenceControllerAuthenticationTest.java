package com.cpf.admin.opr.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.code.dto.CommonCodeRequest;
import com.cpf.common.code.service.CodeCacheService;
import com.cpf.common.parameter.dto.CommonConfigRequest;
import com.cpf.common.parameter.service.ConfigCacheService;
import com.cpf.common.message.dto.CommonResponseCodeRequest;
import com.cpf.common.message.service.ResponseCodeCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

class AdmReferenceControllerAuthenticationTest {

    @Test
    void codeCreateRejectsMissingOperatorBeforeReasonOrSideEffect() {
        CodeCacheService service = mock(CodeCacheService.class);
        AdmAuditLogService audit = mock(AdmAuditLogService.class);
        CommonCodeRequest body = codeRequest("browser-spoof");

        assertThatThrownBy(() -> new AdmCodeController(service, audit).createCode(body, new MockHttpServletRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("operator session");

        verify(audit, never()).requireReason(any());
        verify(service, never()).createCode(any());
    }

    @Test
    void codeCreateOverwritesClaimedActorBeforeRefreshEventAndAudit() {
        CodeCacheService service = mock(CodeCacheService.class);
        AdmAuditLogService audit = mock(AdmAuditLogService.class);
        CommonCodeRequest body = codeRequest("browser-spoof");
        when(audit.requireReason("code create")).thenReturn("code create");
        when(service.createCode(body)).thenReturn(Map.of("codeId", 10L));

        new AdmCodeController(service, audit).createCode(body, authenticated("operator-a"));

        assertThat(body.getRequestUser()).isEqualTo("operator-a");
        verify(audit).record(any(), eq("operator-a"), eq("CODE_CREATE"), eq("CMN_CODE"), eq("10"),
                eq("code create"), eq(null), any(), any(), eq("127.0.0.1"));
    }

    @Test
    void configCreateRejectsMissingOperatorBeforeReasonOrSideEffect() {
        ConfigCacheService service = mock(ConfigCacheService.class);
        AdmAuditLogService audit = mock(AdmAuditLogService.class);
        CommonConfigRequest body = configRequest("browser-spoof");

        assertThatThrownBy(() -> new AdmConfigController(service, audit).createConfig(body, new MockHttpServletRequest()))
                .isInstanceOf(ResponseStatusException.class);

        verify(audit, never()).requireReason(any());
        verify(service, never()).createConfig(any());
    }

    @Test
    void configCreateOverwritesClaimedActorAndMasksEncryptedResult() {
        ConfigCacheService service = mock(ConfigCacheService.class);
        AdmAuditLogService audit = mock(AdmAuditLogService.class);
        CommonConfigRequest body = configRequest("browser-spoof");
        body.setEncryptedYn("Y");
        when(audit.requireReason("config create")).thenReturn("config create");
        when(service.createConfig(body)).thenReturn(Map.of(
                "configId", 20L, "encryptedYn", "Y", "configValue", "plain-secret"));

        var response = new AdmConfigController(service, audit).createConfig(body, authenticated("operator-b"));

        assertThat(body.getRequestUser()).isEqualTo("operator-b");
        assertThat(response.getBody()).containsEntry("configValue", "********");
        assertThat(String.valueOf(response.getBody())).doesNotContain("plain-secret");
    }

    @Test
    void responseCodeCreateRejectsMissingOperatorBeforeReasonOrSideEffect() {
        ResponseCodeCacheService service = mock(ResponseCodeCacheService.class);
        AdmAuditLogService audit = mock(AdmAuditLogService.class);
        CommonResponseCodeRequest body = responseCodeRequest("browser-spoof");

        assertThatThrownBy(() -> new AdmResponseCodeController(service, audit)
                .create(body, "response create", new MockHttpServletRequest()))
                .isInstanceOf(ResponseStatusException.class);

        verify(audit, never()).requireReason(any());
        verify(service, never()).createResponseCode(any());
    }

    @Test
    void responseCodeCreateOverwritesClaimedActorAndMasksDatabaseFailure() {
        ResponseCodeCacheService service = mock(ResponseCodeCacheService.class);
        AdmAuditLogService audit = mock(AdmAuditLogService.class);
        CommonResponseCodeRequest body = responseCodeRequest("browser-spoof");
        when(audit.requireReason("response create")).thenReturn("response create");
        when(service.createResponseCode(body)).thenThrow(new DataAccessResourceFailureException("jdbc:oracle:thin:@secret-host"));

        var response = new AdmResponseCodeController(service, audit)
                .create(body, "response create", authenticated("operator-c"));

        assertThat(body.getRequestUser()).isEqualTo("operator-c");
        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(String.valueOf(response.getBody())).doesNotContain("secret-host");
    }

    private CommonCodeRequest codeRequest(String claimedUser) {
        CommonCodeRequest request = new CommonCodeRequest();
        request.setCodeKey("USER_STATUS");
        request.setCodeValue("ACTIVE");
        request.setReason("code create");
        request.setRequestUser(claimedUser);
        return request;
    }

    private CommonConfigRequest configRequest(String claimedUser) {
        CommonConfigRequest request = new CommonConfigRequest();
        request.setConfigKey("CPF.TEST.SECRET");
        request.setConfigValue("plain-secret");
        request.setReason("config create");
        request.setRequestUser(claimedUser);
        return request;
    }

    private CommonResponseCodeRequest responseCodeRequest(String claimedUser) {
        CommonResponseCodeRequest request = new CommonResponseCodeRequest();
        request.setResponseCode("EREF010001");
        request.setMessageCode("MREF010001");
        request.setResultType("E");
        request.setModuleId("REF");
        request.setResponseGroup("01");
        request.setSequenceNo("0001");
        request.setHttpStatus(400);
        request.setRequestUser(claimedUser);
        return request;
    }

    private MockHttpServletRequest authenticated(String operatorId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("adm.operatorId", operatorId);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
