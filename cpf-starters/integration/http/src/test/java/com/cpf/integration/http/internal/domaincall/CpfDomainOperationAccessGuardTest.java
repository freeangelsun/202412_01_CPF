package com.cpf.integration.http.internal.domaincall;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.cpf.web.api.CpfHttpHeaders;
import com.cpf.web.context.CpfHeaderValidationException;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfRuntimeIdentity;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfDomainOperationAccessGuardTest {
    @Test
    void sameJvmTopologyValidatesResolvedOperationOwnerInsteadOfInventingItsOwnSystem() {
        assertThatCode(() -> CpfDomainOperationAccessGuard.verifyResolvedContract(
                headers("MBR", "MBR"), memberOperation(), topology())).doesNotThrowAnyException();
    }

    @Test
    void sameJvmTopologyRejectsAHeaderThatNamesTheWrongBusinessSystem() {
        assertThatThrownBy(() -> CpfDomainOperationAccessGuard.verifyResolvedContract(
                headers("EXS", "MBR"), memberOperation(), topology()))
                .isInstanceOfSatisfying(CpfHeaderValidationException.class,
                        error -> org.assertj.core.api.Assertions.assertThat(error.category()).isEqualTo("SYSTEM_CODE_MISMATCH"));
    }

    @Test
    void sameJvmTopologyRejectsAHeaderThatNamesTheWrongTargetSystem() {
        assertThatThrownBy(() -> CpfDomainOperationAccessGuard.verifyResolvedContract(
                headers("MBR", "EXS"), memberOperation(), topology()))
                .isInstanceOfSatisfying(CpfHeaderValidationException.class,
                        error -> org.assertj.core.api.Assertions.assertThat(error.category()).isEqualTo("TARGET_SYSTEM_CODE_MISMATCH"));
    }

    private static CpfHttpHeaders headers(String system, String target) {
        return CpfHttpHeaders.captureSingle(Map.of(
                CpfHttpHeaderNames.SYSTEM_CODE, system,
                CpfHttpHeaderNames.TARGET_SYSTEM_CODE, target,
                CpfHttpHeaderNames.TARGET_OPERATION_ID, "MBR_SAMPLE_TX_CREATE"));
    }

    private static CpfRuntimeIdentity topology() {
        return new CpfRuntimeIdentity(null, "DEV", "cpf-local-runtime", "one-was-1");
    }

    private static CpfDomainOperation<TestRequest, TestResponse> memberOperation() {
        return new CpfDomainOperation<>() {
            @Override public String systemCode() { return "MBR"; }
            @Override public String operationId() { return "MBR_SAMPLE_TX_CREATE"; }
            @Override public Class<TestRequest> requestType() { return TestRequest.class; }
            @Override public Class<TestResponse> responseType() { return TestResponse.class; }
            @Override public CpfResult<TestResponse> invoke(TestRequest request) { return CpfResult.success(new TestResponse()); }
        };
    }

    private record TestRequest() implements CpfRequest { }
    private record TestResponse() implements CpfResponse { }
}
