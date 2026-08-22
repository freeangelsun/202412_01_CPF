package member.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import member.sample.service.SampleTransactionService;
import member.sample.dto.SampleIdRequest;
import member.sample.model.SampleItem;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction MBR_SAMPLE_TX_DETAIL의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public final class SampleDetailDomainOperation implements CpfDomainOperation<SampleIdRequest, SampleItem> {
    private final SampleTransactionService service;
    public SampleDetailDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "MBR"; }
    @Override public String operationId() { return "MBR_SAMPLE_TX_DETAIL"; }
    @Override public Class<SampleIdRequest> requestType() { return SampleIdRequest.class; }
    @Override public Class<SampleItem> responseType() { return SampleItem.class; }
    @Override public CpfResult<SampleItem> invoke(SampleIdRequest request) { return CpfResult.success(service.detail(request.id())); }
}
