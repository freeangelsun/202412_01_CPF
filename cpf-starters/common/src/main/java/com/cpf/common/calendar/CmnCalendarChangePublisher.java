package com.cpf.common.calendar;

/**
 * 외부 cache/Redis/broker 기반 Calendar 변경 전파 adapter의 확장 Port입니다.
 * 기본 JDBC Store는 조회마다 canonical DB를 읽으므로 이 Port가 없어도 stale local cache를 만들지 않습니다.
 */
@FunctionalInterface
public interface CmnCalendarChangePublisher {
    void publish(CmnCalendarChangeEvent event);

    static CmnCalendarChangePublisher noop() { return event -> { }; }
}
