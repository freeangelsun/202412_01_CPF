package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayHealthStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CpfGatewayHealthEvaluatorTest {
    private final CpfGatewayHealthEvaluator evaluator=new CpfGatewayHealthEvaluator();
    private final CpfGatewayHealthEvaluator.Policy policy=new CpfGatewayHealthEvaluator.Policy(2,2,2,Duration.ofSeconds(30));

    @Test void failureThresholdAndRecoveryHysteresis(){
        OffsetDateTime now=OffsetDateTime.now();
        var first=evaluator.evaluate(policy,new CpfGatewayHealthEvaluator.State(CpfGatewayHealthStatus.UP,null,0,0),new CpfGatewayHealthEvaluator.ProbeResult(false,"TIMEOUT",100,now),now);
        assertEquals(CpfGatewayHealthStatus.DEGRADED,first.status());
        var down=evaluator.evaluate(policy,new CpfGatewayHealthEvaluator.State(first.status(),null,first.consecutiveSuccesses(),first.consecutiveFailures()),new CpfGatewayHealthEvaluator.ProbeResult(false,"TIMEOUT",100,now),now);
        assertEquals(CpfGatewayHealthStatus.DOWN,down.status());
        var recovering=evaluator.evaluate(policy,new CpfGatewayHealthEvaluator.State(down.status(),null,0,down.consecutiveFailures()),new CpfGatewayHealthEvaluator.ProbeResult(true,"OK",10,now),now);
        assertEquals(CpfGatewayHealthStatus.RECOVERING,recovering.status());
        var up=evaluator.evaluate(policy,new CpfGatewayHealthEvaluator.State(recovering.status(),null,recovering.consecutiveSuccesses(),0),new CpfGatewayHealthEvaluator.ProbeResult(true,"OK",10,now),now);
        assertEquals(CpfGatewayHealthStatus.UP,up.status());
    }

    @Test void staleAndManualStatesWin(){
        OffsetDateTime now=OffsetDateTime.now();
        var stale=evaluator.evaluate(policy,new CpfGatewayHealthEvaluator.State(CpfGatewayHealthStatus.UP,null,2,0),new CpfGatewayHealthEvaluator.ProbeResult(true,"OK",10,now.minusMinutes(2)),now);
        assertEquals(CpfGatewayHealthStatus.STALE,stale.status());
        var drain=evaluator.evaluate(policy,new CpfGatewayHealthEvaluator.State(CpfGatewayHealthStatus.UP,CpfGatewayHealthStatus.DRAINING,2,0),new CpfGatewayHealthEvaluator.ProbeResult(true,"OK",10,now),now);
        assertEquals(CpfGatewayHealthStatus.DRAINING,drain.status());
    }
}
