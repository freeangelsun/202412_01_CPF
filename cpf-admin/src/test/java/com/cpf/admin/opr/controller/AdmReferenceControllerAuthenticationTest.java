package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.code.dto.CommonCodeRequest;
import com.cpf.common.management.CpfCommonManagementApi;
import com.cpf.common.message.api.CpfCommonCatalogManagementService;
import com.cpf.common.message.api.CpfResponseCodeRecord;
import com.cpf.common.message.dto.CommonResponseCodeRequest;
import com.cpf.common.parameter.dto.CommonConfigRequest;
import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdmReferenceControllerAuthenticationTest {
    @Test void codeCreateRejectsMissingOperatorBeforeReasonOrSideEffect() {
        CpfCommonManagementApi common=mock(CpfCommonManagementApi.class); AdmAuditLogService audit=mock(AdmAuditLogService.class);
        assertThatThrownBy(() -> new AdmCodeController(common,audit).createCode(codeRequest("browser-spoof"),new MockHttpServletRequest()))
                .isInstanceOf(CpfValidationException.class).hasMessageContaining("ADM 운영자 ID");
        verify(audit,never()).requireReason(any()); verifyNoInteractions(common);
    }

    @Test void codeCreateUsesTrustedSessionActorInsteadOfClaimedBodyActor() throws Exception {
        CpfCommonManagementApi common=mock(CpfCommonManagementApi.class); AdmAuditLogService audit=mock(AdmAuditLogService.class);
        CommonCodeRequest body=codeRequest("browser-spoof"); when(audit.requireReason("code create")).thenReturn("code create");
        when(common.create(any(),any(),eq("operator-a"))).thenReturn(Map.of("code_id",10L,"code_key","USER_STATUS"));
        try (AutoCloseable _=AdmControllerTestContexts.bind("operator-a")) {
            new AdmCodeController(common,audit).createCode(body,authenticated("operator-a"));
        }
        verify(common).create(any(),any(),eq("operator-a"));
        assertThat(body.getRequestUser()).isEqualTo("browser-spoof");
    }

    @Test void configCreateMasksEncryptedCanonicalProjection() throws Exception {
        CpfCommonManagementApi common=mock(CpfCommonManagementApi.class); AdmAuditLogService audit=mock(AdmAuditLogService.class);
        CommonConfigRequest body=configRequest("browser-spoof");body.setEncryptedYn("Y");when(audit.requireReason("config create")).thenReturn("config create");
        when(common.create(any(),any(),eq("operator-b"))).thenReturn(Map.of("config_id",20L,"encrypted_yn","Y","config_value","plain-secret"));
        try (AutoCloseable _=AdmControllerTestContexts.bind("operator-b")) {
            var response=new AdmConfigController(common,audit).createConfig(body,authenticated("operator-b"));
            assertThat(response.getBody()).containsEntry("config_value","[MASKED]");assertThat(String.valueOf(response.getBody())).doesNotContain("plain-secret");
        }
    }

    @Test void responseCodeCreateUsesTrustedSessionActor() throws Exception {
        CpfCommonCatalogManagementService common=mock(CpfCommonCatalogManagementService.class);AdmAuditLogService audit=mock(AdmAuditLogService.class);
        CommonResponseCodeRequest body=responseCodeRequest("browser-spoof");when(audit.requireReason("response create")).thenReturn("response create");
        when(common.createResponseCode(body,"operator-c","response create")).thenReturn(new CpfResponseCodeRecord(
                "EREF010001","MREF010001","E","REF","01","0001",409,"BUSINESS","NEVER","SAFE_MESSAGE_ONLY",null,null,1,"test","Y",Instant.now()));
        try (AutoCloseable _=AdmControllerTestContexts.bind("operator-c")) {
            var response=new AdmResponseCodeController(common,audit).create(body,"response create",authenticated("operator-c"));
            assertThat(response.getBody()).containsEntry("available",true);
        }
        verify(common).createResponseCode(body,"operator-c","response create");
        assertThat(body.getRequestUser()).isEqualTo("browser-spoof");
    }

    private CommonCodeRequest codeRequest(String claimed){CommonCodeRequest r=new CommonCodeRequest();r.setCodeKey("USER_STATUS");r.setCodeValue("ACTIVE");r.setReason("code create");r.setRequestUser(claimed);return r;}
    private CommonConfigRequest configRequest(String claimed){CommonConfigRequest r=new CommonConfigRequest();r.setConfigKey("CPF.TEST.SECRET");r.setConfigValue("plain-secret");r.setReason("config create");r.setRequestUser(claimed);return r;}
    private CommonResponseCodeRequest responseCodeRequest(String claimed){CommonResponseCodeRequest r=new CommonResponseCodeRequest();r.setResponseCode("EREF010001");r.setMessageCode("MREF010001");r.setResultType("E");r.setModuleId("REF");r.setResponseGroup("01");r.setSequenceNo("0001");r.setHttpStatus(409);r.setRequestUser(claimed);return r;}
    private MockHttpServletRequest authenticated(String id){MockHttpServletRequest r=new MockHttpServletRequest();r.setAttribute("adm.operatorId",id);r.setRemoteAddr("127.0.0.1");return r;}
}
