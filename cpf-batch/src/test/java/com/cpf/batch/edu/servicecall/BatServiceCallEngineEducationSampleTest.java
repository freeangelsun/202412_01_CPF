package com.cpf.batch.edu.servicecall;

import com.cpf.core.common.servicecall.CpfPolicyId;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class BatServiceCallEngineEducationSampleTest {

    @Test
    void serviceCallSampleUsesTypedClientAndDefaultPolicy() {
        AtomicReference<CpfPolicyId> policyId = new AtomicReference<>();
        BatMemberGradeClient client = (request, options) -> {
            policyId.set(options.policyId());
            return new BatMemberGradeResponse(request.memberNo(), "GOLD");
        };
        BatServiceCallEngineEducationSample sample = new BatServiceCallEngineEducationSample(client);

        BatMemberGradeResponse result = sample.callMemberGrade("M-1");

        assertThat(result.gradeCode()).isEqualTo("GOLD");
        assertThat(policyId.get()).isEqualTo(CpfPolicyId.DEFAULT_QUERY);
    }
}
