package com.cpf.admin.approval.spi;

import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AdmApprovalOwnerCommandPortR6Test {
    @Test void legacyTwoDimensionalAdapterCannotAuthorizeFourDimensionalTuple() {
        AdmApprovalOwnerCommandPort legacy = new AdmApprovalOwnerCommandPort() {
            @Override public boolean supports(String ownerModule,String ownerCommand){ return true; }
            @Override public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command){ return null; }
        };
        assertFalse(legacy.supports("ADM","dangerousCommand","LOW_RISK_ACTION","WRONG_TARGET"));
    }
}
