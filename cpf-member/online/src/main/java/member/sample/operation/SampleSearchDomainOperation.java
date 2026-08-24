package member.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import member.sample.service.SampleTransactionService;
import member.sample.dto.SampleSearchRequest;
import member.sample.dto.SamplePage;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction MBR_SAMPLE_TX_SEARCH의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public class SampleSearchDomainOperation implements CpfDomainOperation<SampleSearchRequest, SamplePage> {
    private final SampleTransactionService service;
    public SampleSearchDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "MBR"; }
    @Override public String operationId() { return "MBR_SAMPLE_TX_SEARCH"; }
    @Override public Class<SampleSearchRequest> requestType() { return SampleSearchRequest.class; }
    @Override public Class<SamplePage> responseType() { return SamplePage.class; }
    @Override public CpfResult<SamplePage> invoke(SampleSearchRequest request) { return CpfResult.success(service.search(request)); }
}
