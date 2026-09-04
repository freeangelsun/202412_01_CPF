package com.cpf.backoffice.online.audit.service;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.backoffice.online.base.BackofficeBaseService;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.security.api.CpfSensitiveData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import com.cpf.foundation.annotation.CpfService;

/**
 * MBW 업무 감사의 tamper-evident hash chain 구현.
 *
 * <p>감사 기록 writer는 단일 JVM lock이 아니라 DB lock row를 SELECT FOR UPDATE 하므로 여러 MBW 인스턴스가 동시에 기록해도 이전
 * hash와 현재 hash의 순서를 하나의 체인으로 유지합니다.
 */
@CpfService
public class BackofficeBusinessAuditService extends BackofficeBaseService {
  private static final long LOCK_ID = 1L;
  private static final String GENESIS = "GENESIS";
  private final ObjectProvider<NamedParameterJdbcTemplate> provider;
  private final ObjectMapper mapper;
  private final CpfVendorSqlCatalog sql;

  public BackofficeBusinessAuditService(
      @Qualifier("MBW_JDBC_TEMPLATE") ObjectProvider<NamedParameterJdbcTemplate> provider,
      ObjectMapper mapper,
      CpfVendorSqlCatalogProvider sqlCatalogProvider) {
    this.provider = provider;
    this.mapper = mapper;
    this.sql = sqlCatalogProvider.forModule("backoffice");
  }

  @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
  public Map<String, Object> record(
      String actor,
      String action,
      String targetType,
      String targetId,
      String reason,
      Object before,
      Object after) {
    require(actor, "actor");
    require(action, "action");
    require(targetType, "targetType");
    require(targetId, "targetId");
    String safeReason = CpfSensitiveData.sanitizeAuditReason(reason);
    ensureLockRow();
    Map<String, Object> lock =
        jdbc()
            .queryForMap(
                sql.required("business-audit-service-record-02"),
                new MapSqlParameterSource("id", LOCK_ID));
    String previous = Objects.toString(lock.get("currentHash"), GENESIS);
    String transactionId = CpfContexts.transactionId();
    String beforeJson = canonical(before), afterJson = canonical(after);
    String hash =
        sha256(
            String.join(
                "|",
                previous,
                nullable(transactionId),
                actor,
                action,
                targetType,
                targetId,
                safeReason,
                nullable(beforeJson),
                nullable(afterJson)));
    MapSqlParameterSource p =
        new MapSqlParameterSource()
            .addValue("transactionId", transactionId)
            .addValue("actor", actor)
            .addValue("action", action)
            .addValue("targetType", targetType)
            .addValue("targetId", targetId)
            .addValue("reason", safeReason)
            .addValue("beforeData", beforeJson)
            .addValue("afterData", afterJson)
            .addValue("previous", previous)
            .addValue("hash", hash);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc()
        .update(
            sql.required("business-audit-service-record-01"),
            p,
            keyHolder,
            new String[] {"audit_id"});
    Number generated = keyHolder.getKey();
    if (generated == null) throw new IllegalStateException("MBW 감사 기록 키를 확인할 수 없습니다.");
    Long auditId = generated.longValue();
    jdbc()
        .update(
            sql.required("business-audit-service-record-03"),
            new MapSqlParameterSource()
                .addValue("hash", hash)
                .addValue("auditId", auditId)
                .addValue("actor", actor)
                .addValue("id", LOCK_ID));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("recordHash", hash);
    result.put("previousRecordHash", previous);
    result.put("transactionId", transactionId);
    return result;
  }

  /**
   * MBW 전체에서 하나뿐인 감사 체인 lock을 소유한 상태로 초기 설치 같은 singleton 작업을 수행한다.
   *
   * <p>별도의 process-local lock이나 profile 전용 table을 만들지 않는다. 이 method를 호출한
   * transaction 안에서 {@link #record(String, String, String, String, String, Object, Object)}도
   * 같은 DB lock을 재사용하므로 최초 운영자 생성과 감사 기록을 하나의 MBW transaction으로 묶을 수 있다.</p>
   */
  @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
  public <T> T withAuditChainLock(Supplier<T> action) {
    Objects.requireNonNull(action, "action");
    ensureLockRow();
    jdbc().queryForMap(
        sql.required("business-audit-service-record-02"),
        new MapSqlParameterSource("id", LOCK_ID));
    return action.get();
  }

  /** 전체 체인과 lock-head를 모두 검증한다. 과거 legacy row는 PARTIAL_LEGACY로 구분한다. */
  public Map<String, Object> verify() {
    ensureLockRow();
    List<Map<String, Object>> rows =
        jdbc().queryForList(sql.required("business-audit-service-verify-01"), Map.of());
    String previous = GENESIS;
    boolean legacy = false;
    long verified = 0;
    for (Map<String, Object> row : rows) {
      String actual = Objects.toString(row.get("recordHash"), "");
      String prev = Objects.toString(row.get("previousRecordHash"), "");
      if (actual.isBlank() || prev.isBlank()) {
        legacy = true;
        continue;
      }
      if (!Objects.equals(previous, prev))
        return result("BROKEN", verified, row.get("auditId"), "previous hash mismatch", previous);
      String expected =
          sha256(
              String.join(
                  "|",
                  previous,
                  nullable(row.get("transactionId")),
                  nullable(row.get("actorId")),
                  nullable(row.get("actionType")),
                  nullable(row.get("targetType")),
                  nullable(row.get("targetId")),
                  nullable(row.get("reason")),
                  nullable(row.get("beforeData")),
                  nullable(row.get("afterData"))));
      if (!expected.equalsIgnoreCase(actual))
        return result("BROKEN", verified, row.get("auditId"), "record hash mismatch", previous);
      previous = actual;
      verified++;
    }
    String head =
        Objects.toString(
            jdbc()
                .queryForObject(
                    sql.required("business-audit-service-verify-02"),
                    new MapSqlParameterSource("id", LOCK_ID),
                    String.class),
            GENESIS);
    if (!Objects.equals(previous, head))
      return result("BROKEN", verified, null, "chain head mismatch", previous);
    return result(
        legacy ? "PARTIAL_LEGACY" : "VALID",
        verified,
        null,
        legacy ? "legacy rows without hash exist" : "ok",
        previous);
  }

  private Map<String, Object> result(
      String status, long count, Object auditId, String message, String head) {
    Map<String, Object> r = new LinkedHashMap<>();
    r.put("status", status);
    r.put("verifiedRows", count);
    r.put("brokenAuditId", auditId);
    r.put("message", message);
    r.put("computedHead", head);
    return r;
  }

  private void ensureLockRow() {
    Long count =
        jdbc()
            .queryForObject(
                sql.required("business-audit-service-ensure-lock-row-01"),
                new MapSqlParameterSource("id", LOCK_ID),
                Long.class);
    if (count != null && count > 0) return;
    try {
      jdbc()
          .update(
              sql.required("business-audit-service-ensure-lock-row-02"),
              new MapSqlParameterSource().addValue("id", LOCK_ID).addValue("hash", GENESIS));
    } catch (DuplicateKeyException ignored) {
      // 다른 인스턴스가 동시에 최초 row를 만든 정상 경합입니다.
    }
  }

  private String canonical(Object value) {
    if (value == null) return null;
    try {
      JsonNode tree = mapper.valueToTree(value);
      redact(tree, null);
      return mapper.writeValueAsString(tree);
    } catch (IllegalArgumentException | JsonProcessingException e) {
      throw new IllegalArgumentException("감사 Snapshot JSON 직렬화 실패", e);
    }
  }

  private void redact(JsonNode node, String fieldName) {
    if (node == null) return;
    if (node instanceof ObjectNode object) {
      List<String> names = new ArrayList<>();
      object.fieldNames().forEachRemaining(names::add);
      for (String name : names) {
        JsonNode child = object.get(name);
        String key = name.toLowerCase(Locale.ROOT);
        if (isSecret(key)) object.put(name, "[REDACTED]");
        else if (isPii(key)) object.put(name, "[MASKED]");
        else if (child != null && child.isTextual())
          object.put(name, CpfSensitiveData.sanitizeAuditText(child.asText()));
        else redact(child, name);
      }
    } else if (node instanceof ArrayNode array) {
      for (int i = 0; i < array.size(); i++) {
        JsonNode child = array.get(i);
        if (child != null && child.isTextual())
          array.set(
              i,
              mapper.getNodeFactory().textNode(CpfSensitiveData.sanitizeAuditText(child.asText())));
        else redact(child, fieldName);
      }
    }
  }

  private boolean isPii(String key) {
    return key.contains("email")
        || key.contains("mobile")
        || key.contains("phone")
        || key.contains("contact")
        || key.contains("address")
        || key.contains("resident")
        || key.equals("rrn")
        || key.equals("ssn");
  }

  private boolean isSecret(String key) {
    return key.contains("password")
        || key.contains("secret")
        || key.contains("token")
        || key.contains("credential")
        || key.contains("authorization")
        || key.contains("apikey")
        || key.contains("privatekey")
        || key.contains("attachment");
  }

  private static String sha256(String text) {
    try {
      byte[] b = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
      return java.util.HexFormat.of().formatHex(b);
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }

  private static String nullable(Object v) {
    return v == null ? "" : String.valueOf(v);
  }

  private static void require(String v, String f) {
    if (v == null || v.isBlank()) throw new IllegalArgumentException(f + "는 필수입니다.");
  }

  private NamedParameterJdbcTemplate jdbc() {
    NamedParameterJdbcTemplate j = provider.getIfAvailable();
    if (j == null) throw new IllegalStateException("MBW datasource가 구성되지 않았습니다.");
    return j;
  }
}
