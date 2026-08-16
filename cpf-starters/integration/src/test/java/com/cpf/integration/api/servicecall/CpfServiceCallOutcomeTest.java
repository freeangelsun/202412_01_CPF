package com.cpf.integration.api.servicecall;

import com.cpf.core.api.result.CpfResultStatus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CpfServiceCallOutcomeTest {

    @Test
    void exposesTypedBoundaryResultWithoutBreakingLegacyStatus() {
        var legacyFailure = outcome("FAILED");
        assertThat(legacyFailure.resultStatus()).isEqualTo(CpfResultStatus.TECHNICAL_FAILURE);
        assertThat(legacyFailure.technicalFailure()).isTrue();

        var businessFailure = outcome("BUSINESS_FAILURE");
        assertThat(businessFailure.businessFailure()).isTrue();

        var unknown = outcome("UNKNOWN_RESULT");
        assertThat(unknown.unknown()).isTrue();
        assertThat(unknown.success()).isFalse();
    }

    @Test
    void unknownStatusFailsClosedAsUnknown() {
        assertThat(outcome("provider-specific-new-state").resultStatus())
                .isEqualTo(CpfResultStatus.UNKNOWN);
    }

    private static CpfServiceCallOutcome<Void> outcome(String status) {
        return new CpfServiceCallOutcome<>(status, null, null, null, 0L, 1, null, null);
    }
}
