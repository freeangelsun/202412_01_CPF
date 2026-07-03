package cpf.mbr.bse.service;

import cpf.mbr.bse.dto.MbrDTO;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.segment.TransactionSegmentDirection;
import cpf.pfw.common.logging.segment.TransactionSegmentRole;
import cpf.pfw.common.logging.segment.TransactionSegmentScope;
import cpf.pfw.common.logging.segment.TransactionSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MBR 관점의 복합 거래 구간을 기록하는 교육 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class MbrCompositeTransactionService {
    private final TransactionSegmentService segmentService;
    private final MbrService mbrService;
    private final MbrExsClientService exsClientService;

    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public Map<String, Object> memberProfile(Integer memberId) {
        try (TransactionSegmentScope scope = segmentService.start(
                TransactionSegmentRole.SHARED,
                TransactionSegmentDirection.INBOUND,
                "MBR",
                "ACC",
                "MBR",
                "/mbr/edu/composite/member-profile",
                "MBR 회원 조회 구간")) {
            try {
                MbrDTO member = mbrService.getMemberById(memberId);
                Map<String, Object> response = response(scope, member, null);
                scope.success();
                return response;
            } catch (RuntimeException ex) {
                scope.fail(ex.getClass().getSimpleName(), ex.getMessage());
                throw ex;
            }
        }
    }

    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public Map<String, Object> memberCallsExternal(Integer memberId) {
        try (TransactionSegmentScope scope = segmentService.start(
                TransactionSegmentRole.SUB,
                TransactionSegmentDirection.INBOUND,
                "MBR",
                "ACC",
                "EXS",
                "/mbr/edu/composite/member-calls-external",
                "MBR 회원 검증 후 외부연계 호출")) {
            try {
                MbrDTO member = mbrService.getMemberById(memberId);
                Map<String, Object> external = segmentService.around(
                        TransactionSegmentRole.EXTERNAL,
                        TransactionSegmentDirection.OUTBOUND,
                        "MBR",
                        "MBR",
                        "EXS",
                        "/api/exs/edu/external-transfer",
                        "MBR → EXS 외부 검증 호출",
                        () -> exsClientService.requestExternalVerification(memberId));
                Map<String, Object> result = response(scope, member, external);
                result.put("segmentIds", nestedSegmentIds(scope, external));
                scope.success();
                return result;
            } catch (RuntimeException ex) {
                scope.fail(ex.getClass().getSimpleName(), ex.getMessage());
                throw ex;
            }
        }
    }

    private List<String> nestedSegmentIds(TransactionSegmentScope scope, Map<String, Object> external) {
        Object externalSegmentId = external != null ? external.get("transactionSegmentId") : null;
        if (externalSegmentId == null || String.valueOf(externalSegmentId).isBlank()) {
            return List.of(scope.transactionSegmentId());
        }
        return List.of(scope.transactionSegmentId(), String.valueOf(externalSegmentId));
    }

    private Map<String, Object> response(TransactionSegmentScope scope, MbrDTO member, Map<String, Object> external) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        response.put("rootTransactionGlobalId", TransactionContext.originalTransactionId());
        response.put("transactionSegmentId", scope.transactionSegmentId());
        response.put("moduleCode", "MBR");
        response.put("transactionRole", scope.record().getTransactionRole());
        response.put("memberId", member.getMemberId());
        response.put("memberNo", member.getMemberNo());
        response.put("customerNo", member.getCustomerNo());
        response.put("memberStatus", member.getMemberStatus());
        response.put("external", external);
        return response;
    }
}
