package com.cpf.admin.approval.spi;

import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;

/**
 * ADM이 다른 Owner Module의 DB를 직접 수정하지 않고 승인된 위험조치를 실행하는 SPI입니다.
 *
 * <p>동일 JVM에서는 Local Facade Adapter, 분리 WAS에서는 CPF Service Call 기반 Remote Adapter가
 * 같은 Command Contract를 구현해야 합니다. Timeout/UNKNOWN 결과는 호출자가 실패로 단정하지 않고
 * Reconciliation을 수행할 수 있도록 그대로 반환합니다.</p>
 */
public interface AdmApprovalOwnerCommandPort {

    /**
     * 이미 승인된 Command를 Owner에게 실행 요청합니다.
     *
     * @param command 승인된 불변 Command Snapshot
     * @return 성공/실패/결과불명 상태
     */
    AdmApprovedOperationResult execute(AdmApprovedOperationCommand command);
}
