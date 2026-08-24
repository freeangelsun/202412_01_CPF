package external.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import external.sample.service.SampleTransactionService;
import external.sample.dto.UpdateSampleCommand;
import external.sample.model.SampleItem;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction EXS_SAMPLE_TX_UPDATE의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public class SampleUpdateDomainOperation implements CpfDomainOperation<UpdateSampleCommand, SampleItem> {
    private final SampleTransactionService service;
    public SampleUpdateDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "EXS"; }
    @Override public String operationId() { return "EXS_SAMPLE_TX_UPDATE"; }
    @Override public Class<UpdateSampleCommand> requestType() { return UpdateSampleCommand.class; }
    @Override public Class<SampleItem> responseType() { return SampleItem.class; }
    @Override public CpfResult<SampleItem> invoke(UpdateSampleCommand request) { return CpfResult.success(service.update(request.id(), request.request())); }
}
