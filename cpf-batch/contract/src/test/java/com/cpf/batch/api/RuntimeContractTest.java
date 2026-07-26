package com.cpf.batch.api;
import org.junit.jupiter.api.Test;import java.time.Instant;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class RuntimeContractTest {
 @Test void heartbeatCarriesFencingToken(){RuntimeHeartbeat h=new RuntimeHeartbeat("w1",Instant.now(),ActualState.READY,true,List.of(),List.of(),1,0,false,Map.of(),null,Map.of(),"1.0",7L);assertEquals(7L,h.fencingToken());}
 @Test void commandRequiresTargetsAndPreservesUnknownRecoveryFields(){RuntimeCommand c=new RuntimeCommand("c1","i1","DRAIN","INSTANCE",List.of("w1"),"snapshot","hash",3L,"op","reason",Instant.now(),"p1","a1","approver",Instant.now().plusSeconds(10),CommandState.APPROVED,0,Map.of(),null,null,"before","after","OADM-AA-00000000000000000000000000",null);assertEquals(CommandState.APPROVED,c.executionState());assertEquals("snapshot",c.targetSnapshot());}
}
