package com.cpf.batch.centercut.runner;

import com.cpf.batch.runtime.CenterCutParameterProtector;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.spi.CenterCutTargetProvider;
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

    public CenterCutTargetGenerator(JdbcTemplate jdbc, List<CenterCutTargetProvider> providers,
                                    ObjectMapper mapper, CenterCutParameterProtector protector,
                                    PlatformTransactionManager tm) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.protector = protector;
        this.tx = new TransactionTemplate(tm);
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
        List<Map<String, Object>> executions = jdbc.queryForList("""
            SELECT e.center_cut_execution_id,e.center_cut_job_id,e.parameter_ciphertext,e.parameter_hash,
                   e.target_cursor,e.transaction_id,e.parent_segment_id,j.provider_key,j.chunk_size
              FROM bat_center_cut_execution e
              JOIN bat_center_cut_job j ON j.center_cut_job_id=e.center_cut_job_id
             WHERE e.execution_state IN ('CREATED','TARGETING') AND e.target_complete_yn='N'
             ORDER BY e.created_at
             LIMIT 20
            """);
        for (Map<String, Object> row : executions) {
            try {
                generateOne(row);
            } catch (RuntimeException failure) {
                jdbc.update("""
                    UPDATE bat_center_cut_execution
                       SET execution_state='FAILED',last_error_message=?,updated_at=CURRENT_TIMESTAMP(6)
                     WHERE center_cut_execution_id=?
                    """, SensitiveTextSanitizer.sanitize(failure.getMessage()), row.get("center_cut_execution_id"));
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
        List<CenterCutTargetProvider.Target> targets = returned == null ? List.of() : List.copyOf(returned);

        tx.executeWithoutResult(status -> {
            jdbc.update("""
                UPDATE bat_center_cut_execution
                   SET execution_state='TARGETING',updated_at=CURRENT_TIMESTAMP(6)
                 WHERE center_cut_execution_id=? AND execution_state='CREATED'
                """, executionId);

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
                inserted += jdbc.update("""
                    INSERT IGNORE INTO bat_center_cut_item(
                        center_cut_job_id,center_cut_execution_id,business_key,item_status,
                        transaction_id,transaction_segment_id,parent_segment_id,item_payload,created_by,updated_by)
                    SELECT center_cut_job_id,?,?,'READY',transaction_id,?,parent_segment_id,?,
                           'CENTER_CUT_TARGET','CENTER_CUT_TARGET'
                      FROM bat_center_cut_execution
                     WHERE center_cut_execution_id=?
                    """, executionId, target.businessKey(), segmentId, target.payload(), executionId);
            }

            jdbc.update("""
                UPDATE bat_center_cut_execution
                   SET target_cursor=?,target_complete_yn=?,target_count=target_count+?,
                       execution_state=?,updated_at=CURRENT_TIMESTAMP(6)
                 WHERE center_cut_execution_id=?
                """, nextCursor, last ? "Y" : "N", inserted, last ? "RUNNING" : "TARGETING", executionId);
        });
    }
}
