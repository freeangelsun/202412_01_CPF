package com.cpf.backoffice.online.approval.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BackofficeApprovalDecisionEvaluatorTest {

    @Test
    void allModelsUnanimousDepartmentAgreement() {
        assertEquals(
                BackofficeApprovalStepStatus.WAITING,
                BackofficeApprovalDecisionEvaluator.evaluate(BackofficeApprovalDecisionRule.ALL, 4, 3, 0, null));
        assertEquals(
                BackofficeApprovalStepStatus.APPROVED,
                BackofficeApprovalDecisionEvaluator.evaluate(BackofficeApprovalDecisionRule.ALL, 4, 4, 0, null));
        assertEquals(
                BackofficeApprovalStepStatus.REJECTED,
                BackofficeApprovalDecisionEvaluator.evaluate(BackofficeApprovalDecisionRule.ALL, 4, 3, 1, null));
    }

    @Test
    void anyModelsAtLeastOneDepartmentAgreement() {
        assertEquals(
                BackofficeApprovalStepStatus.APPROVED,
                BackofficeApprovalDecisionEvaluator.evaluate(BackofficeApprovalDecisionRule.ANY, 4, 1, 0, null));
        assertEquals(
                BackofficeApprovalStepStatus.REJECTED,
                BackofficeApprovalDecisionEvaluator.evaluate(BackofficeApprovalDecisionRule.ANY, 4, 0, 4, null));
    }

    @Test
    void nOfMModelsQuorumAgreement() {
        assertEquals(
                BackofficeApprovalStepStatus.WAITING,
                BackofficeApprovalDecisionEvaluator.evaluate(BackofficeApprovalDecisionRule.N_OF_M, 5, 1, 1, 3));
        assertEquals(
                BackofficeApprovalStepStatus.APPROVED,
                BackofficeApprovalDecisionEvaluator.evaluate(BackofficeApprovalDecisionRule.N_OF_M, 5, 3, 1, 3));
        assertEquals(
                BackofficeApprovalStepStatus.REJECTED,
                BackofficeApprovalDecisionEvaluator.evaluate(BackofficeApprovalDecisionRule.N_OF_M, 5, 2, 2, 4));
    }

    @Test
    void invalidQuorumFailsClosed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BackofficeApprovalDecisionEvaluator.evaluate(
                        BackofficeApprovalDecisionRule.N_OF_M, 3, 0, 0, 0));
    }
}
