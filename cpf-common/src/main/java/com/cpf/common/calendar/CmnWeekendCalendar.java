package com.cpf.common.calendar;
import java.time.DayOfWeek;import java.time.LocalDate;
/** DB 없이 사용할 수 있는 기본 주말 Calendar. 고객사는 Bean/SPI로 교체합니다. */
public final class CmnWeekendCalendar implements CmnBusinessCalendar {
 public boolean isBusinessDay(String id,LocalDate d){DayOfWeek w=d.getDayOfWeek();return w!=DayOfWeek.SATURDAY&&w!=DayOfWeek.SUNDAY;}
 public LocalDate nextBusinessDay(String id,LocalDate from,int offset){if(offset==0)return normalize(id,from,1);int dir=offset>0?1:-1,remain=Math.abs(offset);LocalDate d=from;while(remain>0){d=d.plusDays(dir);if(isBusinessDay(id,d))remain--;}return d;}
 private LocalDate normalize(String id,LocalDate d,int dir){LocalDate x=d;while(!isBusinessDay(id,x))x=x.plusDays(dir);return x;}
}
