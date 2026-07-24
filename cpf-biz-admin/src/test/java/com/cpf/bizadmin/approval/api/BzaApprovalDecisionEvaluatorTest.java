package com.cpf.bizadmin.approval.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BzaApprovalDecisionEvaluatorTest {

    @Test
    void allModelsUnanimousDepartmentAgreement() {
        assertEquals(
                BzaApprovalStepStatus.WAITING,
                BzaApprovalDecisionEvaluator.evaluate(BzaApprovalDecisionRule.ALL, 4, 3, 0, null));
        assertEquals(
                BzaApprovalStepStatus.APPROVED,
                BzaApprovalDecisionEvaluator.evaluate(BzaApprovalDecisionRule.ALL, 4, 4, 0, null));
        assertEquals(
                BzaApprovalStepStatus.REJECTED,
                BzaApprovalDecisionEvaluator.evaluate(BzaApprovalDecisionRule.ALL, 4, 3, 1, null));
    }

    @Test
    void anyModelsAtLeastOneDepartmentAgreement() {
        assertEquals(
                BzaApprovalStepStatus.APPROVED,
                BzaApprovalDecisionEvaluator.evaluate(BzaApprovalDecisionRule.ANY, 4, 1, 0, null));
        assertEquals(
                BzaApprovalStepStatus.REJECTED,
                BzaApprovalDecisionEvaluator.evaluate(BzaApprovalDecisionRule.ANY, 4, 0, 4, null));
    }

    @Test
    void nOfMModelsQuorumAgreement() {
        assertEquals(
                BzaApprovalStepStatus.WAITING,
                BzaApprovalDecisionEvaluator.evaluate(BzaApprovalDecisionRule.N_OF_M, 5, 1, 1, 3));
        assertEquals(
                BzaApprovalStepStatus.APPROVED,
                BzaApprovalDecisionEvaluator.evaluate(BzaApprovalDecisionRule.N_OF_M, 5, 3, 1, 3));
        assertEquals(
                BzaApprovalStepStatus.REJECTED,
                BzaApprovalDecisionEvaluator.evaluate(BzaApprovalDecisionRule.N_OF_M, 5, 2, 2, 4));
    }

    @Test
    void invalidQuorumFailsClosed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BzaApprovalDecisionEvaluator.evaluate(
                        BzaApprovalDecisionRule.N_OF_M, 3, 0, 0, 0));
    }
}
