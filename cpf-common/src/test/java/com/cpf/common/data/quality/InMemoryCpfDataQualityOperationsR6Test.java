package com.cpf.common.data.quality;

import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.api.data.quality.CpfDataQualityRule;
import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryCpfDataQualityOperationsR6Test {
    private static InMemoryCpfDataQualityOperations operations() {
        return new InMemoryCpfDataQualityOperations(command -> "v".repeat(64).equals(command.proof()));
    }
    private static String quarantine(InMemoryCpfDataQualityOperations operations) {
        operations.register(new CpfDataQualityRule("required-name",1,"name","NOT_BLANK",
                CpfDataQualityRule.Severity.ERROR,CpfDataQualityRule.State.ACTIVE,Map.of()),"admin","register rule");
        Map<String,Object> nullable = new LinkedHashMap<>(); nullable.put("name",null);
        return operations.validate("R-1",nullable).quarantineId();
    }
    private static CpfDataQualityCorrectionPort.ApprovedCorrection command(String id,long version,Map<String,Object> corrected,String proof) {
        return new CpfDataQualityCorrectionPort.ApprovedCorrection(id,version,corrected,"approver","approved correction",
                "ADM-APP-1-CMD-1","a".repeat(64),"nonce-0123456789abcdef",proof,Instant.parse("2026-08-07T00:00:00Z"));
    }

    @Test void treatsNullAsRuleViolationInsteadOfNpe() {
        InMemoryCpfDataQualityOperations operations=operations(); String id=quarantine(operations);
        assertEquals("QUARANTINED",operations.quarantine(id).orElseThrow().state());
        assertNull(operations.quarantine(id).orElseThrow().original().get("name"));
    }

    @Test void correctionIsFailClosedAndNullSafe() {
        InMemoryCpfDataQualityOperations operations=operations(); String id=quarantine(operations);
        assertThrows(SecurityException.class,()->operations.correctApproved(command(id,1,Map.of("name","ok"),"f".repeat(64))));
        Map<String,Object> corrected=new LinkedHashMap<>(); corrected.put("name",null);
        var after=operations.correctApproved(command(id,1,corrected,"v".repeat(64)));
        assertEquals("CORRECTED",after.state()); assertNull(after.corrected().get("name"));
    }

    @Test void replayUsesCasAndOperationIdWithoutCreatingAnOrphanQuarantine() {
        InMemoryCpfDataQualityOperations operations=operations(); String id=quarantine(operations);
        operations.correctApproved(command(id,1,Map.of("name","ok"),"v".repeat(64)));
        var replay=new CpfDataQualityOperations.ReplayCommand(id,2,"operation-0001","operator","replay corrected row");
        var first=operations.replay(replay); var same=operations.replay(replay);
        assertTrue(first.accepted()); assertSame(first,same); assertEquals("REPLAYED",operations.quarantine(id).orElseThrow().state());
        assertThrows(ConcurrentModificationException.class,()->operations.replay(
                new CpfDataQualityOperations.ReplayCommand(id,2,"operation-0002","operator","stale replay")));
    }

    @Test void operationIdCannotBeReusedForAnotherReplayCommand() {
        InMemoryCpfDataQualityOperations operations=operations(); String id=quarantine(operations);
        Map<String,Object> corrected=new LinkedHashMap<>(); corrected.put("name",null);
        operations.correctApproved(command(id,1,corrected,"v".repeat(64)));
        var first=new CpfDataQualityOperations.ReplayCommand(id,2,"operation-bound","operator","first replay reason");
        assertFalse(operations.replay(first).accepted());
        assertThrows(IllegalStateException.class,()->operations.replay(
                new CpfDataQualityOperations.ReplayCommand(id,2,"operation-bound","other-operator","different replay reason")));
    }

    @Test void rejectedReplayKeepsTheSameQuarantineIdentity() {
        InMemoryCpfDataQualityOperations operations=operations(); String id=quarantine(operations);
        Map<String,Object> corrected=new LinkedHashMap<>(); corrected.put("name",null);
        operations.correctApproved(command(id,1,corrected,"v".repeat(64)));
        var result=operations.replay(new CpfDataQualityOperations.ReplayCommand(id,2,"operation-rejected","operator","replay invalid row"));
        assertFalse(result.accepted()); assertEquals("",result.quarantineId());
        assertEquals(id,operations.quarantine(id).orElseThrow().quarantineId());
        assertEquals("CORRECTED",operations.quarantine(id).orElseThrow().state());
    }
}
