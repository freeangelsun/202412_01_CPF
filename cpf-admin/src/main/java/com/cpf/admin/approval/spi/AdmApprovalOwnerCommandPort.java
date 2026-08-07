package com.cpf.admin.approval.spi;

import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;

/** Owner Module의 승인된 위험조치를 실행하는 명시적 SPI입니다. */
public interface AdmApprovalOwnerCommandPort {
    /** Bean 이름 추측이나 단일 Port fallback 없이 Owner/Command를 명시적으로 선언합니다. */
    default boolean supports(String ownerModule, String ownerCommand) {
        return false;
    }

    /**
     * Full server-registry binding. Every executable adapter must override this method and bind the
     * exact owner/command/action/target tuple. The default is fail-closed so legacy two-dimensional
     * adapters cannot silently authorize arbitrary action or target values.
     */
    default boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        return false;
    }

    AdmApprovedOperationResult execute(AdmApprovedOperationCommand command);

    /**
     * Reconciles an UNKNOWN execution without invoking the mutation again.
     * Owner adapters that cannot prove the side effect state remain UNKNOWN fail-closed.
     */
    default AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command) {
        return new AdmApprovedOperationResult(
                AdmApprovalExecutionStatus.UNKNOWN,
                "ADM-RECONCILE-UNSUPPORTED",
                "Owner 상태를 확정할 수 없어 UNKNOWN을 유지합니다.");
    }
}

