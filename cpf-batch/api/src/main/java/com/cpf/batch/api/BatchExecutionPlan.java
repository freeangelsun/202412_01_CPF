package com.cpf.batch.api;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Published Definition을 Spring Batch Job/Step graph로 materialize하는 승인된 실행 계획입니다. */
public record BatchExecutionPlan(
        String planId,
        long planVersion,
        BatchExecutionTopology topology,
        List<BatchStepDefinition> steps,
        String checksum) {
    public BatchExecutionPlan {
        if (planId == null || !planId.matches("[A-Za-z0-9._-]{1,120}")) {
            throw new IllegalArgumentException("Invalid planId.");
        }
        if (planVersion <= 0) throw new IllegalArgumentException("planVersion must be positive.");
        if (topology == null) throw new IllegalArgumentException("topology is required.");
        steps = steps == null ? List.of() : List.copyOf(steps);
        if (steps.isEmpty()) throw new IllegalArgumentException("At least one Spring Batch step is required.");
        if (steps.size() > 1_000) throw new IllegalArgumentException("Execution plan step count exceeds 1000.");
        Set<String> ids = new HashSet<>();
        for (BatchStepDefinition step : steps) {
            if (step == null) throw new IllegalArgumentException("Execution plan contains a null step.");
            if (!ids.add(step.stepId())) throw new IllegalArgumentException("Duplicate stepId: " + step.stepId());
        }
        for (BatchStepDefinition step : steps) {
            validateTarget(ids, step.nextOnSuccess());
            validateTarget(ids, step.nextOnFailure());
        }
        if (checksum == null || !checksum.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("plan checksum must be SHA-256.");
        }
        checksum = checksum.toLowerCase(Locale.ROOT);
        String actual = BatchCanonicalDigest.planHash(planId, planVersion, topology, steps);
        if (!actual.equals(checksum)) {
            throw new IllegalArgumentException("Execution plan checksum mismatch.");
        }
    }

    /** 승인·로드·실행 경계에서 재계산할 Canonical Digest입니다. */
    public String recomputeChecksum() {
        return BatchCanonicalDigest.planHash(planId, planVersion, topology, steps);
    }

    public void verifyIntegrity() {
        if (!checksum.equals(recomputeChecksum())) {
            throw new SecurityException("BATCH_PLAN_INTEGRITY_MISMATCH:" + planId + ":" + planVersion);
        }
    }

    private static void validateTarget(Set<String> ids, String target) {
        if (!target.isBlank() && !"END".equals(target) && !ids.contains(target)) {
            throw new IllegalArgumentException("Unknown step transition: " + target);
        }
    }
}
