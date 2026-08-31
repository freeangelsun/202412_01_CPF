package com.cpf.backoffice.online.support.repository;

import com.cpf.backoffice.online.base.BackofficeBaseRepository;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import com.cpf.data.persistence.api.CpfRepository;
import org.springframework.web.server.ResponseStatusException;

/** MBW 알림·첨부·저장 검색·다운로드 감사·권한 분석 데이터를 관리합니다. */
@CpfRepository
public class BackofficeSupportRepository extends BackofficeBaseRepository {
  private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;
  private final CpfVendorSqlCatalog sql;

  public BackofficeSupportRepository(
      @Qualifier("MBW_JDBC_TEMPLATE") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
      CpfVendorSqlCatalogProvider sqlCatalogProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
    this.sql = sqlCatalogProvider.forModule("backoffice");
  }

  public Map<String, Object> dashboard(String loginId) {
    return jdbc()
        .queryForMap(
            sql.required("support-repository-dashboard-01"),
            new MapSqlParameterSource("loginId", loginId));
  }

  public List<Map<String, Object>> findNotifications(
      String loginId, boolean unreadOnly, int limit) {
    return jdbc()
        .queryForList(
            sql.required("support-find-notifications"),
            new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("unreadOnly", unreadOnly ? "Y" : "N")
                .addValue("limit", limit));
  }

  public long insertNotification(Map<String, ?> values) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc()
        .update(
            sql.required("support-repository-insert-notification-01"),
            new MapSqlParameterSource(values),
            keyHolder,
            new String[] {"notification_id"});
    return requiredKey(keyHolder, "notification_id");
  }

  public int markNotificationRead(long notificationId, String loginId, String requestUser) {
    return jdbc()
        .update(
            sql.required("support-repository-mark-notification-read-01"),
            new MapSqlParameterSource()
                .addValue("notificationId", notificationId)
                .addValue("loginId", loginId)
                .addValue("requestUser", requestUser));
  }

  public int markAllNotificationsRead(String loginId, String requestUser) {
    return jdbc()
        .update(
            sql.required("support-repository-mark-all-notifications-read-01"),
            new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("requestUser", requestUser));
  }

  public List<Map<String, Object>> findAttachments(String groupId) {
    return jdbc()
        .queryForList(
            sql.required("support-repository-find-attachments-01"),
            new MapSqlParameterSource("groupId", groupId));
  }

  public Optional<Map<String, Object>> findAttachment(long attachmentId) {
    return jdbc()
        .queryForList(
            sql.required("support-repository-find-attachment-01"),
            new MapSqlParameterSource("attachmentId", attachmentId))
        .stream()
        .findFirst();
  }

  public long insertAttachment(Map<String, ?> values) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc()
        .update(
            sql.required("support-repository-insert-attachment-01"),
            new MapSqlParameterSource(values),
            keyHolder,
            new String[] {"attachment_id"});
    return requiredKey(keyHolder, "attachment_id");
  }

  public int updateAttachmentSecurity(
      long attachmentId,
      String scanStatus,
      String dataClassification,
      java.time.Instant retentionUntil,
      String quarantineYn,
      String useYn,
      String requestUser) {
    return jdbc()
        .update(
            sql.required("support-repository-update-attachment-security-01"),
            new MapSqlParameterSource()
                .addValue("attachmentId", attachmentId)
                .addValue("scanStatus", scanStatus)
                .addValue("classification", dataClassification)
                .addValue(
                    "retentionUntil",
                    retentionUntil == null ? null : java.sql.Timestamp.from(retentionUntil))
                .addValue("quarantineYn", quarantineYn)
                .addValue("useYn", useYn)
                .addValue("requestUser", requestUser));
  }

  public List<Map<String, Object>> findSavedSearches(String loginId, String screenCode) {
    return jdbc()
        .queryForList(
            sql.required("support-repository-find-saved-searches-01"),
            new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("screenCode", screenCode));
  }

  public Optional<Map<String, Object>> findSavedSearch(
      String loginId, String screenCode, String searchName) {
    return jdbc()
        .queryForList(
            sql.required("support-repository-find-saved-search-01"),
            new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("screenCode", screenCode)
                .addValue("searchName", searchName))
        .stream()
        .findFirst();
  }

  public void saveSavedSearch(Map<String, ?> values) {
    jdbc().update(sql.required("support-save-saved-search"), values);
  }

  public int disableSavedSearch(long savedSearchId, String loginId, String requestUser) {
    return jdbc()
        .update(
            sql.required("support-repository-disable-saved-search-01"),
            new MapSqlParameterSource()
                .addValue("savedSearchId", savedSearchId)
                .addValue("loginId", loginId)
                .addValue("requestUser", requestUser));
  }

  public List<Map<String, Object>> findDownloadAudits(int limit) {
    return jdbc()
        .queryForList(
            sql.required("support-find-download-audits"),
            new MapSqlParameterSource("limit", limit));
  }

  public void insertDownloadAudit(Map<String, ?> values) {
    jdbc().update(sql.required("support-repository-insert-download-audit-01"), values);
  }

  public List<Map<String, Object>> findRolePermissions(List<String> roleCodes) {
    return jdbc()
        .queryForList(
            sql.required("support-repository-find-role-permissions-01"),
            new MapSqlParameterSource("roleCodes", roleCodes));
  }

  public void insertBusinessAudit(Map<String, ?> values) {
    jdbc().update(sql.required("support-repository-insert-business-audit-01"), values);
  }

  private long requiredKey(KeyHolder keyHolder, String keyName) {
    Number key = keyHolder.getKey();
    if (key == null) {
      throw new IllegalStateException(keyName + " 생성 키를 확인할 수 없습니다.");
    }
    return key.longValue();
  }

  private NamedParameterJdbcTemplate jdbc() {
    NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
    if (jdbcTemplate == null) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "MBW DB datasource가 비활성화되어 지원 기능 저장소를 사용할 수 없습니다.");
    }
    return jdbcTemplate;
  }
}
