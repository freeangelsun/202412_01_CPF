package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeGroupCommand;
import com.cpf.core.api.runtimecontrol.CpfRuntimeGroupMemberCommand;
import com.cpf.core.api.runtimecontrol.CpfRuntimeGroupResult;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CpfRuntimeControlPlaneGroupContractTest {
    @Test void completedGroupOperationReplaysStoredResultNotLaterCurrentState(){
        CpfRuntimeControlPlaneRepository repository=mock(CpfRuntimeControlPlaneRepository.class);
        CpfRuntimeControlPlaneService service=new CpfRuntimeControlPlaneService(repository);
        CpfRuntimeGroupResult original=new CpfRuntimeGroupResult(
                "group-1","before",null,null,null,true,1L,List.of("instance-1"));
        when(repository.findOperation("operation-1")).thenReturn(Optional.of(Map.of(
                "operation_id","operation-1","command_type","RUNTIME_GROUP_MEMBER","request_hash",CpfRuntimeCanonicalHash.sha256(Map.of(
                        "groupId","group-1","instanceId","instance-1","active",true,"requestedBy","operator","reason","reason")),
                "result_state","SUCCESS","entity_id","group-1","result_json","stored")));
        when(repository.readJson("stored",CpfRuntimeGroupResult.class)).thenReturn(original);

        CpfRuntimeGroupResult replay=service.changeGroupMember(new CpfRuntimeGroupMemberCommand(
                "operation-1","group-1","instance-1",true,"reason","operator"));

        assertEquals(original,replay);
        verify(repository,never()).changeGroupMember(anyString(),anyString(),any(Boolean.class),anyString());
    }

    @Test void memberMutationBumpsGroupVersionForSubsequentCas(){
        JdbcTemplate jdbc=mock(JdbcTemplate.class);
        CpfRuntimeControlPlaneRepository repository=new CpfRuntimeControlPlaneRepository(
                jdbc,new com.fasterxml.jackson.databind.ObjectMapper());
        when(jdbc.queryForObject(org.mockito.ArgumentMatchers.contains("COUNT(*) FROM cpf_runtime_version"),
                eq(Integer.class),eq("GROUP_CATALOG"))).thenReturn(1);
        when(jdbc.queryForList(org.mockito.ArgumentMatchers.contains("version_key='GROUP_CATALOG'")))
                .thenReturn(List.of(Map.of("version_no",0L)));
        when(jdbc.queryForList(anyString(),eq("group-1"))).thenReturn(
                List.of(Map.of("group_id","group-1")),
                List.of(Map.of("group_id","group-1","group_name","name","active_yn","Y","row_version",2L)));
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("instance-1"))).thenReturn(1);
        when(jdbc.update(anyString(),any(),any(),any(),any())).thenReturn(1);
        when(jdbc.update(anyString(),any(),any())).thenReturn(1);
        when(jdbc.queryForList(anyString(),eq(String.class),eq("group-1"))).thenReturn(List.of("instance-1"));

        repository.changeGroupMember("group-1","instance-1",true,"operator");

        verify(jdbc).update(org.mockito.ArgumentMatchers.contains("row_version=row_version+1"),
                eq("operator"),eq("group-1"));
    }

    @Test void groupDbLengthOverflowFailsBeforeMutation(){
        CpfRuntimeControlPlaneRepository repository=mock(CpfRuntimeControlPlaneRepository.class);
        CpfRuntimeControlPlaneService service=new CpfRuntimeControlPlaneService(repository);
        assertThrows(IllegalArgumentException.class,()->service.saveGroup(new CpfRuntimeGroupCommand(
                "op","x".repeat(81),"name",null,null,null,0L,true,"reason","operator")));
        verifyNoInteractions(repository);
    }
    @Test void emptyCatalogUsesPersistentSentinelBeforeTopologyMutation(){
        JdbcTemplate jdbc=mock(JdbcTemplate.class);
        CpfRuntimeControlPlaneRepository repository=new CpfRuntimeControlPlaneRepository(
                jdbc,new com.fasterxml.jackson.databind.ObjectMapper());
        when(jdbc.queryForObject(org.mockito.ArgumentMatchers.contains("COUNT(*) FROM cpf_runtime_version"),
                eq(Integer.class),eq("GROUP_CATALOG"))).thenReturn(1);
        when(jdbc.queryForList(org.mockito.ArgumentMatchers.contains("version_key='GROUP_CATALOG'")))
                .thenReturn(List.of(Map.of("version_no",0L)));
        when(jdbc.queryForObject(org.mockito.ArgumentMatchers.contains("parent_group_id"),eq(Integer.class),eq("parent")))
                .thenReturn(0);

        assertThrows(IllegalArgumentException.class,()->repository.saveGroup(
                "child","name","parent",null,null,0L,true,"operator"));

        verify(jdbc).queryForList(org.mockito.ArgumentMatchers.contains("version_key='GROUP_CATALOG'"));
    }

}
