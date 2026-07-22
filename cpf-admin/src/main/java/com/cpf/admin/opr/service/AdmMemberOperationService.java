package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmMemberRoleRequest;
import com.cpf.admin.opr.dto.AdmMemberSaveRequest;
import com.cpf.admin.opr.dto.AdmMemberStatusRequest;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.exception.CpfNotFoundException;
import com.cpf.core.common.exception.CpfValidationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ADM 회원 관리 기능에서 mbrDB 회원과 권한 정보를 조회/변경합니다.
 */
@Service
public class AdmMemberOperationService extends com.cpf.admin.common.base.AdmBaseService {
    private static final DateTimeFormatter MEMBER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final JdbcTemplate mbrJdbcTemplate;
    private final JdbcTemplate cpfJdbcTemplate;
    private final JdbcTemplate admJdbcTemplate;

    public AdmMemberOperationService(
            @Qualifier("mbrJdbcTemplate") JdbcTemplate mbrJdbcTemplate,
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.mbrJdbcTemplate = mbrJdbcTemplate;
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.admJdbcTemplate = admJdbcTemplate;
    }

    /** 회원 검색 조건에 맞는 목록을 조회합니다. */
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

        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT m.id, m.member_no, m.customer_no, m.login_id, m.name, m.email, m.mobile_no,
                       m.member_status, m.lock_yn, m.withdraw_yn, m.channel_code,
                       m.joined_at, m.last_login_at, m.created_at, m.updated_at
                FROM mbr_member m
                LEFT JOIN mbr_member_role r ON r.member_id = m.id AND r.use_yn = 'Y'
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendLike(sql, args, "m.member_no", memberNo);
        appendLike(sql, args, "m.customer_no", customerNo);
        appendLike(sql, args, "m.login_id", loginId);
        appendLike(sql, args, "m.name", name);
        appendLike(sql, args, "m.email", email);
        appendLike(sql, args, "m.mobile_no", mobileNo);
        appendEquals(sql, args, "m.member_status", memberStatus);
        appendEquals(sql, args, "m.channel_code", channelCode);
        appendEquals(sql, args, "r.role_code", roleCode);
        sql.append(" ORDER BY m.id DESC LIMIT ?");
        args.add(Math.max(1, Math.min(limit, 500)));

        return mbrJdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    /** 회원 상세, 권한, 이력, 관련 거래/감사 로그를 함께 조회합니다. */
    public Map<String, Object> findMemberDetail(long memberId) {
        Map<String, Object> member = findMember(memberId);
        String memberNo = value(member.get("member_no"));

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("member", member);
        detail.put("roles", findRoles(memberId));
        detail.put("roleHistory", findRoleHistory(memberId));
        detail.put("loginHistory", findLoginHistory(memberId));
        detail.put("transactionLogs", findTransactionLogs(memberNo));
        detail.put("auditLogs", findAuditLogs(String.valueOf(memberId), memberNo));
        return detail;
    }

    /** 회원을 등록합니다. */
    @Transactional(transactionManager = "mbrAdmTransactionManager")
    public Map<String, Object> createMember(AdmMemberSaveRequest request, String requestUser) {
        validateName(request.name());
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");
        String memberNo = TextUtils.defaultIfBlank(request.memberNo(), generateMemberNo());
        String loginId = TextUtils.defaultIfBlank(request.loginId(), memberNo.toLowerCase());

        mbrJdbcTemplate.update("""
                INSERT INTO mbr_member (
                    member_no, customer_no, login_id, name, email, mobile_no, member_status,
                    lock_yn, withdraw_yn, channel_code, joined_at, description, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)
                """,
                memberNo,
                blankToNull(request.customerNo()),
                loginId,
                request.name().trim(),
                blankToNull(request.email()),
                blankToNull(request.mobileNo()),
                TextUtils.defaultIfBlank(request.memberStatus(), "ACTIVE"),
                yn(request.lockYn(), "N"),
                yn(request.withdrawYn(), "N"),
                TextUtils.defaultIfBlank(request.channelCode(), "WEB"),
                blankToNull(request.description()),
                user,
                user);

        Long memberId = mbrJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findMember(memberId == null ? -1 : memberId);
    }

    /** 회원 기본 정보를 수정합니다. */
    @Transactional(transactionManager = "mbrAdmTransactionManager")
    public Map<String, Object> updateMember(long memberId, AdmMemberSaveRequest request, String requestUser) {
        Map<String, Object> before = findMember(memberId);
        validateName(request.name());
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");

        mbrJdbcTemplate.update("""
                UPDATE mbr_member
                   SET member_no = ?,
                       customer_no = ?,
                       login_id = ?,
                       name = ?,
                       email = ?,
                       mobile_no = ?,
                       member_status = ?,
                       lock_yn = ?,
                       withdraw_yn = ?,
                       channel_code = ?,
                       description = ?,
                       updated_by = ?,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE id = ?
                """,
                TextUtils.defaultIfBlank(request.memberNo(), value(before.get("member_no"))),
                firstText(request.customerNo(), value(before.get("customer_no"))),
                TextUtils.defaultIfBlank(request.loginId(), value(before.get("login_id"))),
                request.name().trim(),
                firstText(request.email(), value(before.get("email"))),
                firstText(request.mobileNo(), value(before.get("mobile_no"))),
                TextUtils.defaultIfBlank(request.memberStatus(), value(before.get("member_status"))),
                yn(firstText(request.lockYn(), value(before.get("lock_yn"))), "N"),
                yn(firstText(request.withdrawYn(), value(before.get("withdraw_yn"))), "N"),
                TextUtils.defaultIfBlank(request.channelCode(), value(before.get("channel_code"))),
                blankToNull(request.description()),
                user,
                memberId);

        return findMember(memberId);
    }

    /** 회원 상태, 잠금, 탈퇴 여부를 변경합니다. */
    @Transactional(transactionManager = "mbrAdmTransactionManager")
    public Map<String, Object> updateStatus(long memberId, AdmMemberStatusRequest request, String requestUser) {
        Map<String, Object> before = findMember(memberId);
        mbrJdbcTemplate.update("""
                UPDATE mbr_member
                   SET member_status = ?,
                       lock_yn = ?,
                       withdraw_yn = ?,
                       updated_by = ?,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE id = ?
                """,
                TextUtils.defaultIfBlank(request.memberStatus(), value(before.get("member_status"))),
                yn(firstText(request.lockYn(), value(before.get("lock_yn"))), "N"),
                yn(firstText(request.withdrawYn(), value(before.get("withdraw_yn"))), "N"),
                TextUtils.defaultIfBlank(requestUser, "ADM"),
                memberId);
        return findMember(memberId);
    }

    /** 회원 권한을 부여하거나 갱신합니다. */
    @Transactional(transactionManager = "mbrAdmTransactionManager")
    public Map<String, Object> grantRole(long memberId, AdmMemberRoleRequest request, String requestUser) {
        findMember(memberId);
        if (!TextUtils.hasText(request.roleCode())) {
            throw new CpfValidationException("회원 권한 코드는 필수입니다.");
        }
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");
        Map<String, Object> before = findRole(memberId, request.roleCode(), request.serviceCode());

        mbrJdbcTemplate.update("""
                INSERT INTO mbr_member_role (
                    member_id, role_code, role_name, role_type, service_code, granted_at,
                    expire_at, temporary_yn, use_yn, grant_reason, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    role_name = VALUES(role_name),
                    role_type = VALUES(role_type),
                    expire_at = VALUES(expire_at),
                    temporary_yn = VALUES(temporary_yn),
                    use_yn = VALUES(use_yn),
                    grant_reason = VALUES(grant_reason),
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """,
                memberId,
                request.roleCode().trim(),
                TextUtils.defaultIfBlank(request.roleName(), request.roleCode()),
                TextUtils.defaultIfBlank(request.roleType(), "SERVICE"),
                TextUtils.defaultIfBlank(request.serviceCode(), "MBR"),
                blankToNull(request.expireAt()),
                yn(request.temporaryYn(), "N"),
                yn(request.useYn(), "Y"),
                blankToNull(request.reason()),
                user,
                user);

        Map<String, Object> after = findRole(memberId, request.roleCode(), request.serviceCode());
        insertRoleHistory(memberId, request.roleCode(), "GRANT", before, after, request.reason(), user);
        return Map.of("before", before, "after", after);
    }

    /** 회원 권한을 회수합니다. */
    @Transactional(transactionManager = "mbrAdmTransactionManager")
    public Map<String, Object> revokeRole(long memberId, String roleCode, String serviceCode, String reason, String requestUser) {
        findMember(memberId);
        Map<String, Object> before = findRole(memberId, roleCode, serviceCode);
        if (before.isEmpty()) {
            throw new CpfNotFoundException("회원 권한을 찾을 수 없습니다. roleCode=" + roleCode);
        }
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");
        mbrJdbcTemplate.update("""
                UPDATE mbr_member_role
                   SET use_yn = 'N',
                       updated_by = ?,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE member_id = ?
                   AND role_code = ?
                   AND service_code = ?
                """, user, memberId, roleCode, TextUtils.defaultIfBlank(serviceCode, "MBR"));
        Map<String, Object> after = findRole(memberId, roleCode, serviceCode);
        insertRoleHistory(memberId, roleCode, "REVOKE", before, after, reason, user);
        return Map.of("before", before, "after", after);
    }

    private Map<String, Object> findMember(long memberId) {
        try {
            return mbrJdbcTemplate.queryForMap("""
                    SELECT id, member_no, customer_no, login_id, name, email, mobile_no,
                           member_status, lock_yn, withdraw_yn, channel_code,
                           joined_at, last_login_at, description, created_by, created_at, updated_by, updated_at
                    FROM mbr_member
                    WHERE id = ?
                    """, memberId);
        } catch (DataAccessException ex) {
            throw new CpfNotFoundException("회원을 찾을 수 없습니다. memberId=" + memberId);
        }
    }

    private List<Map<String, Object>> findRoles(long memberId) {
        return mbrJdbcTemplate.queryForList("""
                SELECT member_role_id, member_id, role_code, role_name, role_type, service_code,
                       granted_at, expire_at, temporary_yn, use_yn, grant_reason, created_at, updated_at
                FROM mbr_member_role
                WHERE member_id = ?
                ORDER BY use_yn DESC, role_code
                """, memberId);
    }

    private Map<String, Object> findRole(long memberId, String roleCode, String serviceCode) {
        if (!TextUtils.hasText(roleCode)) {
            return Map.of();
        }
        List<Map<String, Object>> rows = mbrJdbcTemplate.queryForList("""
                SELECT member_role_id, member_id, role_code, role_name, role_type, service_code,
                       granted_at, expire_at, temporary_yn, use_yn, grant_reason, created_at, updated_at
                FROM mbr_member_role
                WHERE member_id = ?
                  AND role_code = ?
                  AND service_code = ?
                """, memberId, roleCode.trim(), TextUtils.defaultIfBlank(serviceCode, "MBR"));
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    private List<Map<String, Object>> findRoleHistory(long memberId) {
        return mbrJdbcTemplate.queryForList("""
                SELECT history_id, member_id, role_code, action_type, before_data, after_data, reason, created_by, created_at
                FROM mbr_member_role_history
                WHERE member_id = ?
                ORDER BY history_id DESC
                LIMIT 100
                """, memberId);
    }

    private List<Map<String, Object>> findLoginHistory(long memberId) {
        return mbrJdbcTemplate.queryForList("""
                SELECT login_history_id, member_id, login_id, success_yn, fail_reason, client_ip, user_agent, login_at
                FROM mbr_member_login_history
                WHERE member_id = ?
                ORDER BY login_history_id DESC
                LIMIT 50
                """, memberId);
    }

    private List<Map<String, Object>> findTransactionLogs(String memberNo) {
        if (!TextUtils.hasText(memberNo)) {
            return List.of();
        }
        try {
            return cpfJdbcTemplate.queryForList("""
                    SELECT LOG_IDX, TRANSACTION_ID, TRACE_ID, BUSINESS_TRANSACTION_ID, URI,
                           RESPONSE_CODE, ERROR_CODE, LOG_TYPE, START_TIME, END_TIME, DURATION_MS
                    FROM cpf_transaction_log
                    WHERE MEMBER_NO = ?
                    ORDER BY LOG_IDX DESC
                    LIMIT 50
                    """, memberNo);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private List<Map<String, Object>> findAuditLogs(String memberId, String memberNo) {
        try {
            return admJdbcTemplate.queryForList("""
                    SELECT AUDIT_ID, OPERATOR_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, CREATED_AT
                    FROM adm_audit_log
                    WHERE TARGET_TYPE IN ('mbr_member', 'mbr_member_role')
                      AND (TARGET_ID = ? OR TARGET_ID = ?)
                    ORDER BY AUDIT_ID DESC
                    LIMIT 50
                    """, memberId, memberNo);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private void insertRoleHistory(
            long memberId,
            String roleCode,
            String actionType,
            Map<String, Object> before,
            Map<String, Object> after,
            String reason,
            String requestUser) {
        mbrJdbcTemplate.update("""
                INSERT INTO mbr_member_role_history (
                    member_id, role_code, action_type, before_data, after_data, reason, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                memberId,
                roleCode,
                actionType,
                String.valueOf(before),
                String.valueOf(after),
                reason,
                requestUser,
                requestUser);
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (TextUtils.hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE CONCAT('%', ?, '%')");
            args.add(value.trim());
        }
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (TextUtils.hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim());
        }
    }

    private void validateName(String name) {
        if (!TextUtils.hasText(name)) {
            throw new CpfValidationException("회원명은 필수입니다.");
        }
        if (name.length() > 100) {
            throw new CpfValidationException("회원명은 100자 이하여야 합니다.");
        }
    }

    private String generateMemberNo() {
        return "M" + LocalDateTime.now().format(MEMBER_NO_TIME) + (System.nanoTime() % 1000);
    }

    private String blankToNull(String value) {
        return TextUtils.hasText(value) ? value.trim() : null;
    }

    private String firstText(String value, String fallback) {
        return TextUtils.hasText(value) ? value.trim() : fallback;
    }

    private String yn(String value, String fallback) {
        String normalized = TextUtils.defaultIfBlank(value, fallback).trim().toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
