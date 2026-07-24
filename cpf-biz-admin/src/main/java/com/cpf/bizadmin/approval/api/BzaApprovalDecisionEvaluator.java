package com.cpf.bizadmin.approval.api;

import java.util.Objects;

/**
 * BZA 결재 Target/단계의 ALL/ANY/N_OF_M 규칙을 순수 계산합니다.
 *
 * <p>실제 Engine은 정책 Target을 상신 시점 참여자로 먼저 Resolution하고 그 Snapshot을 기준으로
 * 이 계산을 수행해야 합니다. 조직개편이나 역할 변경 때문에 진행 중 결재의 분모가 바뀌면 안 됩니다.</p>
 */
public final class BzaApprovalDecisionEvaluator {

    private BzaApprovalDecisionEvaluator() {
    }

    /**
     * 현재 참여자 결정 집계를 평가합니다.
     *
     * @param rule Target 결정 규칙
     * @param participantCount 상신 시 Snapshot으로 고정된 참여자 수
     * @param approvedOrAgreedCount 승인/합의 수
     * @param rejectedCount 반려 수
     * @param requiredCount N_OF_M 최소 수
     * @return 현재 Target/단계 상태
     */
    public static BzaApprovalStepStatus evaluate(
            BzaApprovalDecisionRule rule,
            int participantCount,
            int approvedOrAgreedCount,
            int rejectedCount,
            Integer requiredCount) {
        Objects.requireNonNull(rule, "rule");
        validateCounts(participantCount, approvedOrAgreedCount, rejectedCount);

        int threshold = switch (rule) {
            case ALL -> participantCount;
            case ANY -> 1;
            case N_OF_M -> validateRequiredCount(requiredCount, participantCount);
        };

        if (approvedOrAgreedCount >= threshold) {
            return BzaApprovalStepStatus.APPROVED;
        }

        int remaining = participantCount - approvedOrAgreedCount - rejectedCount;
        if (approvedOrAgreedCount + remaining < threshold) {
            return BzaApprovalStepStatus.REJECTED;
        }
        return BzaApprovalStepStatus.WAITING;
    }

    private static void validateCounts(
            int participantCount,
            int approvedOrAgreedCount,
            int rejectedCount) {
        if (participantCount <= 0) {
            throw new IllegalArgumentException(
                    "결재 참여자가 0명인 정책 Target은 상신 시 fail-closed 해야 합니다.");
        }
        if (approvedOrAgreedCount < 0 || rejectedCount < 0) {
            throw new IllegalArgumentException("승인/합의/반려 집계 수는 음수일 수 없습니다.");
        }
        if (approvedOrAgreedCount + rejectedCount > participantCount) {
            throw new IllegalArgumentException("결정 집계가 전체 참여자 수를 초과합니다.");
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
