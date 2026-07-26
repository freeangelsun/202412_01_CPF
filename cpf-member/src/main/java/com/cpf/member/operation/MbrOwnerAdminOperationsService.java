package com.cpf.member.operation;

import com.cpf.member.common.base.MbrBaseService;
import com.cpf.core.api.admin.CpfOwnerAdminCommand;
import com.cpf.core.api.admin.CpfOwnerAdminOperationsPort;
import com.cpf.core.api.admin.CpfOwnerAdminQuery;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.util.CpfStrings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.sql.Statement;

/**
 * MBR Owner가 제공하는 회원 운영 구현입니다.
 *
 * <p>회원/역할/로그인 이력 테이블은 MBR가 소유합니다. ADM은 이 구현이나 DB에 직접 접근하지 않고
 * {@link CpfOwnerAdminOperationsPort}만 소비합니다.</p>
 */
@Service("mbrOwnerAdminOperationsPort")
public class MbrOwnerAdminOperationsService extends MbrBaseService
        implements CpfOwnerAdminOperationsPort {
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
            case "findMembersPage" -> findMembersPage(query.criteria());
            case "findMemberNoIssueHistory" -> Map.of("items", findMemberNoIssueHistory(query.criteria()));
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
                       m.member_status, m.lock_yn, m.withdraw_yn, m.channel_code, m.version_no,
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

    /** ADM 대량 운영 조회는 DB에서 직접 page/count하여 Heap 과부하를 방지합니다. */
    private Map<String, Object> findMembersPage(Map<String, Object> criteria) {
        int page = clampInt(criteria.get("page"), 0, 0, 1000000);
        int size = clampInt(criteria.get("size"), 20, 1, 200);
        StringBuilder where = new StringBuilder(" FROM mbr_member m LEFT JOIN mbr_member_role r ON r.member_id=m.id AND r.use_yn='Y' WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        appendLike(where,args,"m.member_no",text(criteria,"memberNo"));
        appendLike(where,args,"m.customer_no",text(criteria,"customerNo"));
        appendLike(where,args,"m.login_id",text(criteria,"loginId"));
        appendLike(where,args,"m.name",text(criteria,"name"));
        appendLike(where,args,"m.email",text(criteria,"email"));
        appendLike(where,args,"m.mobile_no",text(criteria,"mobileNo"));
        appendEquals(where,args,"m.member_status",text(criteria,"memberStatus"));
        appendEquals(where,args,"m.channel_code",text(criteria,"channelCode"));
        appendEquals(where,args,"r.role_code",text(criteria,"roleCode"));
        Long total=jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT m.id)"+where,Long.class,args.toArray());
        List<Object> pageArgs=new ArrayList<>(args); pageArgs.add(size); pageArgs.add((long)page*size);
        String select="SELECT DISTINCT m.id,m.member_no,m.customer_no,m.login_id,m.name,m.email,m.mobile_no,m.member_status,m.lock_yn,m.withdraw_yn,m.channel_code,m.version_no,m.joined_at,m.last_login_at,m.created_at,m.updated_at"+where+" ORDER BY m.id DESC LIMIT ? OFFSET ?";
        List<Map<String,Object>> items=jdbcTemplate.queryForList(select,pageArgs.toArray());
        return Map.of("items",items,"page",page,"size",size,"totalElements",total==null?0L:total);
    }

    private List<Map<String, Object>> findMemberNoIssueHistory(Map<String, Object> criteria) {
        StringBuilder sql = new StringBuilder("""
                SELECT issue_id, member_no, issue_type, issued_by, issued_at
                  FROM mbr_member_no_issue_history
                 WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendLike(sql, args, "member_no", text(criteria, "memberNo"));
        appendEquals(sql, args, "issue_type", text(criteria, "issueType"));
        appendLike(sql, args, "issued_by", text(criteria, "issuedBy"));
        sql.append(" ORDER BY issue_id DESC");
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
        String user = requiredActor(requestUser);
        boolean autoIssued = !CpfStrings.hasText(text(payload, "memberNo"));
        boolean autoLoginId = !CpfStrings.hasText(text(payload, "loginId"));
        int attempts = autoIssued ? 3 : 1;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            String memberNo = autoIssued ? generateMemberNo(user) : text(payload, "memberNo").trim();
            String loginId = CpfStrings.defaultIfBlank(text(payload, "loginId"), memberNo.toLowerCase());
            try {
                jdbcTemplate.update("""
                        INSERT INTO mbr_member (
                            member_no, customer_no, login_id, name, email, mobile_no, member_status,
                            lock_yn, withdraw_yn, channel_code, joined_at, description, version_no, created_by, updated_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, 0, ?, ?)
                        """,
                        memberNo,
                        CpfStrings.defaultIfBlank(text(payload, "customerNo"), memberNo),
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
                jdbcTemplate.update("""
                        INSERT INTO mbr_member_no_issue_history (member_no, issue_type, issued_by)
                        VALUES (?, ?, ?)
                        """, memberNo, autoIssued ? "AUTO" : "MANUAL", user);
                return findMemberByNo(memberNo);
            } catch (DuplicateKeyException ex) {
                // 사용자가 지정한 회원번호/로그인 ID 충돌은 재채번으로 해결되지 않으므로 즉시 반환합니다.
                if (!autoIssued || !autoLoginId) {
                    throw new CpfBusinessException(CpfErrorCode.DUPLICATE, "회원번호 또는 로그인 ID가 이미 사용 중입니다.");
                }
            }
        }
        throw new CpfBusinessException(CpfErrorCode.DUPLICATE,
                "자동 회원번호 발급 충돌을 재시도했지만 완료하지 못했습니다.", Map.of("attempts", attempts));
    }

    private Map<String, Object> updateMember(long memberId, Map<String, Object> payload, String requestUser) {
        Map<String, Object> before = findMember(memberId);
        validateName(text(payload, "name"));
        long expectedVersion = requiredVersion(payload, "expectedVersion");
        String user = requiredActor(requestUser);
        int updated = jdbcTemplate.update("""
                UPDATE mbr_member
                   SET member_no = ?, customer_no = ?, login_id = ?, name = ?, email = ?, mobile_no = ?,
                       member_status = ?, lock_yn = ?, withdraw_yn = ?, channel_code = ?, description = ?,
                       version_no = version_no + 1, updated_by = ?, updated_at = CURRENT_TIMESTAMP
                 WHERE id = ? AND version_no = ?
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
                user, memberId, expectedVersion);
        requireSingleVersionedUpdate(updated, "mbr_member:" + memberId, expectedVersion);
        return findMember(memberId);
    }

    private Map<String, Object> updateStatus(long memberId, Map<String, Object> payload, String requestUser) {
        Map<String, Object> before = findMember(memberId);
        long expectedVersion = requiredVersion(payload, "expectedVersion");
        int updated = jdbcTemplate.update("""
                UPDATE mbr_member
                   SET member_status = ?, lock_yn = ?, withdraw_yn = ?,
                       version_no = version_no + 1, updated_by = ?, updated_at = CURRENT_TIMESTAMP
                 WHERE id = ? AND version_no = ?
                """,
                CpfStrings.defaultIfBlank(text(payload, "memberStatus"), value(before.get("member_status"))),
                yn(firstText(text(payload, "lockYn"), value(before.get("lock_yn"))), "N"),
                yn(firstText(text(payload, "withdrawYn"), value(before.get("withdraw_yn"))), "N"),
                requiredActor(requestUser),
                memberId, expectedVersion);
        requireSingleVersionedUpdate(updated, "mbr_member:" + memberId, expectedVersion);
        return findMember(memberId);
    }

    private Map<String, Object> grantRole(
            long memberId,
            Map<String, Object> payload,
            String requestUser,
            String reason) {
        findMember(memberId);
        String roleCode = requiredText(payload, "roleCode");
        String serviceCode = CpfStrings.defaultIfBlank(text(payload, "serviceCode"), "MBR");
        String idempotencyKey = requiredText(payload, "idempotencyKey");
        String user = requiredActor(requestUser);
        if (!reserveRoleOperation(idempotencyKey, memberId, serviceCode, roleCode, "GRANT", user)) {
            return idempotentRoleResult(memberId, roleCode, serviceCode, idempotencyKey);
        }

        Map<String, Object> before = findRole(memberId, roleCode, serviceCode);
        if (before.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO mbr_member_role (
                        member_id, role_code, role_name, role_type, service_code, granted_by, granted_at,
                        expire_at, temporary_yn, use_yn, grant_reason, version_no, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, 0, ?, ?)
                    """,
                    memberId, roleCode.trim(),
                    CpfStrings.defaultIfBlank(text(payload, "roleName"), roleCode),
                    CpfStrings.defaultIfBlank(text(payload, "roleType"), "SERVICE"),
                    serviceCode, user, blankToNull(text(payload, "expireAt")),
                    yn(text(payload, "temporaryYn"), "N"), yn(text(payload, "useYn"), "Y"),
                    blankToNull(reason), user, user);
        } else {
            long expectedVersion = requiredVersion(payload, "expectedVersion");
            int updated = jdbcTemplate.update("""
                    UPDATE mbr_member_role
                       SET role_name = ?, role_type = ?, expire_at = ?, temporary_yn = ?, use_yn = ?,
                           grant_reason = ?, granted_by = ?, granted_at = CURRENT_TIMESTAMP,
                           revoked_by = NULL, revoked_at = NULL,
                           version_no = version_no + 1, updated_by = ?, updated_at = CURRENT_TIMESTAMP
                     WHERE member_id = ? AND role_code = ? AND service_code = ? AND version_no = ?
                    """,
                    CpfStrings.defaultIfBlank(text(payload, "roleName"), roleCode),
                    CpfStrings.defaultIfBlank(text(payload, "roleType"), "SERVICE"),
                    blankToNull(text(payload, "expireAt")), yn(text(payload, "temporaryYn"), "N"),
                    yn(text(payload, "useYn"), "Y"), blankToNull(reason), user, user,
                    memberId, roleCode.trim(), serviceCode, expectedVersion);
            requireSingleVersionedUpdate(updated, "mbr_member_role:" + memberId + ":" + roleCode, expectedVersion);
        }
        Map<String, Object> after = findRole(memberId, roleCode, serviceCode);
        insertRoleHistory(memberId, serviceCode, roleCode, "GRANT", before, after, reason, user);
        completeRoleOperation(idempotencyKey, after);
        return beforeAfter(before, after);
    }

    private Map<String, Object> revokeRole(
            long memberId,
            Map<String, Object> payload,
            String requestUser,
            String reason) {
        findMember(memberId);
        String roleCode = requiredText(payload, "roleCode");
        String serviceCode = CpfStrings.defaultIfBlank(text(payload, "serviceCode"), "MBR");
        String idempotencyKey = requiredText(payload, "idempotencyKey");
        String user = requiredActor(requestUser);
        if (!reserveRoleOperation(idempotencyKey, memberId, serviceCode, roleCode, "REVOKE", user)) {
            return idempotentRoleResult(memberId, roleCode, serviceCode, idempotencyKey);
        }
        Map<String, Object> before = findRole(memberId, roleCode, serviceCode);
        if (before.isEmpty()) throw new CpfNotFoundException("회원 권한을 찾을 수 없습니다. roleCode=" + roleCode);
        long expectedVersion = requiredVersion(payload, "expectedVersion");
        int updated = jdbcTemplate.update("""
                UPDATE mbr_member_role
                   SET use_yn = 'N', revoked_by = ?, revoked_at = CURRENT_TIMESTAMP,
                       version_no = version_no + 1, updated_by = ?, updated_at = CURRENT_TIMESTAMP
                 WHERE member_id = ? AND role_code = ? AND service_code = ? AND version_no = ?
                """, user, user, memberId, roleCode.trim(), serviceCode, expectedVersion);
        requireSingleVersionedUpdate(updated, "mbr_member_role:" + memberId + ":" + roleCode, expectedVersion);
        Map<String, Object> after = findRole(memberId, roleCode, serviceCode);
        insertRoleHistory(memberId, serviceCode, roleCode, "REVOKE", before, after, reason, user);
        completeRoleOperation(idempotencyKey, after);
        return beforeAfter(before, after);
    }

    private Map<String, Object> findMember(long memberId) {
        try {
            return jdbcTemplate.queryForMap("""
                    SELECT id, member_no, customer_no, login_id, name, email, mobile_no,
                           member_status, lock_yn, withdraw_yn, channel_code, version_no,
                           joined_at, last_login_at, description, created_by, created_at, updated_by, updated_at
                      FROM mbr_member
                     WHERE id = ?
                    """, memberId);
        } catch (EmptyResultDataAccessException ex) {
            throw new CpfNotFoundException("회원을 찾을 수 없습니다. memberId=" + memberId);
        } catch (IncorrectResultSizeDataAccessException ex) {
            throw new CpfBusinessException(CpfErrorCode.DATABASE_ERROR, "회원 데이터 무결성 오류가 감지되었습니다.");
        } catch (DataAccessException ex) {
            throw new CpfBusinessException(CpfErrorCode.DATABASE_ERROR, "회원 데이터 저장소 조회에 실패했습니다.");
        }
    }

    private Map<String, Object> findMemberByNo(String memberNo) {
        try {
            return jdbcTemplate.queryForMap("""
                    SELECT id, member_no, customer_no, login_id, name, email, mobile_no,
                           member_status, lock_yn, withdraw_yn, channel_code, version_no,
                           joined_at, last_login_at, description, created_by, created_at, updated_by, updated_at
                      FROM mbr_member
                     WHERE member_no = ?
                    """, memberNo);
        } catch (EmptyResultDataAccessException ex) {
            throw new CpfNotFoundException("등록된 회원을 다시 조회할 수 없습니다. memberNo=" + memberNo);
        } catch (IncorrectResultSizeDataAccessException ex) {
            throw new CpfBusinessException(CpfErrorCode.DATABASE_ERROR, "회원번호 중복 데이터가 감지되었습니다.");
        } catch (DataAccessException ex) {
            throw new CpfBusinessException(CpfErrorCode.DATABASE_ERROR, "회원 데이터 저장소 조회에 실패했습니다.");
        }
    }

    private List<Map<String, Object>> findRoles(long memberId) {
        return jdbcTemplate.queryForList("""
                SELECT member_role_id, member_id, role_code, role_name, role_type, service_code,
                       granted_at, expire_at, temporary_yn, use_yn, grant_reason, version_no, created_at, updated_at
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
                       granted_at, expire_at, temporary_yn, use_yn, grant_reason, version_no, created_at, updated_at
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
                SELECT login_history_id, member_id, login_id, login_result, failure_reason,
                       login_ip AS client_ip, user_agent, created_at AS login_at
                  FROM mbr_member_login_history
                 WHERE member_id = ?
                 ORDER BY login_history_id DESC
                """, memberId), 50);
    }

    private void insertRoleHistory(
            long memberId,
            String serviceCode,
            String roleCode,
            String actionType,
            Map<String, Object> before,
            Map<String, Object> after,
            String reason,
            String requestUser) {
        jdbcTemplate.update("""
                INSERT INTO mbr_member_role_history (
                    member_id, service_code, role_code, action_type, before_data, after_data,
                    reason, operator_id, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                memberId, serviceCode, roleCode, actionType, String.valueOf(before), String.valueOf(after),
                CpfStrings.defaultIfBlank(reason, "운영 권한 변경"), requestUser, requestUser, requestUser);
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

    private String generateMemberNo(String requestUser) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                    "INSERT INTO mbr_member_no_sequence (requested_by) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, requestUser);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new CpfBusinessException(CpfErrorCode.DATABASE_ERROR, "회원번호 채번 결과를 확인할 수 없습니다.");
        return "M" + String.format("%014d", key.longValue());
    }

    private long requiredVersion(Map<String, Object> payload, String fieldName) {
        Object raw = payload == null ? null : payload.get(fieldName);
        if (raw == null) throw new CpfValidationException(fieldName + "은(는) 필수입니다.");
        try {
            long version = Long.parseLong(String.valueOf(raw));
            if (version < 0) throw new NumberFormatException();
            return version;
        } catch (NumberFormatException ex) {
            throw new CpfValidationException(fieldName + "은(는) 0 이상의 정수여야 합니다.");
        }
    }


    private String requiredActor(String requestUser) {
        if (!CpfStrings.hasText(requestUser)) {
            throw new CpfValidationException("운영 변경 요청자는 필수입니다.");
        }
        return requestUser.trim();
    }

    private String requiredText(Map<String, Object> payload, String fieldName) {
        String result = text(payload, fieldName);
        if (!CpfStrings.hasText(result)) throw new CpfValidationException(fieldName + "은(는) 필수입니다.");
        return result.trim();
    }

    private void requireSingleVersionedUpdate(int updated, String target, long expectedVersion) {
        if (updated != 1) {
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,
                    "다른 요청에 의해 데이터가 변경되었습니다. 최신 Version을 다시 조회하세요.",
                    Map.of("target", target, "expectedVersion", expectedVersion));
        }
    }

    private boolean reserveRoleOperation(
            String idempotencyKey, long memberId, String serviceCode, String roleCode, String operationType, String requestUser) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO mbr_member_role_operation (
                        idempotency_key, member_id, service_code, role_code, operation_type, operation_status, created_by
                    ) VALUES (?, ?, ?, ?, ?, 'PENDING', ?)
                    """, idempotencyKey, memberId, serviceCode, roleCode.trim(), operationType, requestUser);
            return true;
        } catch (DuplicateKeyException ex) {
            Map<String, Object> existing = jdbcTemplate.queryForMap("""
                    SELECT member_id, service_code, role_code, operation_type, operation_status, result_version, result_use_yn
                      FROM mbr_member_role_operation
                     WHERE idempotency_key = ?
                    """, idempotencyKey);
            boolean same = String.valueOf(existing.get("member_id")).equals(String.valueOf(memberId))
                    && serviceCode.equals(String.valueOf(existing.get("service_code")))
                    && roleCode.trim().equals(String.valueOf(existing.get("role_code")))
                    && operationType.equals(String.valueOf(existing.get("operation_type")));
            if (!same) throw new CpfBusinessException(CpfErrorCode.CONFLICT, "멱등 키가 다른 권한 요청에 이미 사용되었습니다.");
            return false;
        }
    }

    private Map<String, Object> idempotentRoleResult(
            long memberId, String roleCode, String serviceCode, String idempotencyKey) {
        Map<String, Object> operation = jdbcTemplate.queryForMap("""
                SELECT operation_status, result_version, result_use_yn, created_at, completed_at
                  FROM mbr_member_role_operation
                 WHERE idempotency_key = ?
                """, idempotencyKey);
        if (!"SUCCEEDED".equals(String.valueOf(operation.get("operation_status")))) {
            throw new CpfBusinessException(CpfErrorCode.CONFLICT, "동일 멱등 키의 권한 요청이 아직 완료되지 않았습니다.");
        }
        Map<String, Object> current = findRole(memberId, roleCode, serviceCode);
        Map<String, Object> result = beforeAfter(current, current);
        result.put("idempotentReplay", true);
        result.put("idempotencyKey", idempotencyKey);
        result.put("originalResult", operation);
        result.put("current", current);
        return result;
    }

    private void completeRoleOperation(String idempotencyKey, Map<String, Object> after) {
        int updated = jdbcTemplate.update("""
                UPDATE mbr_member_role_operation
                   SET operation_status = 'SUCCEEDED', result_version = ?, result_use_yn = ?, completed_at = CURRENT_TIMESTAMP(3)
                 WHERE idempotency_key = ? AND operation_status = 'PENDING'
                """, after.get("version_no"), after.get("use_yn"), idempotencyKey);
        if (updated != 1) {
            throw new CpfBusinessException(CpfErrorCode.CONFLICT, "권한 변경 멱등 처리 상태를 확정하지 못했습니다.");
        }
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
