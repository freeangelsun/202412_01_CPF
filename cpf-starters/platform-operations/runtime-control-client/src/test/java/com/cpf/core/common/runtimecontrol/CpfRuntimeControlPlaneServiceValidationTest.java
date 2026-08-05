package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CpfRuntimeControlPlaneServiceValidationTest {
    private final CpfRuntimeControlPlaneRepository repository=mock(CpfRuntimeControlPlaneRepository.class);
    private final CpfRuntimeControlPlaneService service=new CpfRuntimeControlPlaneService(repository);

    @Test void rejectsUnknownRolloutBeforeStoreMutation(){
        assertThrows(IllegalArgumentException.class,()->service.createChange(command("BLUE_GREEN",0L,null,null)));
        verifyNoInteractions(repository);
    }

    @Test void requiresCasForNonRollbackChange(){
        assertThrows(IllegalArgumentException.class,()->service.createChange(command("ALL_AT_ONCE",null,null,null)));
        verifyNoInteractions(repository);
    }

    @Test void rejectsScheduleAtOrAfterExpiry(){
        Instant scheduled=Instant.now().plusSeconds(120);
        assertThrows(IllegalArgumentException.class,()->service.createChange(command("WAVE",0L,scheduled,scheduled)));
        verifyNoInteractions(repository);
    }

    @Test void rejectsDatabaseLengthOverflowBeforeStoreMutation(){
        String tooLong="x".repeat(101);
        CpfRuntimeChangeCommand invalid=new CpfRuntimeChangeCommand(tooLong,"reconciliation",1,target(),
                CpfRuntimePayload.empty(),0L,"CANARY",1,100,null,null,"reason",null,null,"operator");
        assertThrows(IllegalArgumentException.class,()->service.createChange(invalid));
        verifyNoInteractions(repository);
    }

    private CpfRuntimeChangeCommand command(String rollout,Long expected,Instant scheduled,Instant expires){
        return new CpfRuntimeChangeCommand(" operation-1 "," reconciliation ",1,target(),
                CpfRuntimePayload.empty(),expected,rollout,1,100,scheduled,expires," reason ",null,null," operator ");
    }

    private CpfRuntimeTargetSelector target(){
        return new CpfRuntimeTargetSelector(null,null,null,List.of("instance-1"),List.of(),Map.of(),null,null,false,false,false);
    }

    @Test void rejectsTargetIdentifierOverflowBeforeStoreMutation(){
        CpfRuntimeTargetSelector invalid=new CpfRuntimeTargetSelector(null,null,null,List.of("x".repeat(121)),
                List.of(),Map.of(),null,null,false,false,false);
        CpfRuntimeChangeCommand command=new CpfRuntimeChangeCommand("op","TYPE",1,invalid,CpfRuntimePayload.empty(),
                0L,"ALL_AT_ONCE",1,100,null,null,"reason",null,null,"operator");
        assertThrows(IllegalArgumentException.class,()->service.createChange(command));
        verifyNoInteractions(repository);
    }
    @Test void previewChangeDoesNotRequireCasButStillValidatesInput(){
        CpfRuntimeTargetSelector target=target();
        CpfRuntimeTargetPreview targetPreview=new CpfRuntimeTargetPreview(
                "RECONCILIATION",1,false,0,0,List.of());
        when(repository.previewTargets("RECONCILIATION",1,target)).thenReturn(targetPreview);
        when(repository.featureStates(List.of(),"RECONCILIATION")).thenReturn(List.of());
        CpfRuntimeChangeCommand preview=new CpfRuntimeChangeCommand("preview-op"," reconciliation ",1,target,
                CpfRuntimePayload.empty(),null,"ALL_AT_ONCE",1,100,null,null,
                "preview reason",null,null,"operator");

        assertDoesNotThrow(()->service.previewChange(preview));
        verify(repository).previewTargets("RECONCILIATION",1,target);
    }

    @Test void previewTargetsUsesSameSchemaAndTargetBoundary(){
        assertThrows(IllegalArgumentException.class,
                ()->service.previewTargets("TYPE",0,target()));
        CpfRuntimeTargetSelector invalid=new CpfRuntimeTargetSelector(null,null,null,
                List.of("x".repeat(121)),List.of(),Map.of(),null,null,false,false,false);
        assertThrows(IllegalArgumentException.class,
                ()->service.previewTargets("TYPE",1,invalid));
        verifyNoInteractions(repository);
    }

    @Test void registerRejectsRegistryColumnOverflowBeforeMutation(){
        CpfRuntimeInstanceRegistration invalid=new CpfRuntimeInstanceRegistration(
                "instance-1","s".repeat(41),"endpoint","dev","zone","cell","http://localhost",
                "1.0","abc","APPLICATION","SELF","1","a".repeat(64),Map.of(),Map.of(),Instant.now(),60);
        assertThrows(IllegalArgumentException.class,()->service.register(invalid));
        verifyNoInteractions(repository);
    }

    @Test void heartbeatRejectsInvalidHashAndFencingBeforeMutation(){
        assertThrows(IllegalArgumentException.class,
                ()->service.heartbeat("instance-1",0L,"a".repeat(65),1L));
        verifyNoInteractions(repository);
    }


    @Test void acknowledgeRejectsColumnOverflowAndInvalidFenceBeforeMutation(){
        CpfRuntimeAck oversized=new CpfRuntimeAck("d".repeat(81),"change-1","instance-1",1L,1,1L,
                "a".repeat(64),"ACKED",null,null,Instant.now());
        assertThrows(IllegalArgumentException.class,()->service.acknowledge(oversized));
        CpfRuntimeAck invalidFence=new CpfRuntimeAck("delivery-1","change-1","instance-1",0L,1,1L,
                "a".repeat(64),"ACKED",null,null,Instant.now());
        assertThrows(IllegalArgumentException.class,()->service.acknowledge(invalidFence));
        verifyNoInteractions(repository);
    }

    @Test void deregisterRejectsNonPositiveFenceBeforeMutation(){
        assertThrows(IllegalArgumentException.class,()->service.deregister("instance-1",0L,"reason"));
        verifyNoInteractions(repository);
    }

    @Test void statusNormalizesFiltersAndRejectsRegistryColumnOverflow(){
        service.status(" dev "," service-1 ");
        verify(repository).status("dev","service-1");

        assertThrows(IllegalArgumentException.class,()->service.status("e".repeat(41),null));
        assertThrows(IllegalArgumentException.class,()->service.status(null,"s".repeat(41)));
    }

}
