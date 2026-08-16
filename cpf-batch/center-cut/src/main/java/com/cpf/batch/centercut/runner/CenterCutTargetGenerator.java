package com.cpf.batch.centercut.runner;

import com.cpf.batch.runtime.CenterCutParameterProtector;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.spi.CenterCutTargetProvider;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

/**
 * Center-Cut 대상을 메모리에 전량 적재하지 않고 Provider cursor/chunk 단위로 생성합니다.
 * 생성된 Item은 execution별 업무키 멱등성, 독립 transaction segment와 원 실행 parent segment를 가집니다.
 */
@Component
public class CenterCutTargetGenerator {
    private final JdbcTemplate jdbc;
    private final Map<String, CenterCutTargetProvider> providers;
    private final ObjectMapper mapper;
    private final CenterCutParameterProtector protector;
    private final TransactionTemplate tx;
    private final CpfVendorSqlCatalog sql;

    public CenterCutTargetGenerator(JdbcTemplate jdbc, List<CenterCutTargetProvider> providers,
                                    ObjectMapper mapper, CenterCutParameterProtector protector,
                                    PlatformTransactionManager tm,
                                    CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.protector = protector;
        this.tx = new TransactionTemplate(tm);
        this.sql = sqlCatalogProvider.forModule("bat");
        Map<String, CenterCutTargetProvider> map = new HashMap<>();
        for (CenterCutTargetProvider provider : providers) {
            if (map.put(provider.providerKey(), provider) != null) {
                throw new IllegalStateException("Duplicate Center-Cut provider " + provider.providerKey());
            }
        }
        this.providers = Map.copyOf(map);
    }

    @Scheduled(fixedDelayString = "${cpf.center-cut.target-generation-ms:500}")
    public void generate() {
        List<Map<String, Object>> executions =
                jdbc.queryForList(sql.required("centercut-target-find-pending"));
        for (Map<String, Object> row : executions) {
            try {
                generateOne(row);
            } catch (RuntimeException failure) {
                jdbc.update(sql.required("centercut-target-mark-failed"),
                        SensitiveTextSanitizer.sanitize(failure.getMessage()),
                        row.get("center_cut_execution_id"));
            }
        }
    }

    private void generateOne(Map<String, Object> row) {
        String executionId = String.valueOf(row.get("center_cut_execution_id"));
        String jobId = String.valueOf(row.get("center_cut_job_id"));
        CenterCutTargetProvider provider = providers.get(String.valueOf(row.get("provider_key")));
        if (provider == null) throw new IllegalStateException("Center-Cut provider not loaded");

        String plain = protector.unprotect(String.valueOf(row.get("parameter_ciphertext")));
        if (!protector.sha256(plain).equalsIgnoreCase(String.valueOf(row.get("parameter_hash")))) {
            throw new SecurityException("Center-Cut parameter hash mismatch");
        }
        Map<String, Object> parameters;
        try {
            parameters = mapper.readValue(plain, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Center-Cut parameter JSON invalid", e);
        }

        String cursor = Objects.toString(row.get("target_cursor"), null);
        int limit = Math.max(1, ((Number) row.get("chunk_size")).intValue());
        List<CenterCutTargetProvider.Target> returned = provider.next(jobId, executionId, cursor, limit, parameters);
        List<CenterCutTargetProvider.Target> targets = validatePage(cursor, limit, returned);

        tx.executeWithoutResult(status -> {
            jdbc.update(sql.required("centercut-target-mark-targeting"), executionId);

            String nextCursor = cursor;
            boolean last = targets.isEmpty();
            long inserted = 0L;
            for (CenterCutTargetProvider.Target target : targets) {
                if (target == null || target.businessKey() == null || target.businessKey().isBlank()) {
                    throw new IllegalArgumentException("Center-Cut target businessKey is required");
                }
                nextCursor = target.cursor();
                last |= target.last();
                String segmentId = UUID.randomUUID().toString();
                inserted += jdbc.update(sql.required("centercut-target-insert-item-idempotent"),
                        executionId, target.businessKey(), segmentId, target.payload(), executionId);
            }

            jdbc.update(sql.required("centercut-target-update-progress"),
                    nextCursor, last ? "Y" : "N", inserted,
                    last ? "RUNNING" : "TARGETING", executionId);
        });
    }
    static List<CenterCutTargetProvider.Target> validatePage(
            String currentCursor, int limit, List<CenterCutTargetProvider.Target> returned) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        List<CenterCutTargetProvider.Target> targets = returned == null ? List.of() : List.copyOf(returned);
        if (targets.size() > limit) {
            throw new IllegalStateException("BATCH_CENTER_CUT_PROVIDER_PAGE_LIMIT_EXCEEDED");
        }

        Set<String> businessKeys = new HashSet<>();
        String previousCursor = currentCursor;
        for (int index = 0; index < targets.size(); index++) {
            CenterCutTargetProvider.Target target = targets.get(index);
            if (target == null || target.businessKey() == null || target.businessKey().isBlank()) {
                throw new IllegalArgumentException("Center-Cut target businessKey is required");
            }
            if (!businessKeys.add(target.businessKey())) {
                throw new IllegalArgumentException("BATCH_CENTER_CUT_DUPLICATE_BUSINESS_KEY");
            }
            if (target.last() && index != targets.size() - 1) {
                throw new IllegalArgumentException("BATCH_CENTER_CUT_EARLY_LAST_MARKER");
            }
            String nextCursor = target.cursor();
            if (!target.last() && (nextCursor == null || nextCursor.isBlank())) {
                throw new IllegalArgumentException("BATCH_CENTER_CUT_MISSING_CONTINUATION_CURSOR");
            }
            if (nextCursor != null && !nextCursor.isBlank() && Objects.equals(previousCursor, nextCursor)) {
                throw new IllegalStateException("BATCH_CENTER_CUT_NON_ADVANCING_CURSOR");
            }
            if (nextCursor != null && !nextCursor.isBlank()) previousCursor = nextCursor;
        }
        return targets;
    }

}
