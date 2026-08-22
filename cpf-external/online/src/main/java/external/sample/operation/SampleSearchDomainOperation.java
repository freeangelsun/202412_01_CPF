package external.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import external.sample.service.SampleTransactionService;
import external.sample.dto.SampleSearchRequest;
import external.sample.dto.SamplePage;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction EXS_SAMPLE_TX_SEARCH의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public final class SampleSearchDomainOperation implements CpfDomainOperation<SampleSearchRequest, SamplePage> {
    private final SampleTransactionService service;
    public SampleSearchDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "EXS"; }
    @Override public String operationId() { return "EXS_SAMPLE_TX_SEARCH"; }
    @Override public Class<SampleSearchRequest> requestType() { return SampleSearchRequest.class; }
    @Override public Class<SamplePage> responseType() { return SamplePage.class; }
    @Override public CpfResult<SamplePage> invoke(SampleSearchRequest request) { return CpfResult.success(service.search(request)); }
}
