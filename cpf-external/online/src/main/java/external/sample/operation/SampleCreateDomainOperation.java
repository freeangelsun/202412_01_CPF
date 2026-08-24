package external.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import external.sample.service.SampleTransactionService;
import external.sample.dto.CreateSampleRequest;
import external.sample.model.SampleItem;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction EXS_SAMPLE_TX_CREATE의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public class SampleCreateDomainOperation implements CpfDomainOperation<CreateSampleRequest, SampleItem> {
    private final SampleTransactionService service;
    public SampleCreateDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "EXS"; }
    @Override public String operationId() { return "EXS_SAMPLE_TX_CREATE"; }
    @Override public Class<CreateSampleRequest> requestType() { return CreateSampleRequest.class; }
    @Override public Class<SampleItem> responseType() { return SampleItem.class; }
    @Override public CpfResult<SampleItem> invoke(CreateSampleRequest request) { return CpfResult.success(service.create(request)); }
}
