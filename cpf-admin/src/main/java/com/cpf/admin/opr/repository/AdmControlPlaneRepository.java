package com.cpf.admin.opr.repository;

import com.cpf.admin.common.base.AdmBaseRepository;
import com.cpf.data.persistence.api.CpfRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ADM Control-Plane의 CPF_PLATFORM_DB(cpfDB) Persistence Owner입니다.
 * Vendor 전용 LIMIT/OFFSET 문법 없이 JDBC maxRows와 bind parameter만 사용합니다.
 */
// @CpfRepository 는 @Repository stereotype 이므로 Runtime 이 예외변환 Advice 를 위해 CGLIB
// 프록시를 생성한다. final 클래스는 subclass 를 만들 수 없어 기동 자체가 실패한다.
@CpfRepository
public class AdmControlPlaneRepository extends AdmBaseRepository {
    private final JdbcTemplate jdbc;

    public AdmControlPlaneRepository(@Qualifier("admJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = requireValue(jdbc, "admJdbcTemplate");
    }

    /** findIncidents 작업을 CPF 표준 계약에 따라 수행한다. */
    public List<Map<String, Object>> findIncidents(String status, String severity, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT incident_id AS incidentId, incident_no AS incidentNo, severity, title, summary,
                       source_type AS sourceType, source_id AS sourceId, status, detected_at AS detectedAt,
                       acknowledged_at AS acknowledgedAt, mitigated_at AS mitigatedAt,
                       resolved_at AS resolvedAt, created_by AS createdBy, updated_by AS updatedBy,
                       reason, version
                FROM adm_incident
                WHERE 1 = 1
                """);
        ArrayList<Object> args = new ArrayList<>();
        if (hasText(status)) { sql.append(" AND status = ?"); args.add(status.toUpperCase(Locale.ROOT)); }
        if (hasText(severity)) { sql.append(" AND severity = ?"); args.add(severity.toUpperCase(Locale.ROOT)); }
        sql.append("""
                 ORDER BY CASE severity
                            WHEN 'SEV1' THEN 1 WHEN 'SEV2' THEN 2 WHEN 'SEV3' THEN 3 ELSE 4
                          END, detected_at DESC
                """);
        return queryForList(sql.toString(), args, operationPageSize(limit));
    }

    /** createIncident 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String, Object> createIncident(
            String incidentNo, String severity, String title, String summary,
            String sourceType, String sourceId, String operatorId, String reason) {
        jdbc.update("""
            INSERT INTO adm_incident(incident_no,severity,title,summary,source_type,source_id,status,detected_at,created_by,updated_by,reason,version)
            VALUES(?,?,?,?,?,?,'OPEN',CURRENT_TIMESTAMP,?,?,?,0)
            """, incidentNo, severity, title, summary, sourceType, sourceId, operatorId, operatorId, reason);
        return jdbc.queryForMap("SELECT * FROM adm_incident WHERE incident_no=?", incidentNo);
    }

    /** incident 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String, Object> incident(long incidentId) {
        return jdbc.queryForMap("SELECT incident_id,status,version FROM adm_incident WHERE incident_id=?", incidentId);
    }

    public boolean transitionIncident(
            long incidentId, long expectedVersion, String status, String operatorId,
            String reason, String timestampColumn) {
        String timeFragment = switch (timestampColumn == null ? "" : timestampColumn) {
            case "acknowledged_at", "mitigated_at", "resolved_at" -> ", " + timestampColumn + "=CURRENT_TIMESTAMP";
            case "" -> "";
            default -> throw new IllegalArgumentException("Unsupported incident timestamp column");
        };
        String sql = "UPDATE adm_incident SET status=?, updated_by=?, reason=?, version=version+1, updated_at=CURRENT_TIMESTAMP"
                + timeFragment + " WHERE incident_id=? AND version=?";
        return jdbc.update(sql, status, operatorId, reason, incidentId, expectedVersion) == 1;
    }

    /** incidentDetail 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String, Object> incidentDetail(long incidentId) {
        return jdbc.queryForMap("SELECT * FROM adm_incident WHERE incident_id=?", incidentId);
    }

    public List<Map<String, Object>> findMaintenanceActions(int limit) {
        return queryForList("""
                SELECT action_id AS actionId, service_id AS serviceId, endpoint_code AS endpointCode,
                       instance_id AS instanceId, action_type AS actionType, before_status AS beforeStatus,
                       after_status AS afterStatus, result_status AS resultStatus, reason,
                       requested_by AS requestedBy, requested_at AS requestedAt, result_detail AS resultDetail
                FROM adm_maintenance_action
                ORDER BY action_id DESC
                """, List.of(), operationPageSize(limit));
    }

    /** recordMaintenance 작업을 CPF 표준 계약에 따라 수행한다. */
    public void recordMaintenance(
            String serviceId, String endpointCode, String instanceId, String action,
            String before, String after, String status, String reason, String user, String detail) {
        jdbc.update("""
                INSERT INTO adm_maintenance_action(service_id,endpoint_code,instance_id,action_type,before_status,after_status,result_status,reason,requested_by,requested_at,result_detail)
                VALUES(?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?)
                """, serviceId, endpointCode, instanceId, action, before, after, status, reason, user, detail);
    }

    private List<Map<String, Object>> queryForList(String sql, List<?> parameters, int maxRows) {
        List<?> args = parameters == null ? List.of() : parameters;
        return jdbc.query(connection -> {
            var statement = connection.prepareStatement(sql);
            for (int index = 0; index < args.size(); index++) statement.setObject(index + 1, args.get(index));
            statement.setMaxRows(Math.max(1, maxRows));
            return statement;
        }, new ColumnMapRowMapper());
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
}
