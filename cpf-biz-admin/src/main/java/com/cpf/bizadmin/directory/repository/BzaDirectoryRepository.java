package com.cpf.bizadmin.directory.repository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * BZA 조직/인사 확장 정본 adapter.
 *
 * <p>직원 기본 Profile과 실제 유효 소속/직급/직책을 분리하여 다중 소속과 겸직을 표현합니다.
 * 모든 유효성 판단은 명시적인 기준 시각을 받으며 시스템 현재시각에 암묵적으로 고정하지 않습니다.</p>
 */
@Repository
public class BzaDirectoryRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;

    public BzaDirectoryRepository(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    public List<Map<String, Object>> findPositions() {
        return jdbc().queryForList("""
                SELECT position_code AS positionCode, position_name AS positionName,
                       rank_order AS rankOrder, use_yn AS useYn,
                       created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_position ORDER BY rank_order, position_code
                """, Map.of());
    }

    public int savePosition(Map<String, ?> values) {
        int updated = jdbc().update("""
                UPDATE bza_position
                   SET position_name=:positionName, rank_order=:rankOrder, use_yn=:useYn, updated_by=:operatorId
                 WHERE position_code=:positionCode
                """, values);
        if (updated == 0 && !exists("bza_position", "position_code", values.get("positionCode"))) {
            return jdbc().update("""
                    INSERT INTO bza_position (
                        position_code, position_name, rank_order, use_yn, created_by, updated_by
                    ) VALUES (
                        :positionCode, :positionName, :rankOrder, :useYn, :operatorId, :operatorId
                    )
                    """, values);
        }
        return updated;
    }

    public List<Map<String, Object>> findJobTitles() {
        return jdbc().queryForList("""
                SELECT job_title_code AS jobTitleCode, job_title_name AS jobTitleName,
                       manager_yn AS managerYn, use_yn AS useYn,
                       created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_job_title ORDER BY job_title_code
                """, Map.of());
    }

    public int saveJobTitle(Map<String, ?> values) {
        int updated = jdbc().update("""
                UPDATE bza_job_title
                   SET job_title_name=:jobTitleName, manager_yn=:managerYn, use_yn=:useYn, updated_by=:operatorId
                 WHERE job_title_code=:jobTitleCode
                """, values);
        if (updated == 0 && !exists("bza_job_title", "job_title_code", values.get("jobTitleCode"))) {
            return jdbc().update("""
                    INSERT INTO bza_job_title (
                        job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by
                    ) VALUES (
                        :jobTitleCode, :jobTitleName, :managerYn, :useYn, :operatorId, :operatorId
                    )
                    """, values);
        }
        return updated;
    }

    public List<Map<String, Object>> findAssignments(String employeeNo, String organizationCode, Instant effectiveAt) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("employeeNo", emptyToNull(employeeNo))
                .addValue("organizationCode", emptyToNull(organizationCode))
                .addValue("effectiveAt", Timestamp.from(effectiveAt));
        return jdbc().queryForList("""
                SELECT assignment_id AS assignmentId, employee_no AS employeeNo,
                       organization_code AS organizationCode, position_code AS positionCode,
                       job_title_code AS jobTitleCode, assignment_type AS assignmentType,
                       primary_yn AS primaryYn, effective_from AS effectiveFrom, effective_to AS effectiveTo,
                       created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_employee_assignment
                 WHERE (:employeeNo IS NULL OR employee_no = :employeeNo)
                   AND (:organizationCode IS NULL OR organization_code = :organizationCode)
                   AND effective_from <= :effectiveAt
                   AND (effective_to IS NULL OR effective_to > :effectiveAt)
                 ORDER BY employee_no, primary_yn DESC, effective_from DESC, assignment_id
                """, p);
    }

    public long countOverlappingPrimaryAssignment(
            String employeeNo, Instant effectiveFrom, Instant effectiveTo, Long excludeAssignmentId) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("employeeNo", employeeNo)
                .addValue("effectiveFrom", Timestamp.from(effectiveFrom))
                .addValue("effectiveTo", effectiveTo == null ? null : Timestamp.from(effectiveTo))
                .addValue("excludeId", excludeAssignmentId);
        Long count = jdbc().queryForObject("""
                SELECT COUNT(*)
                  FROM bza_employee_assignment
                 WHERE employee_no = :employeeNo
                   AND primary_yn = 'Y'
                   AND (:excludeId IS NULL OR assignment_id <> :excludeId)
                   AND (effective_to IS NULL OR effective_to > :effectiveFrom)
                   AND (:effectiveTo IS NULL OR effective_from < :effectiveTo)
                """, p, Long.class);
        return count == null ? 0L : count;
    }

    public int saveAssignment(Map<String, ?> values) {
        Object assignmentId = values.get("assignmentId");
        if (assignmentId == null) {
            return jdbc().update("""
                    INSERT INTO bza_employee_assignment (
                        employee_no, organization_code, position_code, job_title_code,
                        assignment_type, primary_yn, effective_from, effective_to,
                        created_by, updated_by
                    ) VALUES (
                        :employeeNo, :organizationCode, :positionCode, :jobTitleCode,
                        :assignmentType, :primaryYn, :effectiveFrom, :effectiveTo,
                        :operatorId, :operatorId
                    )
                    """, values);
        }
        return jdbc().update("""
                UPDATE bza_employee_assignment
                   SET employee_no = :employeeNo,
                       organization_code = :organizationCode,
                       position_code = :positionCode,
                       job_title_code = :jobTitleCode,
                       assignment_type = :assignmentType,
                       primary_yn = :primaryYn,
                       effective_from = :effectiveFrom,
                       effective_to = :effectiveTo,
                       updated_by = :operatorId
                 WHERE assignment_id = :assignmentId
                """, values);
    }

    public List<Map<String, Object>> findResponsibilities(String organizationCode, Instant effectiveAt) {
        return jdbc().queryForList("""
                SELECT responsibility_id AS responsibilityId, organization_code AS organizationCode,
                       responsibility_type AS responsibilityType, employee_no AS employeeNo,
                       effective_from AS effectiveFrom, effective_to AS effectiveTo,
                       created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_organization_responsibility
                 WHERE (:organizationCode IS NULL OR organization_code = :organizationCode)
                   AND effective_from <= :effectiveAt
                   AND (effective_to IS NULL OR effective_to > :effectiveAt)
                 ORDER BY organization_code, responsibility_type, effective_from DESC
                """, new MapSqlParameterSource()
                .addValue("organizationCode", emptyToNull(organizationCode))
                .addValue("effectiveAt", Timestamp.from(effectiveAt)));
    }

    public int saveResponsibility(Map<String, ?> values) {
        Object id = values.get("responsibilityId");
        if (id == null) {
            return jdbc().update("""
                    INSERT INTO bza_organization_responsibility (
                        organization_code, responsibility_type, employee_no,
                        effective_from, effective_to, created_by, updated_by
                    ) VALUES (
                        :organizationCode, :responsibilityType, :employeeNo,
                        :effectiveFrom, :effectiveTo, :operatorId, :operatorId
                    )
                    """, values);
        }
        return jdbc().update("""
                UPDATE bza_organization_responsibility
                   SET organization_code = :organizationCode,
                       responsibility_type = :responsibilityType,
                       employee_no = :employeeNo,
                       effective_from = :effectiveFrom,
                       effective_to = :effectiveTo,
                       updated_by = :operatorId
                 WHERE responsibility_id = :responsibilityId
                """, values);
    }

    public List<Map<String, Object>> findUserRoles(String loginId, Instant effectiveAt) {
        return jdbc().queryForList("""
                SELECT u.admin_login_id AS loginId, ur.admin_user_id AS adminUserId,
                       ur.role_code AS roleCode, r.role_name AS roleName,
                       ur.valid_from AS validFrom, ur.valid_to AS validTo, ur.primary_yn AS primaryYn
                  FROM bza_user_role ur
                  JOIN bza_admin_user u ON u.admin_user_id = ur.admin_user_id
                  JOIN bza_role r ON r.role_code = ur.role_code
                 WHERE (:loginId IS NULL OR u.admin_login_id = :loginId)
                   AND (ur.valid_from IS NULL OR ur.valid_from <= :effectiveAt)
                   AND (ur.valid_to IS NULL OR ur.valid_to > :effectiveAt)
                 ORDER BY u.admin_login_id, ur.primary_yn DESC, ur.role_code
                """, new MapSqlParameterSource()
                .addValue("loginId", emptyToNull(loginId))
                .addValue("effectiveAt", Timestamp.from(effectiveAt)));
    }

    public int saveUserRole(Map<String, ?> values) {
        List<Map<String,Object>> users = jdbc().queryForList("""
                SELECT admin_user_id AS adminUserId FROM bza_admin_user WHERE admin_login_id=:loginId
                """, values);
        if (users.isEmpty()) return 0;
        MapSqlParameterSource params = new MapSqlParameterSource();
        values.forEach(params::addValue);
        params.addValue("adminUserId", users.get(0).get("adminUserId"));
        int updated = jdbc().update("""
                UPDATE bza_user_role
                   SET valid_from=:validFrom, valid_to=:validTo, primary_yn=:primaryYn, updated_by=:operatorId
                 WHERE admin_user_id=:adminUserId AND role_code=:roleCode
                """, params);
        if (updated == 0) {
            Long count = jdbc().queryForObject("""
                    SELECT COUNT(*) FROM bza_user_role WHERE admin_user_id=:adminUserId AND role_code=:roleCode
                    """, params, Long.class);
            if (count == null || count == 0) {
                return jdbc().update("""
                        INSERT INTO bza_user_role (
                            admin_user_id, role_code, valid_from, valid_to, primary_yn, created_by, updated_by
                        ) VALUES (
                            :adminUserId, :roleCode, :validFrom, :validTo, :primaryYn, :operatorId, :operatorId
                        )
                        """, params);
            }
        }
        return 1;
    }

    private boolean exists(String table, String keyColumn, Object keyValue) {
        if (!java.util.Set.of("bza_position", "bza_job_title").contains(table)
                || !java.util.Set.of("position_code", "job_title_code").contains(keyColumn)) {
            throw new IllegalArgumentException("허용되지 않은 정본 존재 확인입니다.");
        }
        Long count = jdbc().queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + keyColumn + "=:key",
                new MapSqlParameterSource("key", keyValue), Long.class);
        return count != null && count > 0;
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbc = jdbcTemplateProvider.getIfAvailable();
        if (jdbc == null) {
            throw new IllegalStateException("BZA datasource가 구성되지 않았습니다.");
        }
        return jdbc;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
