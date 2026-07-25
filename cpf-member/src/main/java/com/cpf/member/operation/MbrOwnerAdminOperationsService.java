package com.cpf.member.operation;

import com.cpf.core.api.admin.CpfOwnerAdminCommand;
import com.cpf.core.api.admin.CpfOwnerAdminOperationsPort;
import com.cpf.core.api.admin.CpfOwnerAdminQuery;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.util.CpfStrings;
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
 * MBR Owner가 제공하는 회원 운영 구현입니다.
 *
 * <p>회원/역할/로그인 이력 테이블은 MBR가 소유합니다. ADM은 이 구현이나 DB에 직접 접근하지 않고
 * {@link CpfOwnerAdminOperationsPort}만 소비합니다.</p>
 */
@Service("mbrOwnerAdminOperationsPort")
public class MbrOwnerAdminOperationsService implements CpfOwnerAdminOperationsPort {
    private static final DateTimeFormatter MEMBER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String RESOURCE_MEMBER = "member";

    private final JdbcTemplate jdbcTemplate;

    public MbrOwnerAdminOperationsService(@Qualifier("mbrJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String ownerSystemCode() {
        return "MBR";
    }

    @Override
    public Map<String, Object> query(CpfOwnerAdminQuery query) {
        if ("system".equalsIgnoreCase(query.resource()) && "health".equals(query.operation())) {
            return health();
        }
        requireMemberResource(query.resource());
        return switch (query.operation()) {
            case "findMembers" -> Map.of("items", findMembers(query.criteria()));
            case "findMemberDetail" -> findMemberDetail(requiredLong(query.resourceId(), "memberId"));
            default -> throw new CpfValidationException("지원하지 않는 MBR 운영 조회입니다. operation=" + query.operation());
        };
    }

    @Override
    @Transactional(transactionManager = "mbrTransactionManager")
    public Map<String, Object> command(CpfOwnerAdminCommand command) {
        requireMemberResource(command.resource());
        return switch (command.operation()) {
            case "createMember" -> createMember(command.payload(), command.requestUser());
            case "updateMember" -> updateMember(requiredLong(command.resourceId(), "memberId"), command.payload(), command.requestUser());
            case "updateStatus" -> updateStatus(requiredLong(command.resourceId(), "memberId"), command.payload(), command.requestUser());
            case "grantRole" -> grantRole(requiredLong(command.resourceId(), "memberId"), command.payload(), command.requestUser(), command.reason());
            case "revokeRole" -> revokeRole(requiredLong(command.resourceId(), "memberId"), command.payload(), command.requestUser(), command.reason());
            default -> throw new CpfValidationException("지원하지 않는 MBR 운영 명령입니다. operation=" + command.operation());
        };
    }

    private Map<String, Object> health() {
        try {
            Integer value = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Map.of("status", Integer.valueOf(1).equals(value) ? "UP" : "DOWN", "owner", "MBR");
        } catch (DataAccessException ex) {
            return Map.of("status", "DOWN", "owner", "MBR");
        }
    }

    private List<Map<String, Object>> findMembers(Map<String, Object> criteria) {
        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT m.id, m.member_no, m.customer_no, m.login_id, m.name, m.email, m.mobile_no,
                       m.member_status, m.lock_yn, m.withdraw_yn, m.channel_code,
                       m.joined_at, m.last_login_at, m.created_at, m.updated_at
                  FROM mbr_member m
                  LEFT JOIN mbr_member_role r ON r.member_id = m.id AND r.use_yn = 'Y'
                 WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendLike(sql, args, "m.member_no", text(criteria, "memberNo"));
        appendLike(sql, args, "m.customer_no", text(criteria, "customerNo"));
        appendLike(sql, args, "m.login_id", text(criteria, "loginId"));
        appendLike(sql, args, "m.name", text(criteria, "name"));
        appendLike(sql, args, "m.email", text(criteria, "email"));
        appendLike(sql, args, "m.mobile_no", text(criteria, "mobileNo"));
        appendEquals(sql, args, "m.member_status", text(criteria, "memberStatus"));
        appendEquals(sql, args, "m.channel_code", text(criteria, "channelCode"));
        appendEquals(sql, args, "r.role_code", text(criteria, "roleCode"));
        sql.append(" ORDER BY m.id DESC");
        int limit = clampInt(criteria.get("limit"), 100, 1, 500);
        return limitRows(jdbcTemplate.queryForList(sql.toString(), args.toArray()), limit);
    }

    private Map<String, Object> findMemberDetail(long memberId) {
        Map<String, Object> member = findMember(memberId);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("member", member);
        detail.put("roles", findRoles(memberId));
        detail.put("roleHistory", findRoleHistory(memberId));
        detail.put("loginHistory", findLoginHistory(memberId));
        return detail;
    }

    private Map<String, Object> createMember(Map<String, Object> payload, String requestUser) {
        validateName(text(payload, "name"));
        String user = CpfStrings.defaultIfBlank(requestUser, "ADM");
        String memberNo = CpfStrings.defaultIfBlank(text(payload, "memberNo"), generateMemberNo());
        String loginId = CpfStrings.defaultIfBlank(text(payload, "loginId"), memberNo.toLowerCase());
        jdbcTemplate.update("""
                INSERT INTO mbr_member (
                    member_no, customer_no, login_id, name, email, mobile_no, member_status,
                    lock_yn, withdraw_yn, channel_code, joined_at, description, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)
                """,
                memberNo,
                blankToNull(text(payload, "customerNo")),
                loginId,
                text(payload, "name").trim(),
                blankToNull(text(payload, "email")),
                blankToNull(text(payload, "mobileNo")),
                CpfStrings.defaultIfBlank(text(payload, "memberStatus"), "ACTIVE"),
                yn(text(payload, "lockYn"), "N"),
                yn(text(payload, "withdrawYn"), "N"),
                CpfStrings.defaultIfBlank(text(payload, "channelCode"), "WEB"),
                blankToNull(text(payload, "description")),
                user,
                user);
        return findMemberByNo(memberNo);
    }

    private Map<String, Object> updateMember(long memberId, Map<String, Object> payload, String requestUser) {
        Map<String, Object> before = findMember(memberId);
        validateName(text(payload, "name"));
        String user = CpfStrings.defaultIfBlank(requestUser, "ADM");
        jdbcTemplate.update("""
                UPDATE mbr_member
                   SET member_no = ?, customer_no = ?, login_id = ?, name = ?, email = ?, mobile_no = ?,
                       member_status = ?, lock_yn = ?, withdraw_yn = ?, channel_code = ?, description = ?,
                       updated_by = ?, updated_at = CURRENT_TIMESTAMP
                 WHERE id = ?
                """,
                CpfStrings.defaultIfBlank(text(payload, "memberNo"), value(before.get("member_no"))),
                firstText(text(payload, "customerNo"), value(before.get("customer_no"))),
                CpfStrings.defaultIfBlank(text(payload, "loginId"), value(before.get("login_id"))),
                text(payload, "name").trim(),
                firstText(text(payload, "email"), value(before.get("email"))),
                firstText(text(payload, "mobileNo"), value(before.get("mobile_no"))),
                CpfStrings.defaultIfBlank(text(payload, "memberStatus"), value(before.get("member_status"))),
                yn(firstText(text(payload, "lockYn"), value(before.get("lock_yn"))), "N"),
                yn(firstText(text(payload, "withdrawYn"), value(before.get("withdraw_yn"))), "N"),
                CpfStrings.defaultIfBlank(text(payload, "channelCode"), value(before.get("channel_code"))),
                blankToNull(text(payload, "description")),
                user,
                memberId);
        return findMember(memberId);
    }

    private Map<String, Object> updateStatus(long memberId, Map<String, Object> payload, String requestUser) {
        Map<String, Object> before = findMember(memberId);
        jdbcTemplate.update("""
                UPDATE mbr_member
                   SET member_status = ?, lock_yn = ?, withdraw_yn = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP
                 WHERE id = ?
                """,
                CpfStrings.defaultIfBlank(text(payload, "memberStatus"), value(before.get("member_status"))),
                yn(firstText(text(payload, "lockYn"), value(before.get("lock_yn"))), "N"),
                yn(firstText(text(payload, "withdrawYn"), value(before.get("withdraw_yn"))), "N"),
                CpfStrings.defaultIfBlank(requestUser, "ADM"),
                memberId);
        return findMember(memberId);
    }

    private Map<String, Object> grantRole(
            long memberId,
            Map<String, Object> payload,
            String requestUser,
            String reason) {
        findMember(memberId);
        String roleCode = text(payload, "roleCode");
        if (!CpfStrings.hasText(roleCode)) {
            throw new CpfValidationException("회원 권한 코드는 필수입니다.");
        }
        String serviceCode = CpfStrings.defaultIfBlank(text(payload, "serviceCode"), "MBR");
        String user = CpfStrings.defaultIfBlank(requestUser, "ADM");
        Map<String, Object> before = findRole(memberId, roleCode, serviceCode);
        if (before.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO mbr_member_role (
                        member_id, role_code, role_name, role_type, service_code, granted_at,
                        expire_at, temporary_yn, use_yn, grant_reason, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?)
                    """,
                    memberId,
                    roleCode.trim(),
                    CpfStrings.defaultIfBlank(text(payload, "roleName"), roleCode),
                    CpfStrings.defaultIfBlank(text(payload, "roleType"), "SERVICE"),
                    serviceCode,
                    blankToNull(text(payload, "expireAt")),
                    yn(text(payload, "temporaryYn"), "N"),
                    yn(text(payload, "useYn"), "Y"),
                    blankToNull(reason),
                    user,
                    user);
        } else {
            jdbcTemplate.update("""
                    UPDATE mbr_member_role
                       SET role_name = ?, role_type = ?, expire_at = ?, temporary_yn = ?, use_yn = ?,
                           grant_reason = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP
                     WHERE member_id = ? AND role_code = ? AND service_code = ?
                    """,
                    CpfStrings.defaultIfBlank(text(payload, "roleName"), roleCode),
                    CpfStrings.defaultIfBlank(text(payload, "roleType"), "SERVICE"),
                    blankToNull(text(payload, "expireAt")),
                    yn(text(payload, "temporaryYn"), "N"),
                    yn(text(payload, "useYn"), "Y"),
                    blankToNull(reason),
                    user,
                    memberId,
                    roleCode.trim(),
                    serviceCode);
        }
        Map<String, Object> after = findRole(memberId, roleCode, serviceCode);
        insertRoleHistory(memberId, roleCode, "GRANT", before, after, reason, user);
        return beforeAfter(before, after);
    }

    private Map<String, Object> revokeRole(
            long memberId,
            Map<String, Object> payload,
            String requestUser,
            String reason) {
        findMember(memberId);
        String roleCode = text(payload, "roleCode");
        String serviceCode = CpfStrings.defaultIfBlank(text(payload, "serviceCode"), "MBR");
        Map<String, Object> before = findRole(memberId, roleCode, serviceCode);
        if (before.isEmpty()) {
            throw new CpfNotFoundException("회원 권한을 찾을 수 없습니다. roleCode=" + roleCode);
        }
        String user = CpfStrings.defaultIfBlank(requestUser, "ADM");
        jdbcTemplate.update("""
                UPDATE mbr_member_role
                   SET use_yn = 'N', updated_by = ?, updated_at = CURRENT_TIMESTAMP
                 WHERE member_id = ? AND role_code = ? AND service_code = ?
                """, user, memberId, roleCode.trim(), serviceCode);
        Map<String, Object> after = findRole(memberId, roleCode, serviceCode);
        insertRoleHistory(memberId, roleCode, "REVOKE", before, after, reason, user);
        return beforeAfter(before, after);
    }

    private Map<String, Object> findMember(long memberId) {
        try {
            return jdbcTemplate.queryForMap("""
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

    private Map<String, Object> findMemberByNo(String memberNo) {
        try {
            return jdbcTemplate.queryForMap("""
                    SELECT id, member_no, customer_no, login_id, name, email, mobile_no,
                           member_status, lock_yn, withdraw_yn, channel_code,
                           joined_at, last_login_at, description, created_by, created_at, updated_by, updated_at
                      FROM mbr_member
                     WHERE member_no = ?
                    """, memberNo);
        } catch (DataAccessException ex) {
            throw new CpfNotFoundException("등록된 회원을 다시 조회할 수 없습니다. memberNo=" + memberNo);
        }
    }

    private List<Map<String, Object>> findRoles(long memberId) {
        return jdbcTemplate.queryForList("""
                SELECT member_role_id, member_id, role_code, role_name, role_type, service_code,
                       granted_at, expire_at, temporary_yn, use_yn, grant_reason, created_at, updated_at
                  FROM mbr_member_role
                 WHERE member_id = ?
                 ORDER BY use_yn DESC, role_code
                """, memberId);
    }

    private Map<String, Object> findRole(long memberId, String roleCode, String serviceCode) {
        if (!CpfStrings.hasText(roleCode)) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT member_role_id, member_id, role_code, role_name, role_type, service_code,
                       granted_at, expire_at, temporary_yn, use_yn, grant_reason, created_at, updated_at
                  FROM mbr_member_role
                 WHERE member_id = ? AND role_code = ? AND service_code = ?
                """, memberId, roleCode.trim(), CpfStrings.defaultIfBlank(serviceCode, "MBR"));
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    private List<Map<String, Object>> findRoleHistory(long memberId) {
        return limitRows(jdbcTemplate.queryForList("""
                SELECT history_id, member_id, role_code, action_type, before_data, after_data, reason, created_by, created_at
                  FROM mbr_member_role_history
                 WHERE member_id = ?
                 ORDER BY history_id DESC
                """, memberId), 100);
    }

    private List<Map<String, Object>> findLoginHistory(long memberId) {
        return limitRows(jdbcTemplate.queryForList("""
                SELECT login_history_id, member_id, login_id, success_yn, fail_reason, client_ip, user_agent, login_at
                  FROM mbr_member_login_history
                 WHERE member_id = ?
                 ORDER BY login_history_id DESC
                """, memberId), 50);
    }

    private void insertRoleHistory(
            long memberId,
            String roleCode,
            String actionType,
            Map<String, Object> before,
            Map<String, Object> after,
            String reason,
            String requestUser) {
        jdbcTemplate.update("""
                INSERT INTO mbr_member_role_history (
                    member_id, role_code, action_type, before_data, after_data, reason, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                memberId,
                roleCode,
                actionType,
                String.valueOf(before),
                String.valueOf(after),
                blankToNull(reason),
                requestUser,
                requestUser);
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (CpfStrings.hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            args.add("%" + value.trim() + "%");
        }
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (CpfStrings.hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim());
        }
    }

    private List<Map<String, Object>> limitRows(List<Map<String, Object>> rows, int limit) {
        return rows.size() <= limit ? rows : List.copyOf(rows.subList(0, limit));
    }

    private int clampInt(Object value, int fallback, int min, int max) {
        try {
            int parsed = value == null ? fallback : Integer.parseInt(String.valueOf(value));
            return Math.max(min, Math.min(parsed, max));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private long requiredLong(String value, String fieldName) {
        if (!CpfStrings.hasText(value)) {
            throw new CpfValidationException(fieldName + "은(는) 필수입니다.");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new CpfValidationException(fieldName + " 형식이 올바르지 않습니다.");
        }
    }

    private void requireMemberResource(String resource) {
        if (!RESOURCE_MEMBER.equalsIgnoreCase(resource)) {
            throw new CpfValidationException("지원하지 않는 MBR 운영 자원입니다. resource=" + resource);
        }
    }

    private void validateName(String name) {
        if (!CpfStrings.hasText(name)) {
            throw new CpfValidationException("회원명은 필수입니다.");
        }
        if (name.length() > 100) {
            throw new CpfValidationException("회원명은 100자 이하여야 합니다.");
        }
    }

    private String generateMemberNo() {
        return "M" + LocalDateTime.now().format(MEMBER_NO_TIME) + Math.floorMod(System.nanoTime(), 1000);
    }

    private String text(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String blankToNull(String value) {
        return CpfStrings.hasText(value) ? value.trim() : null;
    }

    private String firstText(String value, String fallback) {
        return CpfStrings.hasText(value) ? value.trim() : fallback;
    }

    private String yn(String value, String fallback) {
        String normalized = CpfStrings.defaultIfBlank(value, fallback).trim().toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Map<String, Object> beforeAfter(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("before", before == null ? Map.of() : before);
        result.put("after", after == null ? Map.of() : after);
        return result;
    }
}
