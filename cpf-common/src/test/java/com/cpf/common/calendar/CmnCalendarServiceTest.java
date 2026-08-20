package com.cpf.common.calendar;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/** CMN 영업일 정책의 주말 기본값, Override, 낙관적 버전 동작을 검증합니다. */
class CmnCalendarServiceTest {
    @Test
    void weekend기본정책과override와version을함께적용한다() {
        CmnInMemoryCalendarStore store = new CmnInMemoryCalendarStore();
        CmnCalendarService service = new CmnCalendarService(store);
        LocalDate saturday = LocalDate.of(2026, 7, 25);
        assertFalse(service.isBusinessDay("DEFAULT", saturday));

        CmnCalendarDay saved = service.save(
                new CmnCalendarDay("DEFAULT", saturday, true, "SPECIAL_OPEN", "BANK", "특별 영업", 0), 0);
        assertEquals(1, saved.version());
        assertTrue(service.isBusinessDay("DEFAULT", saturday));
        assertEquals(saturday,
                service.shiftBusinessDay("DEFAULT", LocalDate.of(2026, 7, 24), 1),
                "특별 영업으로 Override한 토요일은 첫 번째 영업일이어야 함");
        assertEquals(LocalDate.of(2026, 7, 27),
                service.shiftBusinessDay("DEFAULT", LocalDate.of(2026, 7, 24), 2),
                "Override가 없는 일요일을 건너뛴 두 번째 영업일은 월요일이어야 함");
    }

    @Test
    void dbLessWeekendStoreIsReadOnly() {
        CmnCalendarService service = new CmnCalendarService(new CmnWeekendCalendarStore());
        assertFalse(service.writable());
        assertThrows(IllegalStateException.class, () -> service.save(
                new CmnCalendarDay("DEFAULT", LocalDate.of(2026,7,27), true, "BUSINESS", "", "test", 0), 0));
    }
}
