package com.cpf.education.common.context;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.testkit.context.CpfContextTestSupport;
import com.cpf.testkit.context.CpfTestContextRuntime;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** 11. Testkit: deterministic root Context + try-with-resources + leak assertion. */
class CpfContextTestkitEducationTest {
    @Test void deterministicContextIsBoundAndCleared() {
        try (var runtime = CpfTestContextRuntime.install()) {
            var support = new CpfContextTestSupport("EDU", LocalDate.of(2026, 8, 9));
            try (var scope = support.bindRoot("EDU-CORR", "EDU-IDEM", "edu-user")) {
                assertThat(CpfContexts.transactionId()).startsWith("EDU-TX-");
                assertThat(CpfContexts.idempotencyKey()).isEqualTo("EDU-IDEM");
            }
            support.assertClear();
        }
    }
}
