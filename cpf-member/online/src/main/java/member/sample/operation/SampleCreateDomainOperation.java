package member.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import member.sample.service.SampleTransactionService;
import member.sample.dto.CreateSampleRequest;
import member.sample.model.SampleItem;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction MBR_SAMPLE_TX_CREATE의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public final class SampleCreateDomainOperation implements CpfDomainOperation<CreateSampleRequest, SampleItem> {
    private final SampleTransactionService service;
    public SampleCreateDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "MBR"; }
    @Override public String operationId() { return "MBR_SAMPLE_TX_CREATE"; }
    @Override public Class<CreateSampleRequest> requestType() { return CreateSampleRequest.class; }
    @Override public Class<SampleItem> responseType() { return SampleItem.class; }
    @Override public CpfResult<SampleItem> invoke(CreateSampleRequest request) { return CpfResult.success(service.create(request)); }
}
