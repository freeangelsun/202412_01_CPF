package com.cpf.admin.approval.spi;

import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;

/** Owner Module의 승인된 위험조치를 실행하는 명시적 SPI입니다. */
public interface AdmApprovalOwnerCommandPort {
    /** Bean 이름 추측이나 단일 Port fallback 없이 Owner/Command를 명시적으로 선언합니다. */
    default boolean supports(String ownerModule, String ownerCommand) {
        return false;
    }

    AdmApprovedOperationResult execute(AdmApprovedOperationCommand command);
}
