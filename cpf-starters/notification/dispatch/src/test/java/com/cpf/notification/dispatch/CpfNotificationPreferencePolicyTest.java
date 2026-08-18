package com.cpf.notification.dispatch;

import com.cpf.notification.api.CpfNotificationRequest;import java.time.*;import java.util.*;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
class CpfNotificationPreferencePolicyTest {
 @Test void defersDuringQuietHours(){var p=new CpfNotificationPreferencePolicy();p.replace("user",new CpfNotificationPreferencePolicy.Preference(Set.of("EMAIL"),ZoneOffset.UTC,LocalTime.of(22,0),LocalTime.of(7,0)));var r=new CpfNotificationRequest("n1","EMAIL","user","t",Map.of(),"i",null);var d=p.evaluate(r,Clock.fixed(Instant.parse("2026-08-02T23:00:00Z"),ZoneOffset.UTC));assertThat(d.allowed()).isFalse();assertThat(d.resumeAt()).isEqualTo(Instant.parse("2026-08-03T07:00:00Z"));}
}
