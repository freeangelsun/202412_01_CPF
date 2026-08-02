package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.*;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CpfGatewayTargetSelectorTest {
    private final CpfGatewayTargetSelector selector = new CpfGatewayTargetSelector();

    @Test void excludesDownOpenAndManualTargets() {
        var result = selector.select(request(CpfGatewayLoadBalancePolicy.ROUND_ROBIN, "key", List.of(
                candidate("down", 100, 0, CpfGatewayHealthStatus.DOWN, "CLOSED", false, false),
                candidate("open", 100, 0, CpfGatewayHealthStatus.UP, "OPEN", false, false),
                candidate("drain", 100, 0, CpfGatewayHealthStatus.UP, "CLOSED", true, false),
                candidate("ok", 100, 0, CpfGatewayHealthStatus.UP, "CLOSED", false, false))));
        assertEquals("ok", result.instanceId());
        assertEquals(1, result.eligibleCount());
    }

    @Test void weightedRoundRobinHonorsWeight() {
        List<String> selected = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> selector.select(request(CpfGatewayLoadBalancePolicy.WEIGHTED_ROUND_ROBIN, "", List.of(
                        candidate("a", 3, 0, CpfGatewayHealthStatus.UP, "CLOSED", false, false),
                        candidate("b", 1, 0, CpfGatewayHealthStatus.UP, "CLOSED", false, false)))).instanceId()).toList();
        assertEquals(List.of("a", "a", "a", "b"), selected);
    }

    @Test void rendezvousIsStable() {
        var candidates = List.of(candidate("a", 1, 0, CpfGatewayHealthStatus.UP, "CLOSED", false, false), candidate("b", 1, 0, CpfGatewayHealthStatus.UP, "CLOSED", false, false));
        String first = selector.select(request(CpfGatewayLoadBalancePolicy.RENDEZVOUS_HASH, "customer-1", candidates)).instanceId();
        for (int i=0;i<10;i++) assertEquals(first, selector.select(request(CpfGatewayLoadBalancePolicy.RENDEZVOUS_HASH, "customer-1", candidates)).instanceId());
    }

    @Test void priorityFailoverUsesLowestHealthyPriority() {
        var result=selector.select(request(CpfGatewayLoadBalancePolicy.PRIORITY_FAILOVER,"",List.of(
                candidate("secondary",100,10,CpfGatewayHealthStatus.UP,"CLOSED",false,false),
                candidate("primary",100,1,CpfGatewayHealthStatus.UP,"CLOSED",false,false))));
        assertEquals("primary",result.instanceId());
    }

    private static CpfGatewayTargetSelectionPort.SelectionRequest request(CpfGatewayLoadBalancePolicy policy,String key,List<CpfGatewayTargetSelectionPort.TargetCandidate> candidates){
        return new CpfGatewayTargetSelectionPort.SelectionRequest("group",policy,key,candidates,Map.of(),OffsetDateTime.now());
    }
    private static CpfGatewayTargetSelectionPort.TargetCandidate candidate(String id,int weight,int priority,CpfGatewayHealthStatus health,String circuit,boolean draining,boolean maintenance){
        return new CpfGatewayTargetSelectionPort.TargetCandidate(id,"127.0.0.1",8080,weight,priority,health,circuit,true,draining,maintenance,0,1,0,OffsetDateTime.now());
    }
}
