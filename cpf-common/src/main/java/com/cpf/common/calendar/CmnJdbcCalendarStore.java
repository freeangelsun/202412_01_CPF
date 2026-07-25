package com.cpf.common.calendar;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 선택형 cmnDB Calendar 영속 Adapter입니다.
 *
 * <p>MariaDB 정식 제품 구성에서는 기본 활성화되며 CMN canonical table을 사용합니다.
 * 고객이 외부 Calendar Store를 연결하는 경우 `cpf.common.calendar.jdbc.enabled=false`로 끄고
 * {@link CmnCalendarStore} 구현을 제공할 수 있습니다.</p>
 */
@Component
@ConditionalOnProperty(name = "cpf.common.calendar.jdbc.enabled", havingValue = "true", matchIfMissing = true)
public class CmnJdbcCalendarStore implements CmnCalendarStore {
    private final JdbcTemplate jdbc;

    public CmnJdbcCalendarStore(@Qualifier("cmnDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<CmnCalendarDay> find(String calendarId, LocalDate businessDate) {
        List<CmnCalendarDay> rows = jdbc.query("""
                SELECT calendar_id,business_date,business_day_yn,day_type,institution_code,reason,version_no
                  FROM cmn_business_calendar_day
                 WHERE calendar_id=? AND business_date=?
                """, (rs, n) -> map(rs), calendarId, businessDate);
        return rows.stream().findFirst();
    }

    @Override
    public List<CmnCalendarDay> findRange(String calendarId, LocalDate from, LocalDate to, int limit) {
        LocalDate safeFrom = from == null ? LocalDate.of(1900, 1, 1) : from;
        LocalDate safeTo = to == null ? LocalDate.of(2999, 12, 31) : to;
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        return jdbc.query(connection -> {
            var statement = connection.prepareStatement("""
                    SELECT calendar_id,business_date,business_day_yn,day_type,institution_code,reason,version_no
                      FROM cmn_business_calendar_day
                     WHERE calendar_id=? AND business_date BETWEEN ? AND ?
                     ORDER BY business_date
                    """);
            statement.setString(1, calendarId);
            statement.setObject(2, safeFrom);
            statement.setObject(3, safeTo);
            statement.setMaxRows(safeLimit);
            return statement;
        }, (rs, n) -> map(rs));
    }

    @Override
    public CmnCalendarDay save(CmnCalendarDay day, long expectedVersion) {
        Optional<CmnCalendarDay> current = find(day.calendarId(), day.businessDate());
        if (current.isEmpty()) {
            if (expectedVersion != 0) throw new IllegalStateException("신규 Calendar expectedVersion은 0이어야 합니다.");
            jdbc.update("""
                    INSERT INTO cmn_business_calendar_day
                    (calendar_id,business_date,business_day_yn,day_type,institution_code,reason,version_no,updated_at)
                    VALUES (?,?,?,?,?,?,1,CURRENT_TIMESTAMP)
                    """, day.calendarId(), day.businessDate(), yn(day.businessDay()), day.dayType(),
                    day.institutionCode(), day.reason());
            return new CmnCalendarDay(day.calendarId(), day.businessDate(), day.businessDay(), day.dayType(),
                    day.institutionCode(), day.reason(), 1);
        }
        int updated = jdbc.update("""
                UPDATE cmn_business_calendar_day
                   SET business_day_yn=?,day_type=?,institution_code=?,reason=?,version_no=version_no+1,updated_at=CURRENT_TIMESTAMP
                 WHERE calendar_id=? AND business_date=? AND version_no=?
                """, yn(day.businessDay()), day.dayType(), day.institutionCode(), day.reason(),
                day.calendarId(), day.businessDate(), expectedVersion);
        if (updated != 1) throw new IllegalStateException("Calendar optimistic lock 충돌입니다.");
        return find(day.calendarId(), day.businessDate()).orElseThrow();
    }

    @Override
    public void delete(String calendarId, LocalDate businessDate, long expectedVersion) {
        int deleted = jdbc.update(
                "DELETE FROM cmn_business_calendar_day WHERE calendar_id=? AND business_date=? AND version_no=?",
                calendarId, businessDate, expectedVersion);
        if (deleted != 1) throw new IllegalStateException("Calendar delete version 충돌 또는 대상 없음입니다.");
    }

    private CmnCalendarDay map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new CmnCalendarDay(
                rs.getString("calendar_id"),
                rs.getObject("business_date", LocalDate.class),
                "Y".equalsIgnoreCase(rs.getString("business_day_yn")),
                rs.getString("day_type"),
                rs.getString("institution_code"),
                rs.getString("reason"),
                rs.getLong("version_no"));
    }

    private String yn(boolean value) { return value ? "Y" : "N"; }
}
