package com.cpf.batch.runtime;
import com.cpf.common.calendar.api.CpfCalendarService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import java.time.*;
@Configuration
public class DefaultBusinessCalendarConfiguration {
 @Bean @ConditionalOnMissingBean(CpfCalendarService.class)
 CpfCalendarService defaultBusinessCalendar(){return new CpfCalendarService(){
  public boolean isBusinessDay(String id,LocalDate d){if(!"DEFAULT".equalsIgnoreCase(id))throw new IllegalStateException("Configured business calendar required: "+id);return d.getDayOfWeek()!=DayOfWeek.SATURDAY&&d.getDayOfWeek()!=DayOfWeek.SUNDAY;}
  public LocalDate shiftBusinessDay(String id,LocalDate from,int offset){if(offset==0)return from;int dir=offset>0?1:-1,remain=Math.abs(offset);LocalDate d=from;while(remain>0){d=d.plusDays(dir);if(isBusinessDay(id,d))remain--;}return d;}
 };}
}
