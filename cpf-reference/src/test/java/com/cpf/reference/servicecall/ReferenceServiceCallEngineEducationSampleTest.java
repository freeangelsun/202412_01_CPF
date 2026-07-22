package com.cpf.reference.servicecall;

import com.cpf.core.common.servicecall.CpfPolicyId;
import com.cpf.core.common.servicecall.CpfServiceCallOptions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceServiceCallEngineEducationSampleTest {

    @Test
    void basicSampleUsesTypedClientAndCentralDefaultPolicy() {
        AtomicReference<ReferenceMemberSummaryRequest> capturedRequest = new AtomicReference<>();
        AtomicReference<CpfServiceCallOptions> capturedOptions = new AtomicReference<>();
        ReferenceMemberSummaryClient client = (request, options) -> {
            capturedRequest.set(request);
            capturedOptions.set(options);
            return new ReferenceMemberSummaryResponse(request.memberNo(), "홍*동", "ACTIVE");
        };
        ReferenceServiceCallEngineEducationSample sample = new ReferenceServiceCallEngineEducationSample(client);

        ReferenceMemberSummaryResponse result = sample.callMemberSummary("M-1");

        assertThat(result.statusCode()).isEqualTo("ACTIVE");
        assertThat(capturedRequest.get().memberNo()).isEqualTo("M-1");
        assertThat(capturedOptions.get().policyId()).isEqualTo(CpfPolicyId.DEFAULT_QUERY);
    }
}
