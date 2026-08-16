package com.cpf.core.api.error;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class CpfExceptionTaxonomyTest {
    @Test
    void businessReferenceDoesNotSynthesizeMessageCode() {
        var error = new CpfBusinessException("ORG-BUS-4711", Map.of("id", "A"));
        assertThat(error.getErrorReference()).isEqualTo("ORG-BUS-4711");
        assertThat(error.getErrorCode()).isEqualTo(CpfErrorCode.BUSINESS_RULE_VIOLATION);
        assertThat(error.getErrorCode().messageCode()).isEqualTo("MCPF020001");
    }

    @Test
    void systemAndValidationRemainSemanticallyDistinct() {
        assertThat(new CpfSystemException("failure").getErrorCode().category())
                .isEqualTo(CpfErrorDefinition.Category.INTERNAL);
        assertThat(new CpfValidationException("bad field").getErrorCode().category())
                .isEqualTo(CpfErrorDefinition.Category.VALIDATION);
    }
}
