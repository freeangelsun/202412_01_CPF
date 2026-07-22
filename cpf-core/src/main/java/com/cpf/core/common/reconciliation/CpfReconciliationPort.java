package cpf.pfw.common.reconciliation;

import java.util.List;

/**
 * Unknown result 등록, 후속 조회, 수동 확정을 처리하는 공통 포트입니다.
 */
public interface CpfReconciliationPort {

    CpfUnknownResultRecord register(CpfUnknownResultRecord record);

    List<CpfUnknownResultRecord> find(String unknownType, String status, int limit);

    void resolve(String unknownId, String status, String operatorId, String auditReason);
}
