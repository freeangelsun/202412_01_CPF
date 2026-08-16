package com.cpf.admin.approval.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdmApprovalDecisionEvaluatorTest {

    @Test
    void allRequiresEveryParticipant() {
        assertEquals(
                AdmApprovalDecisionStatus.WAITING,
                AdmApprovalDecisionEvaluator.evaluate(AdmApprovalDecisionRule.ALL, 3, 2, 0, null));
        assertEquals(
                AdmApprovalDecisionStatus.APPROVED,
                AdmApprovalDecisionEvaluator.evaluate(AdmApprovalDecisionRule.ALL, 3, 3, 0, null));
        assertEquals(
                AdmApprovalDecisionStatus.REJECTED,
                AdmApprovalDecisionEvaluator.evaluate(AdmApprovalDecisionRule.ALL, 3, 2, 1, null));
    }

    @Test
    void anyCompletesAfterFirstApproval() {
        assertEquals(
                AdmApprovalDecisionStatus.APPROVED,
                AdmApprovalDecisionEvaluator.evaluate(AdmApprovalDecisionRule.ANY, 3, 1, 1, null));
        assertEquals(
                AdmApprovalDecisionStatus.REJECTED,
                AdmApprovalDecisionEvaluator.evaluate(AdmApprovalDecisionRule.ANY, 3, 0, 3, null));
    }

    @Test
    void nOfMRejectsWhenThresholdCanNoLongerBeReached() {
        assertEquals(
                AdmApprovalDecisionStatus.WAITING,
                AdmApprovalDecisionEvaluator.evaluate(AdmApprovalDecisionRule.N_OF_M, 5, 2, 1, 3));
        assertEquals(
                AdmApprovalDecisionStatus.APPROVED,
                AdmApprovalDecisionEvaluator.evaluate(AdmApprovalDecisionRule.N_OF_M, 5, 3, 1, 3));
        assertEquals(
                AdmApprovalDecisionStatus.REJECTED,
                AdmApprovalDecisionEvaluator.evaluate(AdmApprovalDecisionRule.N_OF_M, 5, 2, 2, 4));
    }

    @Test
    void invalidPolicyFailsClosed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AdmApprovalDecisionEvaluator.evaluate(
                        AdmApprovalDecisionRule.N_OF_M, 0, 0, 0, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> AdmApprovalDecisionEvaluator.evaluate(
                        AdmApprovalDecisionRule.N_OF_M, 3, 0, 0, 4));
    }
}
