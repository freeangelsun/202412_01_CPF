package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.message.api.CpfCommonCatalogManagementService;
import com.cpf.common.message.api.CpfMessageRecord;
import com.cpf.common.message.dto.CommonMessageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdmMessageControllerAuthenticationTest {
    @Test void rejectsMissingOperatorBeforeCreateSideEffects(){CpfCommonCatalogManagementService common=mock(CpfCommonCatalogManagementService.class);AdmAuditLogService audit=mock(AdmAuditLogService.class);assertThatThrownBy(()->new AdmMessageController(common,audit).createMessage(request("spoof"),new MockHttpServletRequest())).isInstanceOf(ResponseStatusException.class);verifyNoInteractions(common);verify(audit,never()).requireReason(any());}
    @Test void usesAuthenticatedOperatorInsteadOfRequestBodyIdentity(){CpfCommonCatalogManagementService common=mock(CpfCommonCatalogManagementService.class);AdmAuditLogService audit=mock(AdmAuditLogService.class);CommonMessageRequest body=request("spoof");when(audit.requireReason("message create")).thenReturn("message create");when(common.createMessage(body,"operator-a","message create")).thenReturn(record(10,1,"Y"));new AdmMessageController(common,audit).createMessage(body,authenticated("operator-a"));verify(common).createMessage(body,"operator-a","message create");assertThat(body.getRequestUser()).isEqualTo("spoof");}
    @Test void deleteUsesCurrentCatalogVersionAndSessionOperator(){CpfCommonCatalogManagementService common=mock(CpfCommonCatalogManagementService.class);AdmAuditLogService audit=mock(AdmAuditLogService.class);when(audit.requireReason("disable message")).thenReturn("disable message");when(common.getMessage(10L)).thenReturn(record(10,7,"Y"));when(common.searchMessages(null,null,null,0,500)).thenReturn(new com.cpf.common.message.api.CpfCatalogPage<>(List.of(),0,500,0));var response=new AdmMessageController(common,audit).deleteMessage(10L,"disable message",authenticated("operator-a"));assertThat(response.getBody()).isEmpty();verify(common).deleteMessage(10L,7L,"operator-a","disable message");}
    private CpfMessageRecord record(long id,long version,String use){return new CpfMessageRecord(id,"MCPF900001","ko-KR","FIXED","message","internal",0,null,null,"Y","Y",null,null,version,"test",use,Instant.now());}
    private CommonMessageRequest request(String claimed){CommonMessageRequest r=new CommonMessageRequest();r.setMessageCode("MCPF900001");r.setLocale("ko-KR");r.setExternalMessage("message");r.setInternalMessage("internal");r.setReason("message create");r.setRequestUser(claimed);return r;}
    private MockHttpServletRequest authenticated(String id){MockHttpServletRequest r=new MockHttpServletRequest();r.setAttribute("adm.operatorId",id);r.setRemoteAddr("127.0.0.1");return r;}
}
