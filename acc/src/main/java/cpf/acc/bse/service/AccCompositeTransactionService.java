package cpf.acc.bse.service;

import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.segment.TransactionSegmentDirection;
import cpf.pfw.common.logging.segment.TransactionSegmentRole;
import cpf.pfw.common.logging.segment.TransactionSegmentScope;
import cpf.pfw.common.logging.segment.TransactionSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ACC가 오케스트레이션 주체가 되는 복합 거래 교육 샘플입니다.
 */
@Service
@RequiredArgsConstructor
public class AccCompositeTransactionService {
    private final TransactionSegmentService segmentService;
    private final MbrMemberClientService mbrMemberClientService;
    private final ExsExchangeClientService exsExchangeClientService;

    public Map<String, Object> memberThenExternal(Integer memberId) {
        long started = System.nanoTime();
        List<String> flow = new ArrayList<>();
        List<String> segmentIds = new ArrayList<>();
        try (TransactionSegmentScope main = segmentService.start(
                TransactionSegmentRole.MAIN,
                TransactionSegmentDirection.INBOUND,
                "ACC",
                null,
                "ACC",
                "/acc/edu/composite/member-then-external",
                "ACC 복합 거래 시작")) {
            try {
                flow.add("ACC MAIN");
                segmentIds.add(main.transactionSegmentId());

                Map<String, Object> member = callMbr(memberId, flow, segmentIds);
                Map<String, Object> external = callExs(memberId, flow, segmentIds);

                Map<String, Object> response = baseResponse(started, flow, segmentIds);
                response.put("pattern", "ACC-MBR-ACC-EXS-ACC");
                response.put("member", member);
                response.put("external", external);
                response.put("overallStatus", "SUCCESS");
                main.success();
                return response;
            } catch (RuntimeException ex) {
                main.fail(ex.getClass().getSimpleName(), ex.getMessage());
                throw ex;
            }
        }
    }

    public Map<String, Object> memberCallsExternal(Integer memberId) {
        long started = System.nanoTime();
        List<String> flow = new ArrayList<>();
        List<String> segmentIds = new ArrayList<>();
        try (TransactionSegmentScope main = segmentService.start(
                TransactionSegmentRole.MAIN,
                TransactionSegmentDirection.INBOUND,
                "ACC",
                null,
                "MBR",
                "/acc/edu/composite/member-calls-external",
                "ACC가 MBR 중첩 거래 호출")) {
            try {
                flow.add("ACC MAIN");
                segmentIds.add(main.transactionSegmentId());

                Map<String, Object> memberExternal = callMbrWithExternal(memberId, flow, segmentIds);

                Map<String, Object> response = baseResponse(started, flow, segmentIds);
                response.put("pattern", "ACC-MBR-EXS-MBR-ACC");
                response.put("memberExternal", memberExternal);
                response.put("overallStatus", "SUCCESS");
                main.success();
                return response;
            } catch (RuntimeException ex) {
                main.fail(ex.getClass().getSimpleName(), ex.getMessage());
                throw ex;
            }
        }
    }

    private Map<String, Object> callMbr(Integer memberId, List<String> flow, List<String> segmentIds) {
        try (TransactionSegmentScope outbound = segmentService.start(
                TransactionSegmentRole.SUB,
                TransactionSegmentDirection.OUTBOUND,
                "ACC",
                "ACC",
                "MBR",
                "/mbr/edu/composite/member-profile",
                "ACC -> MBR 회원 조회")) {
            try {
                segmentIds.add(outbound.transactionSegmentId());
                Map<String, Object> result = mbrMemberClientService.callCompositeMember(memberId);
                outbound.success();
                flow.add("ACC -> MBR OUTBOUND");
                addNestedSegmentIds(segmentIds, result);
                return result;
            } catch (RuntimeException ex) {
                outbound.fail(ex.getClass().getSimpleName(), ex.getMessage());
                throw ex;
            }
        }
    }

    private Map<String, Object> callExs(Integer memberId, List<String> flow, List<String> segmentIds) {
        try (TransactionSegmentScope outbound = segmentService.start(
                TransactionSegmentRole.EXTERNAL,
                TransactionSegmentDirection.OUTBOUND,
                "ACC",
                "ACC",
                "EXS",
                "/api/exs/edu/external-transfer",
                "ACC -> EXS 외부연계 호출")) {
            try {
                segmentIds.add(outbound.transactionSegmentId());
                String externalTransactionId = "EXT-ACC-" + System.currentTimeMillis();
                outbound.record().setExternalInstitutionCode("BANK01");
                outbound.record().setExternalTransactionId(externalTransactionId);
                Map<String, Object> result = exsExchangeClientService.requestExternalTransfer(Map.of(
                        "memberId", memberId,
                        "institutionCode", "BANK01",
                        "externalTransactionId", externalTransactionId));
                outbound.success();
                flow.add("ACC -> EXS OUTBOUND");
                addNestedSegmentIds(segmentIds, result);
                return result;
            } catch (RuntimeException ex) {
                outbound.fail(ex.getClass().getSimpleName(), ex.getMessage());
                throw ex;
            }
        }
    }

    private Map<String, Object> callMbrWithExternal(Integer memberId, List<String> flow, List<String> segmentIds) {
        try (TransactionSegmentScope outbound = segmentService.start(
                TransactionSegmentRole.SUB,
                TransactionSegmentDirection.OUTBOUND,
                "ACC",
                "ACC",
                "MBR",
                "/mbr/edu/composite/member-calls-external",
                "ACC -> MBR 중첩 외부연계 호출")) {
            try {
                segmentIds.add(outbound.transactionSegmentId());
                Map<String, Object> result = mbrMemberClientService.callMemberWithExternal(memberId);
                outbound.success();
                flow.add("ACC -> MBR OUTBOUND");
                addNestedSegmentIds(segmentIds, result);
                return result;
            } catch (RuntimeException ex) {
                outbound.fail(ex.getClass().getSimpleName(), ex.getMessage());
                throw ex;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addNestedSegmentIds(List<String> segmentIds, Map<String, Object> result) {
        if (result == null) {
            return;
        }
        Object segmentId = result.get("transactionSegmentId");
        if (segmentId != null) {
            segmentIds.add(String.valueOf(segmentId));
        }
        Object nested = result.get("segmentIds");
        if (nested instanceof List<?> values) {
            values.forEach(value -> segmentIds.add(String.valueOf(value)));
        }
    }

    private Map<String, Object> baseResponse(long started, List<String> flow, List<String> segmentIds) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        response.put("rootTransactionGlobalId", TransactionContext.originalTransactionId());
        response.put("segmentCount", segmentIds.size());
        response.put("segmentIds", segmentIds);
        response.put("moduleFlow", List.copyOf(flow));
        response.put("moduleFlowText", String.join(" -> ", flow));
        response.put("overallDurationMs", (System.nanoTime() - started) / 1_000_000L);
        response.put("failedSegmentId", null);
        return response;
    }
}
