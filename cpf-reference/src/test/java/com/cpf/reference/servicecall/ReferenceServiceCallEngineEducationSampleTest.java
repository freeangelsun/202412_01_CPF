package cpf.xyz.servicecall;

import cpf.pfw.common.servicecall.CpfPolicyId;
import cpf.pfw.common.servicecall.CpfServiceCallOptions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class XyzServiceCallEngineEducationSampleTest {

    @Test
    void basicSampleUsesTypedClientAndCentralDefaultPolicy() {
        AtomicReference<XyzMemberSummaryRequest> capturedRequest = new AtomicReference<>();
        AtomicReference<CpfServiceCallOptions> capturedOptions = new AtomicReference<>();
        XyzMemberSummaryClient client = (request, options) -> {
            capturedRequest.set(request);
            capturedOptions.set(options);
            return new XyzMemberSummaryResponse(request.memberNo(), "홍*동", "ACTIVE");
        };
        XyzServiceCallEngineEducationSample sample = new XyzServiceCallEngineEducationSample(client);

        XyzMemberSummaryResponse result = sample.callMemberSummary("M-1");

        assertThat(result.statusCode()).isEqualTo("ACTIVE");
        assertThat(capturedRequest.get().memberNo()).isEqualTo("M-1");
        assertThat(capturedOptions.get().policyId()).isEqualTo(CpfPolicyId.DEFAULT_QUERY);
    }
}
