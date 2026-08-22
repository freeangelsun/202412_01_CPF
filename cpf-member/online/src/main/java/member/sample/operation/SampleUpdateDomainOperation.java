package member.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import member.sample.service.SampleTransactionService;
import member.sample.dto.UpdateSampleCommand;
import member.sample.model.SampleItem;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction MBR_SAMPLE_TX_UPDATE의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public final class SampleUpdateDomainOperation implements CpfDomainOperation<UpdateSampleCommand, SampleItem> {
    private final SampleTransactionService service;
    public SampleUpdateDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "MBR"; }
    @Override public String operationId() { return "MBR_SAMPLE_TX_UPDATE"; }
    @Override public Class<UpdateSampleCommand> requestType() { return UpdateSampleCommand.class; }
    @Override public Class<SampleItem> responseType() { return SampleItem.class; }
    @Override public CpfResult<SampleItem> invoke(UpdateSampleCommand request) { return CpfResult.success(service.update(request.id(), request.request())); }
}
