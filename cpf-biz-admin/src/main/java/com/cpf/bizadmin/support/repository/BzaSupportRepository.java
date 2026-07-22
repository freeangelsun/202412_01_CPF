package cpf.bza.support.repository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** BZA 알림·첨부·저장 검색·다운로드 감사·권한 분석 데이터를 관리합니다. */
@Repository
public class BzaSupportRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;

    public BzaSupportRepository(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    public Map<String, Object> dashboard(String loginId) {
        return jdbc().queryForMap("""
                SELECT (SELECT COUNT(*) FROM bza_admin_user WHERE use_yn = 'Y') AS activeUserCount,
                       (SELECT COUNT(*) FROM bza_employee WHERE use_yn = 'Y' AND employment_status = 'ACTIVE') AS activeEmployeeCount,
                       (SELECT COUNT(*) FROM bza_approval_document WHERE approval_status = 'IN_REVIEW') AS pendingApprovalCount,
                       (SELECT COUNT(*) FROM bza_notification
                         WHERE recipient_login_id = :loginId AND read_yn = 'N' AND use_yn = 'Y') AS unreadNotificationCount,
                       (SELECT COUNT(*) FROM bza_business_audit
                         WHERE created_at >= CURRENT_DATE) AS todayAuditCount
                """, new MapSqlParameterSource("loginId", loginId));
    }

    public List<Map<String, Object>> findNotifications(String loginId, boolean unreadOnly, int limit) {
        return jdbc().queryForList("""
                SELECT notification_id AS notificationId, notification_type AS notificationType,
                       title, message_body AS messageBody, reference_type AS referenceType,
                       reference_id AS referenceId, read_yn AS readYn, read_at AS readAt,
                       created_at AS createdAt
                  FROM bza_notification
                 WHERE recipient_login_id = :loginId
                   AND use_yn = 'Y'
                   AND (:unreadOnly = 'N' OR read_yn = 'N')
                 ORDER BY notification_id DESC
                 LIMIT :limit
                """, new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("unreadOnly", unreadOnly ? "Y" : "N")
                .addValue("limit", limit));
    }

    public long insertNotification(Map<String, ?> values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update("""
                INSERT INTO bza_notification (
                    recipient_login_id, notification_type, title, message_body,
                    reference_type, reference_id, read_yn, use_yn, created_by, updated_by
                ) VALUES (
                    :recipientLoginId, :notificationType, :title, :messageBody,
                    :referenceType, :referenceId, 'N', 'Y', :requestUser, :requestUser
                )
                """, new MapSqlParameterSource(values), keyHolder, new String[]{"notification_id"});
        return requiredKey(keyHolder, "notification_id");
    }

    public int markNotificationRead(long notificationId, String loginId, String requestUser) {
        return jdbc().update("""
                UPDATE bza_notification
                   SET read_yn = 'Y', read_at = CURRENT_TIMESTAMP,
                       updated_by = :requestUser, updated_at = CURRENT_TIMESTAMP
                 WHERE notification_id = :notificationId
                   AND recipient_login_id = :loginId
                   AND use_yn = 'Y'
                """, new MapSqlParameterSource()
                .addValue("notificationId", notificationId)
                .addValue("loginId", loginId)
                .addValue("requestUser", requestUser));
    }

    public List<Map<String, Object>> findAttachments(String groupId) {
        return jdbc().queryForList("""
                SELECT attachment_id AS attachmentId, attachment_group_id AS attachmentGroupId,
                       original_file_name AS originalFileName, content_type AS contentType,
                       file_size AS fileSize, checksum_sha256 AS checksumSha256,
                       scan_status AS scanStatus, created_by AS createdBy, created_at AS createdAt
                  FROM bza_attachment
                 WHERE attachment_group_id = :groupId AND use_yn = 'Y'
                 ORDER BY attachment_id
                """, new MapSqlParameterSource("groupId", groupId));
    }

    public Optional<Map<String, Object>> findAttachment(long attachmentId) {
        return jdbc().queryForList("""
                SELECT attachment_id AS attachmentId, attachment_group_id AS attachmentGroupId,
                       original_file_name AS originalFileName, stored_file_name AS storedFileName,
                       storage_key AS storageKey, content_type AS contentType, file_size AS fileSize,
                       checksum_sha256 AS checksumSha256, scan_status AS scanStatus, use_yn AS useYn
                  FROM bza_attachment
                 WHERE attachment_id = :attachmentId
                """, new MapSqlParameterSource("attachmentId", attachmentId)).stream().findFirst();
    }

    public long insertAttachment(Map<String, ?> values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update("""
                INSERT INTO bza_attachment (
                    attachment_group_id, original_file_name, stored_file_name, storage_key,
                    content_type, file_size, checksum_sha256, scan_status, use_yn,
                    created_by, updated_by
                ) VALUES (
                    :attachmentGroupId, :originalFileName, :storedFileName, :storageKey,
                    :contentType, :fileSize, :checksumSha256, :scanStatus, 'Y',
                    :requestUser, :requestUser
                )
                """, new MapSqlParameterSource(values), keyHolder, new String[]{"attachment_id"});
        return requiredKey(keyHolder, "attachment_id");
    }

    public List<Map<String, Object>> findSavedSearches(String loginId, String screenCode) {
        return jdbc().queryForList("""
                SELECT saved_search_id AS savedSearchId, screen_code AS screenCode,
                       search_name AS searchName, criteria_json AS criteriaJson,
                       shared_yn AS sharedYn, created_by AS createdBy, updated_at AS updatedAt
                  FROM bza_saved_search
                 WHERE use_yn = 'Y'
                   AND (:screenCode IS NULL OR screen_code = :screenCode)
                   AND (owner_login_id = :loginId OR shared_yn = 'Y')
                 ORDER BY screen_code, search_name
                """, new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("screenCode", screenCode));
    }

    public Optional<Map<String, Object>> findSavedSearch(String loginId, String screenCode, String searchName) {
        return jdbc().queryForList("""
                SELECT saved_search_id AS savedSearchId, owner_login_id AS ownerLoginId,
                       screen_code AS screenCode, search_name AS searchName,
                       criteria_json AS criteriaJson, shared_yn AS sharedYn, use_yn AS useYn
                  FROM bza_saved_search
                 WHERE owner_login_id = :loginId AND screen_code = :screenCode AND search_name = :searchName
                """, new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("screenCode", screenCode)
                .addValue("searchName", searchName)).stream().findFirst();
    }

    public void saveSavedSearch(Map<String, ?> values) {
        jdbc().update("""
                INSERT INTO bza_saved_search (
                    owner_login_id, screen_code, search_name, criteria_json,
                    shared_yn, use_yn, created_by, updated_by
                ) VALUES (
                    :ownerLoginId, :screenCode, :searchName, :criteriaJson,
                    :sharedYn, 'Y', :requestUser, :requestUser
                )
                ON DUPLICATE KEY UPDATE
                    criteria_json = VALUES(criteria_json), shared_yn = VALUES(shared_yn), use_yn = 'Y',
                    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
                """, values);
    }

    public int disableSavedSearch(long savedSearchId, String loginId, String requestUser) {
        return jdbc().update("""
                UPDATE bza_saved_search
                   SET use_yn = 'N', updated_by = :requestUser, updated_at = CURRENT_TIMESTAMP
                 WHERE saved_search_id = :savedSearchId AND owner_login_id = :loginId AND use_yn = 'Y'
                """, new MapSqlParameterSource()
                .addValue("savedSearchId", savedSearchId)
                .addValue("loginId", loginId)
                .addValue("requestUser", requestUser));
    }

    public List<Map<String, Object>> findDownloadAudits(int limit) {
        return jdbc().queryForList("""
                SELECT download_audit_id AS downloadAuditId, actor_id AS actorId,
                       download_code AS downloadCode, reason, filter_json AS filterJson,
                       row_count AS rowCount, result_status AS resultStatus, file_name AS fileName,
                       masking_applied_yn AS maskingAppliedYn,
                       transaction_global_id AS transactionGlobalId, created_at AS createdAt
                  FROM bza_download_audit
                 ORDER BY download_audit_id DESC
                 LIMIT :limit
                """, new MapSqlParameterSource("limit", limit));
    }

    public void insertDownloadAudit(Map<String, ?> values) {
        jdbc().update("""
                INSERT INTO bza_download_audit (
                    actor_id, download_code, reason, filter_json, row_count,
                    result_status, file_name, masking_applied_yn, transaction_global_id,
                    created_by, updated_by
                ) VALUES (
                    :actorId, :downloadCode, :reason, :filterJson, :rowCount,
                    :resultStatus, :fileName, :maskingAppliedYn, :transactionGlobalId,
                    :actorId, :actorId
                )
                """, values);
    }

    public List<Map<String, Object>> findRolePermissions(List<String> roleCodes) {
        return jdbc().queryForList("""
                SELECT role_code AS roleCode, menu_code AS menuCode, button_code AS buttonCode,
                       permission_type AS permissionType, http_method AS httpMethod,
                       api_pattern AS apiPattern, domain_code AS domainCode,
                       environment_code AS environmentCode, data_scope AS dataScope,
                       allow_yn AS allowYn, use_yn AS useYn
                  FROM bza_permission
                 WHERE role_code IN (:roleCodes) AND use_yn = 'Y'
                 ORDER BY menu_code, button_code, role_code
                """, new MapSqlParameterSource("roleCodes", roleCodes));
    }

    public void insertBusinessAudit(Map<String, ?> values) {
        jdbc().update("""
                INSERT INTO bza_business_audit (
                    transaction_global_id, actor_id, action_type, target_type, target_id,
                    reason, before_data, after_data, created_by, updated_by
                ) VALUES (
                    :transactionGlobalId, :actorId, :actionType, :targetType, :targetId,
                    :reason, :beforeData, :afterData, :actorId, :actorId
                )
                """, values);
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
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "BZA DB datasource가 비활성화되어 지원 기능 저장소를 사용할 수 없습니다.");
        }
        return jdbcTemplate;
    }
}
