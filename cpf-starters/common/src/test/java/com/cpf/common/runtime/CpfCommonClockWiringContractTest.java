package com.cpf.common.runtime;

import com.cpf.common.calendar.CmnCalendarService;
import com.cpf.common.management.JdbcCpfCommonManagementService;
import com.cpf.common.message.service.CmnCommonCatalogManagementService;
import com.cpf.common.message.service.CmnCpfMessageSource;
import com.cpf.common.message.service.CmnMessageProductAutoConfiguration;
import com.cpf.common.runtime.cache.CpfCommonCacheRefreshListener;
import com.cpf.common.spi.CpfCommonPersistenceNames;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;

class CpfCommonClockWiringContractTest {

    @Test
    void exposesAStableNamedOverrideWithoutClaimingGlobalClockPrecedence() throws Exception {
        Method clock = CpfCommonJdbcAutoConfiguration.class.getDeclaredMethod("cpfCommonClock");

        assertThat(clock.getAnnotation(Bean.class).name())
                .containsExactly(CpfCommonPersistenceNames.CLOCK_BEAN);
        assertThat(clock.getAnnotation(ConditionalOnMissingBean.class).name())
                .containsExactly(CpfCommonPersistenceNames.CLOCK_BEAN);
        assertThat(clock.isAnnotationPresent(Primary.class)).isFalse();
    }

    @Test
    void everySpringManagedCommonConsumerSelectsOnlyTheCommonClock() throws Exception {
        List<Executable> consumers = List.of(
                autowiredConstructor(JdbcCpfCommonManagementService.class),
                autowiredConstructor(CmnCalendarService.class),
                autowiredConstructor(CmnCpfMessageSource.class),
                autowiredConstructor(CmnCommonCatalogManagementService.class),
                autowiredConstructor(CpfCommonCacheRefreshListener.class),
                Arrays.stream(CmnMessageProductAutoConfiguration.class.getDeclaredMethods())
                        .filter(method -> method.getName().equals("cpfCommonErrorCatalogResolver"))
                        .findFirst()
                        .orElseThrow());

        consumers.forEach(CpfCommonClockWiringContractTest::assertCommonClockQualifier);
    }

    private static Executable autowiredConstructor(Class<?> type) {
        return Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .findFirst()
                .orElseThrow();
    }

    private static void assertCommonClockQualifier(Executable executable) {
        var clockParameter = Arrays.stream(executable.getParameters())
                .filter(parameter -> parameter.getType().equals(Clock.class))
                .findFirst()
                .orElseThrow();
        Qualifier qualifier = clockParameter.getAnnotation(Qualifier.class);
        assertThat(qualifier)
                .as("Common Clock qualifier on %s", executable)
                .isNotNull();
        assertThat(qualifier.value()).isEqualTo(CpfCommonPersistenceNames.CLOCK_BEAN);
    }
}
