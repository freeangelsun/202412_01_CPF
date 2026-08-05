package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeActualState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfRuntimeControlPlaneRepositorySafetyTest {
    @Test
    void runtimeRestartDrainReleaseRequiresRuntimeOwnershipAndNoPendingRestart() {
        org.junit.jupiter.api.Assertions.assertTrue(
                CpfRuntimeControlPlaneRepository.isRuntimeRestartDrainOwned("Y", "CPF_RUNTIME_CONTROL", 0));
        assertFalse(CpfRuntimeControlPlaneRepository.isRuntimeRestartDrainOwned("Y", "operator-1", 0));
        assertFalse(CpfRuntimeControlPlaneRepository.isRuntimeRestartDrainOwned("Y", "CPF_RUNTIME_CONTROL", 1));
        assertFalse(CpfRuntimeControlPlaneRepository.isRuntimeRestartDrainOwned("N", "CPF_RUNTIME_CONTROL", 0));
    }

    @Test
    void durableActualStateProofAcceptsRecoverableDeliveryAndRollbackBaseType() {
        Map<String,Object> proof = Map.of(
                "instance_id", "instance-1",
                "change_type", "ROLLBACK:TEST",
                "desired_version", 7L,
                "delivery_state", "UNKNOWN_RESULT",
                "actual_hash", "");
        CpfRuntimeControlPlaneRepository.validateActualStateProof(
                proof, new CpfRuntimeActualState("TEST", 7L, "actual-7", "delivery-7"), "instance-1");
    }

    @Test
    void durableActualStateProofRejectsWrongVersionOrTerminalDelivery() {
        Map<String,Object> wrongVersion = Map.of(
                "instance_id", "instance-1", "change_type", "TEST", "desired_version", 8L,
                "delivery_state", "UNKNOWN_RESULT", "actual_hash", "");
        assertThrows(CpfRuntimeFenceException.class, () ->
                CpfRuntimeControlPlaneRepository.validateActualStateProof(
                        wrongVersion, new CpfRuntimeActualState("TEST", 7L, "actual-7", "delivery-7"), "instance-1"));

        Map<String,Object> cancelled = Map.of(
                "instance_id", "instance-1", "change_type", "TEST", "desired_version", 7L,
                "delivery_state", "CANCELLED", "actual_hash", "");
        assertThrows(CpfRuntimeFenceException.class, () ->
                CpfRuntimeControlPlaneRepository.validateActualStateProof(
                        cancelled, new CpfRuntimeActualState("TEST", 7L, "actual-7", "delivery-7"), "instance-1"));
    }

    @Test
    void durableActualStateProofRejectsConflictingAcknowledgedHash() {
        Map<String,Object> proof = Map.of(
                "instance_id", "instance-1", "change_type", "TEST", "desired_version", 7L,
                "delivery_state", "ACKED", "actual_hash", "actual-old");
        assertThrows(CpfRuntimeFenceException.class, () ->
                CpfRuntimeControlPlaneRepository.validateActualStateProof(
                        proof, new CpfRuntimeActualState("TEST", 7L, "actual-new", "delivery-7"), "instance-1"));
    }

    @Test
    void successfulAckMustProveDesiredVersion() {
        CpfRuntimeControlPlaneRepository.validateAckVersion(7L, 7L, "SUCCESS", "delivery-7");
        assertThrows(CpfRuntimeFenceException.class, () ->
                CpfRuntimeControlPlaneRepository.validateAckVersion(7L, 6L, "SUCCESS", "delivery-7"));
        CpfRuntimeControlPlaneRepository.validateAckVersion(7L, 6L, "FAILED", "delivery-7");
    }

    @Test
    void staleAttemptAckIsRejected() {
        assertThrows(CpfRuntimeFenceException.class, () ->
                CpfRuntimeControlPlaneRepository.validateAckAttempt(2, 1, "delivery-1"));
    }

    @Test
    void legacyAckIsAllowedOnlyForFirstClaim() {
        CpfRuntimeControlPlaneRepository.validateAckAttempt(1, 0, "delivery-1");
        assertThrows(CpfRuntimeFenceException.class, () ->
                CpfRuntimeControlPlaneRepository.validateAckAttempt(2, 0, "delivery-1"));
    }

    @Test
    void liveLeaseCannotBeTakenOverEvenBySameRegistrationSource() {
        Instant now=Instant.now();
        assertThrows(CpfRuntimeFenceException.class, () ->
                CpfRuntimeControlPlaneRepository.validateRegistrationTakeover(
                        now.plusSeconds(60), "SELF", "SELF", "instance-1", now));
    }

    @Test
    void expiredLeaseCanBeReRegistered() {
        Instant now=Instant.now();
        CpfRuntimeControlPlaneRepository.validateRegistrationTakeover(
                now.minusSeconds(1), "DEPLOYMENT", "SELF", "instance-1", now);
    }

    @Test
    void heartbeatPreservesLeaseDurationConfiguredAtRegistration() {
        Instant heartbeat=Instant.parse("2026-08-05T00:00:00Z");
        assertEquals(300,CpfRuntimeControlPlaneRepository.resolveHeartbeatLeaseSeconds(
                heartbeat,heartbeat.plusSeconds(300),60));
    }

    @Test
    void invalidHistoricalLeaseDurationFallsBackToBoundedDefault() {
        Instant heartbeat=Instant.parse("2026-08-05T00:00:00Z");
        assertEquals(60,CpfRuntimeControlPlaneRepository.resolveHeartbeatLeaseSeconds(
                heartbeat,heartbeat.plusSeconds(7200),60));
    }

    @Test
    void ackErrorCodeIsCanonicalAndDatabaseBounded() {
        assertEquals("TEMPORARY_FAILURE",CpfRuntimeControlPlaneRepository.normalizeErrorCode(" temporary_failure "));
        assertEquals(80,CpfRuntimeControlPlaneRepository.normalizeErrorCode("x".repeat(100)).length());
    }

    @Test
    void controllerMasksSensitiveAckMessageEvenForRemoteAgent() {
        String masked=CpfRuntimeControlPlaneRepository.sanitizeRuntimeMessage(
                "password=plain user@example.com 900101-1234567 010-1234-5678",900);
        assertFalse(masked.contains("plain"));
        assertFalse(masked.contains("user@example.com"));
        assertFalse(masked.contains("900101-1234567"));
        assertFalse(masked.contains("010-1234-5678"));
    }
    @Test
    void auditReasonUsesSameServerSideMaskingAndColumnBound() {
        String masked=CpfRuntimeControlPlaneRepository.sanitizeRuntimeMessage(
                "authorization=Bearer-secret 01012345678 " + "x".repeat(700),500);
        assertFalse(masked.contains("Bearer-secret"));
        assertFalse(masked.contains("01012345678"));
        assertEquals(500,masked.length());
    }


    @Test
    void rollbackActualStateProofUsesBaseFeatureType() {
        java.util.Map<String,Object> proof=java.util.Map.of(
                "instance_id","i1","change_type","ROLLBACK:RECONCILIATION",
                "desired_version",3L,"delivery_state","UNKNOWN_RESULT",
                "actual_hash","old","error_code","ACK_TIMEOUT");
        CpfRuntimeControlPlaneRepository.validateActualStateProof(
                proof,new CpfRuntimeActualState("RECONCILIATION",3L,"new","d1"),"i1");
    }
    @Test void healthCountsSaturateWithoutIntegerOverflow(){
        org.junit.jupiter.api.Assertions.assertEquals(Integer.MAX_VALUE,
                CpfRuntimeControlPlaneRepository.saturatingCount((long)Integer.MAX_VALUE+1L));
        org.junit.jupiter.api.Assertions.assertEquals(7,
                CpfRuntimeControlPlaneRepository.saturatingCount(7L));
        assertThrows(IllegalStateException.class,
                ()->CpfRuntimeControlPlaneRepository.nonNegativeCount(-1L,"backlogCount"));
    }

    @Test void monotonicVersionsFailClosedBeforeLongOverflow(){
        assertEquals(8L,CpfRuntimeControlPlaneRepository.nextMonotonic(7L,"version"));
        assertThrows(IllegalStateException.class,
                ()->CpfRuntimeControlPlaneRepository.nextMonotonic(Long.MAX_VALUE,"version"));
        assertThrows(IllegalStateException.class,
                ()->CpfRuntimeControlPlaneRepository.nextMonotonic(-1L,"version"));
    }

}
