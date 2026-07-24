package com.cpf.admin.approval.api;

import java.util.Objects;

/**
 * ADM 승인 단계의 {@link AdmApprovalDecisionRule}을 순수 계산하는 기술 Helper입니다.
 *
 * <p>이 Class는 DB 저장, 승인자 자격 확인, 자기승인 금지, 권한, 감사 또는 Owner Command 실행을
 * 수행하지 않습니다. Approval Engine은 먼저 승인 참여자 Snapshot과 현재 결정을 원자적으로
 * 읽은 뒤 이 계산 결과를 사용하고, 상태 전이는 optimistic version 조건으로 한 번만 반영해야 합니다.</p>
 */
public final class AdmApprovalDecisionEvaluator {

    private AdmApprovalDecisionEvaluator() {
    }

    /**
     * 현재 승인 집계가 단계 완료/실패 조건을 충족했는지 계산합니다.
     *
     * @param rule 승인 정책 규칙
     * @param participantCount 상신/요청 시 Snapshot으로 확정된 전체 참여자 수
     * @param approvedCount 승인 완료 수
     * @param rejectedCount 반려 완료 수
     * @param requiredCount {@code N_OF_M}일 때 필요한 최소 승인 수, 그 외에는 {@code null} 허용
     * @return 현재 단계 계산 상태
     * @throws IllegalArgumentException 참여자/집계 수 또는 {@code requiredCount}가 정책상 불가능한 경우
     */
    public static AdmApprovalDecisionStatus evaluate(
            AdmApprovalDecisionRule rule,
            int participantCount,
            int approvedCount,
            int rejectedCount,
            Integer requiredCount) {
        Objects.requireNonNull(rule, "rule");
        validateCounts(participantCount, approvedCount, rejectedCount);

        int threshold = switch (rule) {
            case ALL -> participantCount;
            case ANY -> 1;
            case N_OF_M -> validateRequiredCount(requiredCount, participantCount);
        };

        if (approvedCount >= threshold) {
            return AdmApprovalDecisionStatus.APPROVED;
        }

        int remaining = participantCount - approvedCount - rejectedCount;
        if (approvedCount + remaining < threshold) {
            return AdmApprovalDecisionStatus.REJECTED;
        }
        return AdmApprovalDecisionStatus.WAITING;
    }

    private static void validateCounts(
            int participantCount,
            int approvedCount,
            int rejectedCount) {
        if (participantCount <= 0) {
            throw new IllegalArgumentException(
                    "승인 참여자가 0명인 정책 Target은 fail-closed 해야 합니다.");
        }
        if (approvedCount < 0 || rejectedCount < 0) {
            throw new IllegalArgumentException("승인/반려 집계 수는 음수일 수 없습니다.");
        }
        if (approvedCount + rejectedCount > participantCount) {
            throw new IllegalArgumentException("승인/반려 집계가 전체 참여자 수를 초과합니다.");
        }
    }

    private static int validateRequiredCount(Integer requiredCount, int participantCount) {
        if (requiredCount == null || requiredCount <= 0 || requiredCount > participantCount) {
            throw new IllegalArgumentException(
                    "N_OF_M requiredCount는 1 이상 전체 참여자 수 이하여야 합니다.");
        }
        return requiredCount;
    }
}
