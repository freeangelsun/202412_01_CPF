package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmMemberRoleRequest;
import com.cpf.admin.opr.dto.AdmMemberSaveRequest;
import com.cpf.admin.opr.dto.AdmMemberStatusRequest;
import com.cpf.core.api.admin.CpfOwnerAdminCommand;
import com.cpf.core.api.admin.CpfOwnerAdminOperationsPort;
import com.cpf.core.api.admin.CpfOwnerAdminQuery;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.util.CpfStrings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ADM 회원 운영 Facade입니다.
 *
 * <p>회원/역할 DB는 MBR Owner가 소유하며 ADM은 {@link CpfOwnerAdminOperationsPort}를 통해서만
 * 조회·변경합니다. ADM 소유 auditDB와 CPF 플랫폼 거래로그는 상세 화면을 위한 read-model로만 결합합니다.</p>
 */
@Service
public class AdmMemberOperationService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfOwnerAdminOperationsPort mbrOperations;
    private final JdbcTemplate cpfJdbcTemplate;
    private final JdbcTemplate admJdbcTemplate;

    public AdmMemberOperationService(
            @Qualifier("mbrOwnerAdminOperationsPort") CpfOwnerAdminOperationsPort mbrOperations,
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.mbrOperations = mbrOperations;
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.admJdbcTemplate = admJdbcTemplate;
    }

    /** 회원 검색 조건을 MBR Owner에 위임합니다. */
    public List<Map<String, Object>> findMembers(
            String memberNo,
            String customerNo,
            String loginId,
            String name,
            String email,
            String mobileNo,
            String memberStatus,
            String channelCode,
            String roleCode,
            int limit) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        put(criteria, "memberNo", memberNo);
        put(criteria, "customerNo", customerNo);
        put(criteria, "loginId", loginId);
        put(criteria, "name", name);
        put(criteria, "email", email);
        put(criteria, "mobileNo", mobileNo);
        put(criteria, "memberStatus", memberStatus);
        put(criteria, "channelCode", channelCode);
        put(criteria, "roleCode", roleCode);
        criteria.put("limit", Math.max(1, Math.min(limit, 500)));
        Map<String, Object> response = mbrOperations.query(
                new CpfOwnerAdminQuery("member", "findMembers", null, criteria));
        return list(response.get("items"));
    }

    /** 회원번호 발급 이력을 MBR Owner에서 조회합니다. */
    public List<Map<String, Object>> findMemberNoIssueHistory(
            String memberNo, String issueType, String issuedBy, int limit) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        put(criteria, "memberNo", memberNo);
        put(criteria, "issueType", issueType);
        put(criteria, "issuedBy", issuedBy);
        criteria.put("limit", Math.max(1, Math.min(limit, 500)));
        Map<String, Object> response = mbrOperations.query(
                new CpfOwnerAdminQuery("member", "findMemberNoIssueHistory", null, criteria));
        return list(response.get("items"));
    }

    /** MBR Owner 상세에 ADM/CPF read-model을 결합합니다. 부분 조회 장애를 0건으로 위장하지 않습니다. */
    public Map<String, Object> findMemberDetail(long memberId) {
        Map<String, Object> ownerDetail = mbrOperations.query(
                new CpfOwnerAdminQuery("member", "findMemberDetail", String.valueOf(memberId), Map.of()));
        Map<String, Object> detail = new LinkedHashMap<>(ownerDetail);
        Map<String, Object> member = map(ownerDetail.get("member"));
        String memberNo = value(member.get("member_no"));

        ReadSection transactionLogs = readTransactionLogs(memberNo);
        ReadSection auditLogs = readAuditLogs(String.valueOf(memberId), memberNo);
        detail.put("transactionLogs", transactionLogs.items());
        detail.put("auditLogs", auditLogs.items());
        detail.put("readModelSections", Map.of(
                "transactionLogs", transactionLogs.status(),
                "auditLogs", auditLogs.status()));
        return detail;
    }

    public Map<String, Object> createMember(AdmMemberSaveRequest request, String requestUser) {
        return mbrOperations.command(new CpfOwnerAdminCommand(
                "member", "createMember", null, memberPayload(request), requestUser, request.reason()));
    }

    public Map<String, Object> updateMember(long memberId, AdmMemberSaveRequest request, String requestUser) {
        return mbrOperations.command(new CpfOwnerAdminCommand(
                "member", "updateMember", String.valueOf(memberId), memberPayload(request), requestUser, request.reason()));
    }

    public Map<String, Object> updateStatus(long memberId, AdmMemberStatusRequest request, String requestUser) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, "memberStatus", request.memberStatus());
        put(payload, "lockYn", request.lockYn());
        put(payload, "withdrawYn", request.withdrawYn());
        payload.put("expectedVersion", request.expectedVersion());
        return mbrOperations.command(new CpfOwnerAdminCommand(
                "member", "updateStatus", String.valueOf(memberId), payload, requestUser, request.reason()));
    }

    public Map<String, Object> grantRole(long memberId, AdmMemberRoleRequest request, String requestUser) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, "roleCode", request.roleCode());
        put(payload, "roleName", request.roleName());
        put(payload, "roleType", request.roleType());
        put(payload, "serviceCode", request.serviceCode());
        put(payload, "temporaryYn", request.temporaryYn());
        put(payload, "expireAt", request.expireAt());
        put(payload, "useYn", request.useYn());
        put(payload, "expectedVersion", request.expectedVersion());
        put(payload, "idempotencyKey", request.idempotencyKey());
        return mbrOperations.command(new CpfOwnerAdminCommand(
                "member", "grantRole", String.valueOf(memberId), payload, requestUser, request.reason()));
    }

    public Map<String, Object> revokeRole(
            long memberId,
            String roleCode,
            String serviceCode,
            long expectedVersion,
            String idempotencyKey,
            String reason,
            String requestUser) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, "roleCode", roleCode);
        put(payload, "serviceCode", serviceCode);
        payload.put("expectedVersion", expectedVersion);
        put(payload, "idempotencyKey", idempotencyKey);
        return mbrOperations.command(new CpfOwnerAdminCommand(
                "member", "revokeRole", String.valueOf(memberId), payload, requestUser, reason));
    }

    private Map<String, Object> memberPayload(AdmMemberSaveRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, "memberNo", request.memberNo());
        put(payload, "customerNo", request.customerNo());
        put(payload, "loginId", request.loginId());
        put(payload, "name", request.name());
        put(payload, "email", request.email());
        put(payload, "mobileNo", request.mobileNo());
        put(payload, "memberStatus", request.memberStatus());
        put(payload, "lockYn", request.lockYn());
        put(payload, "withdrawYn", request.withdrawYn());
        put(payload, "channelCode", request.channelCode());
        put(payload, "description", request.description());
        put(payload, "expectedVersion", request.expectedVersion());
        return payload;
    }

    private ReadSection readTransactionLogs(String memberNo) {
        if (!CpfStrings.hasText(memberNo)) return ReadSection.available(List.of());
        try {
            return ReadSection.available(cpfJdbcTemplate.query("""
                    SELECT LOG_IDX, TRANSACTION_ID, TRACE_ID, BUSINESS_TRANSACTION_ID, URI,
                           RESPONSE_CODE, ERROR_CODE, LOG_TYPE, START_TIME, END_TIME, DURATION_MS
                      FROM cpf_transaction_log
                     WHERE MEMBER_NO = ?
                     ORDER BY LOG_IDX DESC
                    """,
                    ps -> {
                        ps.setString(1, memberNo);
                        ps.setMaxRows(50);
                    },
                    new ColumnMapRowMapper()));
        } catch (DataAccessException ex) {
            return ReadSection.failed("CPF_DB_READ_FAILED", CpfTransactionContext.transactionId());
        }
    }

    private ReadSection readAuditLogs(String memberId, String memberNo) {
        try {
            return ReadSection.available(admJdbcTemplate.query("""
                    SELECT AUDIT_ID, OPERATOR_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, CREATED_AT
                      FROM adm_audit_log
                     WHERE TARGET_TYPE IN ('mbr_member', 'mbr_member_role')
                       AND (TARGET_ID = ? OR TARGET_ID = ?)
                     ORDER BY AUDIT_ID DESC
                    """,
                    ps -> {
                        ps.setString(1, memberId);
                        ps.setString(2, memberNo);
                        ps.setMaxRows(50);
                    },
                    new ColumnMapRowMapper()));
        } catch (DataAccessException ex) {
            return ReadSection.failed("ADM_DB_READ_FAILED", CpfTransactionContext.transactionId());
        }
    }

    private record ReadSection(String state, List<Map<String, Object>> items, String errorCode, String transactionId) {
        static ReadSection available(List<Map<String, Object>> items) {
            return new ReadSection("AVAILABLE", List.copyOf(items), null, null);
        }

        static ReadSection failed(String errorCode, String transactionId) {
            return new ReadSection("FAILED", List.of(), errorCode, transactionId);
        }

        Map<String, Object> status() {
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("state", state);
            if (errorCode != null) status.put("errorCode", errorCode);
            if (transactionId != null && !transactionId.isBlank()) status.put("transactionId", transactionId);
            return status;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        if (!(value instanceof List<?> rows)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object row : rows) {
            if (row instanceof Map<?, ?> map) {
                result.add((Map<String, Object>) map);
            }
        }
        return List.copyOf(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private void put(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
