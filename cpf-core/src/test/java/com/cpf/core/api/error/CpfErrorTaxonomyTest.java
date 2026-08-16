package com.cpf.core.api.error;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfErrorTaxonomyTest {
    @Test void arbitraryBusinessReferenceDoesNotRequireEnumChange() {
        CpfBusinessException ex = new CpfBusinessException("EBANK990777", Map.of("memberId", "42"));
        assertThat(ex.errorReference()).isEqualTo("EBANK990777");
        assertThat(ex.fallbackError()).isEqualTo(CpfErrorCode.BUSINESS_RULE_VIOLATION);
        assertThat(ex.getMessageArguments()).containsEntry("memberId", "42");
    }

    @Test void systemAndValidationHaveIndependentMeaning() {
        assertThat(new CpfSystemException("sensitive internal database detail").fallbackError().exposure())
                .isEqualTo(CpfErrorDefinition.Exposure.GENERIC_MESSAGE_ONLY);
        assertThat(new CpfValidationException("invalid field").fallbackError().category())
                .isEqualTo(CpfErrorDefinition.Category.VALIDATION);
    }
}
