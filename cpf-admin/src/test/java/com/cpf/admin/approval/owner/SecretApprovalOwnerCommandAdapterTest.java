package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.*;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.security.api.secret.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SecretApprovalOwnerCommandAdapterTest {
    private static final String HASH="a".repeat(64);
    private final CpfRotatableSecretProvider provider=mock(CpfRotatableSecretProvider.class);
    private final AdmAuditLogService audit=mock(AdmAuditLogService.class);
    private final SecretApprovalOwnerCommandAdapter adapter=new SecretApprovalOwnerCommandAdapter(List.of(provider),audit,new ObjectMapper());
    @Test void selfApprovalFailsBeforeProvider(){var r=adapter.execute(command("same","same"));assertEquals(AdmApprovalExecutionStatus.FAILED,r.status());verifyNoInteractions(provider,audit);}
    @Test void versionConflictBlocksRotation(){when(provider.providerId()).thenReturn("vault");when(provider.metadata(any())).thenReturn(meta("v2"));var r=adapter.execute(command("req","app"));assertEquals("SECRET_VERSION_CONFLICT",r.resultCode());verify(provider,never()).rotate(any(),anyString(),anyString());}
    @SuppressWarnings("unchecked") @Test void approvedRotationUsesAuditBoundary(){when(provider.providerId()).thenReturn("vault");when(provider.metadata(any())).thenReturn(meta("v1"));when(audit.executeAudited(anyString(),eq("app"),eq("SECRET_ROTATE"),eq("secret_reference"),eq("vault:key"),eq("approved reason"),isNull(),isNull(),any(Supplier.class),any(Function.class))).thenAnswer(inv->((Supplier<CpfSecretMetadata>)inv.getArgument(8)).get());when(provider.rotate(any(),eq("approved reason"),eq("app"))).thenReturn(meta("v2"));var r=adapter.execute(command("req","app"));assertEquals(AdmApprovalExecutionStatus.SUCCEEDED,r.status());verify(provider).rotate(any(),eq("approved reason"),eq("app"));}
    @Test void reconcileDoesNotRotate(){when(provider.providerId()).thenReturn("vault");when(provider.metadata(any())).thenReturn(meta("v2"));var r=adapter.reconcile(command("req","app"));assertEquals(AdmApprovalExecutionStatus.SUCCEEDED,r.status());verify(provider,never()).rotate(any(),anyString(),anyString());}
    private static AdmApprovedOperationCommand command(String req,String app){return new AdmApprovedOperationCommand(101,"cmd","SECRET_ROTATE","CPF-SECURITY","SECRET_ROTATE","SECRET_REFERENCE","vault:key",HASH,"{\"provider\":\"vault\",\"key\":\"key\",\"expectedVersion\":\"v1\"}",req,app,"approved reason","20260815000000000ADM00000010000001","lease",1);}
    private static CpfSecretMetadata meta(String version){return new CpfSecretMetadata(new CpfSecretReference("vault","key"),version,Instant.now(),null,true,Map.of());}
}
