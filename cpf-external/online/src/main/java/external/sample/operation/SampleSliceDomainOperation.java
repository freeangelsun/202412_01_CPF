package external.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import external.sample.service.SampleTransactionService;
import external.sample.dto.SampleSearchRequest;
import external.sample.dto.SampleSlice;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction EXS_SAMPLE_TX_SLICE의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public final class SampleSliceDomainOperation implements CpfDomainOperation<SampleSearchRequest, SampleSlice> {
    private final SampleTransactionService service;
    public SampleSliceDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "EXS"; }
    @Override public String operationId() { return "EXS_SAMPLE_TX_SLICE"; }
    @Override public Class<SampleSearchRequest> requestType() { return SampleSearchRequest.class; }
    @Override public Class<SampleSlice> responseType() { return SampleSlice.class; }
    @Override public CpfResult<SampleSlice> invoke(SampleSearchRequest request) { return CpfResult.success(service.slice(request)); }
}
