package com.cpf.core.api.util;

import com.cpf.core.api.page.CpfPage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfDataUtilityApiTest {
    @Test void jsonMapAndTypeConversionRoundTrip() {
        String json = CpfJson.write(Map.of("count", "2", "amount", "10.50", "enabled", "Y"));
        Map<String,Object> map = CpfJson.map(json);
        assertThat(CpfValues.integer(map.get("count"))).isEqualTo(2);
        assertThat(CpfValues.decimal(map.get("amount"))).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(CpfValues.bool(map.get("enabled"))).isTrue();
    }

    @Test void offsetPageUsesCpfStandardPage() {
        CpfPage<String> page = CpfPages.offsetPage(List.of("A","B","C","D"), CpfPages.request(1,2));
        assertThat(page.items()).containsExactly("C","D");
        assertThat(page.totalElements()).isEqualTo(4);
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test void booleanConversionFailsClosed() {
        assertThatThrownBy(() -> CpfValues.bool("maybe")).isInstanceOf(IllegalArgumentException.class);
    }
}
