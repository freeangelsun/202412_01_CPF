package com.cpf.reference.servicecall;

import com.cpf.core.common.servicecall.CpfPolicyId;
import com.cpf.core.common.servicecall.CpfServiceCallOptions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceServiceCallEngineEducationSampleTest {

    @Test
    void basicSampleUsesTypedClientAndCentralDefaultPolicy() {
        AtomicReference<ReferenceServiceEchoRequest> capturedRequest = new AtomicReference<>();
        AtomicReference<CpfServiceCallOptions> capturedOptions = new AtomicReference<>();
        ReferenceServiceEchoClient client = (request, options) -> {
            capturedRequest.set(request);
            capturedOptions.set(options);
            return new ReferenceServiceEchoResponse(
                    request.requestKey(),
                    "200",
                    "2026-07-27T00:00:00Z");
        };
        ReferenceServiceCallEngineEducationSample sample = new ReferenceServiceCallEngineEducationSample(client);

        ReferenceServiceEchoResponse result = sample.callEcho("REF-REQUEST-1");

        assertThat(result.statusCode()).isEqualTo("200");
        assertThat(capturedRequest.get().requestKey()).isEqualTo("REF-REQUEST-1");
        assertThat(capturedOptions.get().policyId()).isEqualTo(CpfPolicyId.DEFAULT_QUERY);
    }
}
