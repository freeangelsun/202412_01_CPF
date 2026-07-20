package cpf.xyz.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzOperationTraceEducationSampleTest {

    @Test
    void traceKeysContainAdmLink() {
        assertThat(new XyzOperationTraceEducationSample().traceKeys("T-1", "/api/v1/xyz"))
                .containsEntry("admLink", "/adm/opr/logs?transactionGlobalId=T-1");
    }
}
