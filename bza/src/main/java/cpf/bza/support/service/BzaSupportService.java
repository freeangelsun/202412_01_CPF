package cpf.bza.support.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.bza.support.repository.BzaSupportRepository;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.attachment.CpfAttachmentContent;
import cpf.pfw.common.attachment.CpfAttachmentStoragePort;
import cpf.pfw.common.attachment.CpfStoredAttachment;
import cpf.pfw.common.exception.CpfNotFoundException;
import cpf.pfw.common.exception.CpfValidationException;
import cpf.pfw.common.logging.TransactionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** BZA 대시보드·알림·첨부·저장 검색·다운로드 감사·권한 분석을 담당합니다. */
@Service
public class BzaSupportService {
    private static final Set<String> DOWNLOADABLE_SCAN_STATUSES = Set.of("CLEAN", "PASSED_LOCAL_POLICY");

    private final BzaSupportRepository repository;
    private final CpfAttachmentStoragePort attachmentStoragePort;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public BzaSupportService(
            BzaSupportRepository repository,
            CpfAttachmentStoragePort attachmentStoragePort,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.attachmentStoragePort = attachmentStoragePort;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> dashboard(String operatorId) {
        return repository.dashboard(required(operatorId, "operatorId"));
    }

    public List<Map<String, Object>> findNotifications(String operatorId, boolean unreadOnly, int limit) {
        return repository.findNotifications(
                required(operatorId, "operatorId"),
                unreadOnly,
                boundedLimit(limit));
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> createNotification(NotificationRequest request, String operatorId) {
        String actor = required(operatorId, "operatorId");
        String recipient = required(request.recipientLoginId(), "recipientLoginId");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("recipientLoginId", recipient);
        values.put("notificationType", code(defaultText(request.notificationType(), "BUSINESS"), "notificationType"));
        values.put("title", limited(request.title(), "title", 200));
        values.put("messageBody", limited(request.messageBody(), "messageBody", 2_000));
        values.put("referenceType", blankToNull(request.referenceType()));
        values.put("referenceId", blankToNull(request.referenceId()));
        values.put("requestUser", actor);
        long id = repository.insertNotification(values);
        values.put("notificationId", id);
        audit(actor, "NOTIFICATION_CREATE", "bza_notification", String.valueOf(id),
                required(request.reason(), "reason"), null, values);
        return values;
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> markNotificationRead(long notificationId, String reason, String operatorId) {
        String actor = required(operatorId, "operatorId");
        int updated = repository.markNotificationRead(notificationId, actor, actor);
        if (updated != 1) {
            throw new CpfNotFoundException("읽음 처리할 알림을 찾을 수 없습니다. notificationId=" + notificationId);
        }
        Map<String, Object> after = Map.of("notificationId", notificationId, "readYn", "Y");
        audit(actor, "NOTIFICATION_READ", "bza_notification", String.valueOf(notificationId),
                required(reason, "reason"), null, after);
        return after;
    }

    public List<Map<String, Object>> findAttachments(String groupId) {
        return repository.findAttachments(safeGroupId(groupId));
    }

    /** 파일 저장 성공 후 DB 적재가 실패하면 저장 파일을 보상 삭제합니다. */
    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> storeAttachment(
            String groupId,
            String originalFileName,
            String contentType,
            byte[] content,
            String reason,
            String operatorId) {
        String actor = required(operatorId, "operatorId");
        CpfStoredAttachment stored = attachmentStoragePort.store(
                safeGroupId(groupId), originalFileName, contentType, content);
        try {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("attachmentGroupId", groupId);
            values.put("originalFileName", stored.originalFileName());
            values.put("storedFileName", stored.storedFileName());
            values.put("storageKey", stored.storageKey());
            values.put("contentType", stored.contentType());
            values.put("fileSize", stored.fileSize());
            values.put("checksumSha256", stored.checksumSha256());
            values.put("scanStatus", "PASSED_LOCAL_POLICY");
            values.put("requestUser", actor);
            long id = repository.insertAttachment(values);
            Map<String, Object> response = publicAttachment(values, id);
            audit(actor, "ATTACHMENT_UPLOAD", "bza_attachment", String.valueOf(id),
                    required(reason, "reason"), null, response);
            return response;
        } catch (RuntimeException ex) {
            attachmentStoragePort.delete(stored.storageKey());
            throw ex;
        }
    }

    public AttachmentDownload downloadAttachment(long attachmentId, String reason, String operatorId) {
        String actor = required(operatorId, "operatorId");
        String resolvedReason = required(reason, "reason");
        Map<String, Object> metadata = repository.findAttachment(attachmentId)
                .orElseThrow(() -> new CpfNotFoundException("첨부파일을 찾을 수 없습니다. attachmentId=" + attachmentId));
        String scanStatus = text(metadata, "scanStatus");
        if (!DOWNLOADABLE_SCAN_STATUSES.contains(scanStatus)) {
            throw new CpfValidationException("보안 검사 완료 전에는 첨부파일을 다운로드할 수 없습니다. scanStatus=" + scanStatus);
        }
        String storageKey = text(metadata, "storageKey");
        String fileName = text(metadata, "originalFileName");
        try {
            CpfAttachmentContent content = attachmentStoragePort.read(storageKey);
            if (!content.checksumSha256().equalsIgnoreCase(text(metadata, "checksumSha256"))) {
                recordDownload(actor, "ATTACHMENT", resolvedReason, fileName, "CHECKSUM_MISMATCH", 0, false);
                throw new CpfValidationException("첨부파일 checksum이 메타 정보와 일치하지 않습니다.");
            }
            recordDownload(actor, "ATTACHMENT", resolvedReason, fileName, "SUCCESS", 1, false);
            audit(actor, "ATTACHMENT_DOWNLOAD", "bza_attachment", String.valueOf(attachmentId),
                    resolvedReason, null, Map.of("fileName", fileName, "checksumSha256", content.checksumSha256()));
            return new AttachmentDownload(
                    fileName,
                    text(metadata, "contentType"),
                    content.bytes(),
                    content.checksumSha256());
        } catch (RuntimeException ex) {
            if (!(ex instanceof CpfValidationException
                    && ex.getMessage() != null
                    && ex.getMessage().contains("checksum"))) {
                recordDownload(actor, "ATTACHMENT", resolvedReason, fileName, "FAILED", 0, false);
            }
            throw ex;
        }
    }

    public List<Map<String, Object>> findSavedSearches(String screenCode, String operatorId) {
        return repository.findSavedSearches(
                required(operatorId, "operatorId"),
                blankToNull(screenCode) == null ? null : code(screenCode, "screenCode"));
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveSavedSearch(SavedSearchRequest request, String operatorId) {
        String actor = required(operatorId, "operatorId");
        String screenCode = code(request.screenCode(), "screenCode");
        String searchName = limited(request.searchName(), "searchName", 120);
        Map<String, Object> before = repository.findSavedSearch(actor, screenCode, searchName).orElse(null);
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("ownerLoginId", actor);
        values.put("screenCode", screenCode);
        values.put("searchName", searchName);
        values.put("criteriaJson", canonicalObjectJson(request.criteriaJson()));
        values.put("sharedYn", yn(request.sharedYn(), "N"));
        values.put("requestUser", actor);
        repository.saveSavedSearch(values);
        audit(actor, "SAVED_SEARCH_SAVE", "bza_saved_search", screenCode + ":" + searchName,
                required(request.reason(), "reason"), before, values);
        return values;
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> disableSavedSearch(long savedSearchId, String reason, String operatorId) {
        String actor = required(operatorId, "operatorId");
        int updated = repository.disableSavedSearch(savedSearchId, actor, actor);
        if (updated != 1) {
            throw new CpfNotFoundException("삭제할 저장 검색을 찾을 수 없습니다. savedSearchId=" + savedSearchId);
        }
        Map<String, Object> after = Map.of("savedSearchId", savedSearchId, "useYn", "N");
        audit(actor, "SAVED_SEARCH_DISABLE", "bza_saved_search", String.valueOf(savedSearchId),
                required(reason, "reason"), null, after);
        return after;
    }

    public List<Map<String, Object>> findDownloadAudits(int limit) {
        return repository.findDownloadAudits(boundedLimit(limit));
    }

    public List<Map<String, Object>> compareRoles(String leftRoleCode, String rightRoleCode) {
        String left = code(leftRoleCode, "leftRoleCode");
        String right = code(rightRoleCode, "rightRoleCode");
        List<Map<String, Object>> rows = repository.findRolePermissions(List.of(left, right));
        Map<String, Map<String, Map<String, Object>>> matrix = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String key = text(row, "menuCode") + ":" + text(row, "buttonCode") + ":" + text(row, "permissionType");
            matrix.computeIfAbsent(key, ignored -> new LinkedHashMap<>())
                    .put(text(row, "roleCode"), row);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        matrix.forEach((permissionKey, roleRows) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("permissionKey", permissionKey);
            item.put("leftRoleCode", left);
            item.put("left", roleRows.get(left));
            item.put("rightRoleCode", right);
            item.put("right", roleRows.get(right));
            item.put("different", !Objects.equals(normalizedPermission(roleRows.get(left)), normalizedPermission(roleRows.get(right))));
            result.add(item);
        });
        result.sort(Comparator.comparing(item -> String.valueOf(item.get("permissionKey"))));
        return result;
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> simulatePermission(PermissionSimulationRequest request, String operatorId) {
        String actor = required(operatorId, "operatorId");
        String role = code(request.roleCode(), "roleCode");
        String menu = code(request.menuCode(), "menuCode");
        String action = code(request.actionCode(), "actionCode");
        String method = code(defaultText(request.httpMethod(), "GET"), "httpMethod");
        String path = required(request.apiPath(), "apiPath");
        String environment = code(defaultText(request.environmentCode(), "ALL"), "environmentCode");
        String domain = code(defaultText(request.domainCode(), "BZA"), "domainCode");
        List<Map<String, Object>> candidates = repository.findRolePermissions(List.of(role));
        List<Map<String, Object>> matched = candidates.stream()
                .filter(row -> menu.equalsIgnoreCase(text(row, "menuCode")))
                .filter(row -> action.equalsIgnoreCase(text(row, "buttonCode")))
                .filter(row -> matchesScope(row, method, path, environment, domain))
                .toList();
        boolean allowed = matched.stream().anyMatch(row -> "Y".equalsIgnoreCase(text(row, "allowYn")));
        Set<String> dataScopes = new LinkedHashSet<>();
        matched.forEach(row -> dataScopes.add(text(row, "dataScope")));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("roleCode", role);
        result.put("menuCode", menu);
        result.put("actionCode", action);
        result.put("httpMethod", method);
        result.put("apiPath", path);
        result.put("environmentCode", environment);
        result.put("domainCode", domain);
        result.put("allowed", allowed);
        result.put("dataScopes", dataScopes);
        result.put("matchedRules", matched);
        audit(actor, "PERMISSION_SIMULATE", "bza_permission", role + ":" + menu + ":" + action,
                required(request.reason(), "reason"), null, result);
        return result;
    }

    private boolean matchesScope(
            Map<String, Object> row,
            String method,
            String path,
            String environment,
            String domain) {
        String ruleMethod = text(row, "httpMethod");
        String rulePath = text(row, "apiPattern");
        String ruleEnvironment = text(row, "environmentCode");
        String ruleDomain = text(row, "domainCode");
        return (ruleMethod == null || "ALL".equalsIgnoreCase(ruleMethod) || method.equalsIgnoreCase(ruleMethod))
                && (rulePath == null || pathMatcher.match(rulePath, path))
                && (ruleEnvironment == null || "ALL".equalsIgnoreCase(ruleEnvironment)
                        || environment.equalsIgnoreCase(ruleEnvironment))
                && (ruleDomain == null || "ALL".equalsIgnoreCase(ruleDomain) || domain.equalsIgnoreCase(ruleDomain));
    }

    private Map<String, Object> normalizedPermission(Map<String, Object> row) {
        if (row == null) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.remove("roleCode");
        return result;
    }

    private Map<String, Object> publicAttachment(Map<String, Object> values, long attachmentId) {
        Map<String, Object> response = new LinkedHashMap<>(values);
        response.put("attachmentId", attachmentId);
        response.remove("storageKey");
        response.remove("storedFileName");
        response.remove("requestUser");
        return response;
    }

    private void recordDownload(
            String actor,
            String downloadCode,
            String reason,
            String fileName,
            String status,
            int rowCount,
            boolean maskingApplied) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("actorId", actor);
        values.put("downloadCode", downloadCode);
        values.put("reason", reason);
        values.put("filterJson", null);
        values.put("rowCount", rowCount);
        values.put("resultStatus", status);
        values.put("fileName", fileName);
        values.put("maskingAppliedYn", maskingApplied ? "Y" : "N");
        values.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        repository.insertDownloadAudit(values);
    }

    private void audit(
            String actor,
            String action,
            String targetType,
            String targetId,
            String reason,
            Object before,
            Object after) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        values.put("actorId", actor);
        values.put("actionType", action);
        values.put("targetType", targetType);
        values.put("targetId", targetId);
        values.put("reason", reason);
        values.put("beforeData", before == null ? null : String.valueOf(before));
        values.put("afterData", after == null ? null : String.valueOf(after));
        repository.insertBusinessAudit(values);
    }

    private String canonicalObjectJson(String value) {
        String json = limited(value, "criteriaJson", 10_000);
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node == null || !node.isObject()) {
                throw new CpfValidationException("criteriaJson은 JSON object여야 합니다.");
            }
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new CpfValidationException("criteriaJson이 올바른 JSON이 아닙니다.");
        }
    }

    private String safeGroupId(String value) {
        String groupId = required(value, "groupId");
        if (!groupId.matches("[A-Za-z0-9_-]{1,80}")) {
            throw new CpfValidationException("groupId는 영문·숫자·밑줄·하이픈 80자 이하여야 합니다.");
        }
        return groupId;
    }

    private String limited(String value, String field, int maxLength) {
        String resolved = required(value, field);
        if (resolved.length() > maxLength) {
            throw new CpfValidationException(field + "는 " + maxLength + "자를 초과할 수 없습니다.");
        }
        return resolved;
    }

    private String required(String value, String field) {
        return TextUtils.requireText(value, field);
    }

    private String code(String value, String field) {
        return required(value, field).toUpperCase(Locale.ROOT);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String yn(String value, String fallback) {
        String resolved = code(defaultText(value, fallback), "yn");
        if (!Set.of("Y", "N").contains(resolved)) {
            throw new CpfValidationException("Y/N 값이 올바르지 않습니다. value=" + value);
        }
        return resolved;
    }

    private int boundedLimit(int limit) {
        return Math.max(1, Math.min(limit, 500));
    }

    private String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toUpperCase(Locale.ROOT));
        }
        return value == null ? null : String.valueOf(value);
    }

    public record NotificationRequest(
            String recipientLoginId,
            String notificationType,
            String title,
            String messageBody,
            String referenceType,
            String referenceId,
            String reason) {
    }

    public record SavedSearchRequest(
            String screenCode,
            String searchName,
            String criteriaJson,
            String sharedYn,
            String reason) {
    }

    public record PermissionSimulationRequest(
            String roleCode,
            String menuCode,
            String actionCode,
            String httpMethod,
            String apiPath,
            String environmentCode,
            String domainCode,
            String reason) {
    }

    public record AttachmentDownload(
            String fileName,
            String contentType,
            byte[] content,
            String checksumSha256) {

        public AttachmentDownload {
            content = content == null ? new byte[0] : content.clone();
        }

        @Override
        public byte[] content() {
            return content.clone();
        }
    }
}
