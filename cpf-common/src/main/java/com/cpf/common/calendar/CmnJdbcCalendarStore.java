package com.cpf.common.calendar;

import com.cpf.common.persistence.CpfCommonSqlResourceLoader;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Product mode의 CMN canonical Calendar JDBC Adapter입니다. */
public class CmnJdbcCalendarStore implements CmnCalendarStore {
    private final JdbcTemplate jdbc;
    public CmnJdbcCalendarStore(DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }
    @Override public Optional<CmnCalendarDay> find(String calendarId, LocalDate businessDate) {
        return jdbc.query(CpfCommonSqlResourceLoader.load("calendar/find.sql"),
                (rs,n)->map(rs), calendarId,businessDate).stream().findFirst();
    }
    @Override public List<CmnCalendarDay> findRange(String calendarId, LocalDate from, LocalDate to, int limit) {
        LocalDate safeFrom=from==null?LocalDate.of(1900,1,1):from; LocalDate safeTo=to==null?LocalDate.of(2999,12,31):to;
        return jdbc.query(con->{var st=con.prepareStatement(CpfCommonSqlResourceLoader.load("calendar/find-range.sql")); st.setString(1,calendarId); st.setObject(2,safeFrom); st.setObject(3,safeTo); st.setMaxRows(Math.max(1,Math.min(limit,1000))); return st;},(rs,n)->map(rs));
    }
    @Override public CmnCalendarDay save(CmnCalendarDay day,long expectedVersion){ throw new IllegalStateException("CmnJdbcCalendarStore mutation은 operatorId가 필수입니다."); }
    @Override public CmnCalendarDay save(CmnCalendarDay day,long expectedVersion,String operatorId){
        String actor=required(operatorId,"operatorId");
        if(expectedVersion==0){
            try { jdbc.update(CpfCommonSqlResourceLoader.load("calendar/insert.sql"),day.calendarId(),day.businessDate(),yn(day.businessDay()),day.dayType(),day.institutionCode(),day.reason(),actor,actor); }
            catch(DuplicateKeyException ex){ throw new CmnCalendarConflictException(CmnCalendarConflictException.Type.CREATE_CONFLICT,"Calendar 신규 등록 경쟁이 발생했습니다."); }
        } else {
            int updated=jdbc.update(CpfCommonSqlResourceLoader.load("calendar/update.sql"),yn(day.businessDay()),day.dayType(),day.institutionCode(),day.reason(),actor,day.calendarId(),day.businessDate(),expectedVersion);
            if(updated!=1) throw new CmnCalendarConflictException(CmnCalendarConflictException.Type.VERSION_CONFLICT,"Calendar version 충돌입니다.");
        }
        return find(day.calendarId(),day.businessDate()).orElseThrow(()->new CmnCalendarConflictException(CmnCalendarConflictException.Type.NOT_FOUND,"Calendar 저장 결과를 찾을 수 없습니다."));
    }
    @Override public void delete(String calendarId,LocalDate businessDate,long expectedVersion){ throw new IllegalStateException("CmnJdbcCalendarStore mutation은 operatorId가 필수입니다."); }
    @Override public void delete(String calendarId,LocalDate businessDate,long expectedVersion,String operatorId){
        required(operatorId,"operatorId"); int deleted=jdbc.update(CpfCommonSqlResourceLoader.load("calendar/delete.sql"),calendarId,businessDate,expectedVersion);
        if(deleted!=1) throw new CmnCalendarConflictException(CmnCalendarConflictException.Type.DELETE_CONFLICT,"Calendar delete version 충돌 또는 대상 없음입니다.");
    }
    @Override public boolean actorAwareMutations(){return true;}
    private CmnCalendarDay map(java.sql.ResultSet rs)throws java.sql.SQLException{return new CmnCalendarDay(rs.getString("calendar_id"),rs.getObject("business_date",LocalDate.class),"Y".equalsIgnoreCase(rs.getString("business_day_yn")),rs.getString("day_type"),rs.getString("institution_code"),rs.getString("reason"),rs.getLong("version_no"));}
    private String yn(boolean v){return v?"Y":"N";} private String required(String v,String f){if(v==null||v.isBlank())throw new IllegalArgumentException(f+"은 필수입니다.");return v.trim();}
}
