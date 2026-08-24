package external.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import external.sample.service.SampleTransactionService;
import external.sample.dto.SampleIdRequest;
import external.sample.model.SampleItem;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction EXS_SAMPLE_TX_DETAIL의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public class SampleDetailDomainOperation implements CpfDomainOperation<SampleIdRequest, SampleItem> {
    private final SampleTransactionService service;
    public SampleDetailDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "EXS"; }
    @Override public String operationId() { return "EXS_SAMPLE_TX_DETAIL"; }
    @Override public Class<SampleIdRequest> requestType() { return SampleIdRequest.class; }
    @Override public Class<SampleItem> responseType() { return SampleItem.class; }
    @Override public CpfResult<SampleItem> invoke(SampleIdRequest request) { return CpfResult.success(service.detail(request.id())); }
}
