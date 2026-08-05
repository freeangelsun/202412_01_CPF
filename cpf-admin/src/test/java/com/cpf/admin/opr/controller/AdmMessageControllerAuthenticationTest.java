package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.msg.dto.CommonMessageRequest;
import com.cpf.common.msg.service.MessageCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmMessageControllerAuthenticationTest {

    @Test
    void rejectsMissingOperatorBeforeCreateSideEffects() {
        MessageCacheService messageService = mock(MessageCacheService.class);
        AdmAuditLogService auditService = mock(AdmAuditLogService.class);
        AdmMessageController controller = new AdmMessageController(messageService, auditService);
        CommonMessageRequest request = request("browser-spoof");

        assertThatThrownBy(() -> controller.createMessage(request, new MockHttpServletRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("operator session");

        verify(messageService, never()).createMessage(any());
        verify(auditService, never()).requireReason(any());
    }

    @Test
    void usesAuthenticatedOperatorInsteadOfRequestBodyIdentity() {
        MessageCacheService messageService = mock(MessageCacheService.class);
        AdmAuditLogService auditService = mock(AdmAuditLogService.class);
        AdmMessageController controller = new AdmMessageController(messageService, auditService);
        CommonMessageRequest request = request("browser-spoof");
        MockHttpServletRequest servletRequest = authenticated("operator-a");
        when(auditService.requireReason("message create")).thenReturn("message create");
        when(messageService.createMessage(request)).thenReturn(Map.of("messageId", 10L));

        controller.createMessage(request, servletRequest);

        verify(auditService).record(
                any(), eq("operator-a"), eq("MESSAGE_CREATE"), eq("cpf_message"), eq("10"),
                eq("message create"), eq(null), any(), any(), eq("127.0.0.1"));
    }

    @Test
    void deleteRequiresSessionOperatorAndHasNoClaimedOperatorParameter() {
        MessageCacheService messageService = mock(MessageCacheService.class);
        AdmAuditLogService auditService = mock(AdmAuditLogService.class);
        AdmMessageController controller = new AdmMessageController(messageService, auditService);
        MockHttpServletRequest servletRequest = authenticated("operator-a");
        when(auditService.requireReason("disable message")).thenReturn("disable message");
        when(messageService.getMessageById(10L)).thenReturn(Map.of("messageId", 10L));
        when(messageService.deleteMessage(10L)).thenReturn(List.of());

        assertThat(controller.deleteMessage(10L, "disable message", servletRequest).getBody()).isEmpty();

        verify(auditService).record(
                any(), eq("operator-a"), eq("MESSAGE_DISABLE"), eq("cpf_message"), eq("10"),
                eq("disable message"), any(), eq(null), eq("메시지 비활성"), eq("127.0.0.1"));
    }

    private CommonMessageRequest request(String claimedUser) {
        CommonMessageRequest request = new CommonMessageRequest();
        request.setMessageCode("MCPF900001");
        request.setLocale("ko-KR");
        request.setExternalMessage("message");
        request.setReason("message create");
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
