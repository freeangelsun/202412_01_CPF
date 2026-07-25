package com.cpf.common.calendar;
import java.time.LocalDate;import java.util.Map;import java.util.Objects;
/** Calendar resolver. 고객별 기관 Calendar를 등록하고 미등록 시 DEFAULT 정책을 사용합니다. */
public final class CmnCalendarService implements CmnBusinessCalendar {
 private final Map<String,CmnBusinessCalendar> calendars; private final CmnBusinessCalendar fallback;
 public CmnCalendarService(Map<String,CmnBusinessCalendar> calendars,CmnBusinessCalendar fallback){this.calendars=Map.copyOf(calendars==null?Map.of():calendars);this.fallback=Objects.requireNonNullElseGet(fallback,CmnWeekendCalendar::new);}
 private CmnBusinessCalendar resolve(String id){return calendars.getOrDefault(id==null?"DEFAULT":id,fallback);}
 public boolean isBusinessDay(String id,LocalDate d){return resolve(id).isBusinessDay(id,d);}
 public LocalDate nextBusinessDay(String id,LocalDate d,int offset){return resolve(id).nextBusinessDay(id,d,offset);}
}
