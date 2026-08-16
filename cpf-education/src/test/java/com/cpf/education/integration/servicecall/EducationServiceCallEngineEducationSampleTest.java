package com.cpf.education.integration.servicecall;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class EducationServiceCallEngineEducationSampleTest {

    @Test
    void basicSampleUsesPublicTypedClientBoundary() {
        AtomicReference<EducationServiceEchoRequest> capturedRequest = new AtomicReference<>();
        EducationServiceEchoClient client = request -> {
            capturedRequest.set(request);
            return new EducationServiceEchoResponse(request.requestKey(), "200", "2026-07-27T00:00:00Z");
        };
        EducationServiceCallEngineEducationSample sample = new EducationServiceCallEngineEducationSample(client);

        EducationServiceEchoResponse result = sample.callEcho("EDU-REQUEST-1");

        assertThat(result.statusCode()).isEqualTo("200");
        assertThat(capturedRequest.get().requestKey()).isEqualTo("EDU-REQUEST-1");
    }
}
