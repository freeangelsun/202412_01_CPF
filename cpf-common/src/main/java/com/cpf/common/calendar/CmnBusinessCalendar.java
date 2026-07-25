package com.cpf.common.calendar;
import java.time.LocalDate;
/** 고객사 영업일/휴일 정책의 공개 계약입니다. */
public interface CmnBusinessCalendar { boolean isBusinessDay(String calendarId, LocalDate date); LocalDate nextBusinessDay(String calendarId, LocalDate from, int offset); }
