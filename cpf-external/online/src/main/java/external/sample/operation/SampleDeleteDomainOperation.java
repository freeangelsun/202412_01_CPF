package external.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import external.sample.service.SampleTransactionService;
import external.sample.dto.DeleteSampleCommand;
import external.sample.model.SampleItem;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction EXS_SAMPLE_TX_DELETE의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public final class SampleDeleteDomainOperation implements CpfDomainOperation<DeleteSampleCommand, SampleItem> {
    private final SampleTransactionService service;
    public SampleDeleteDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "EXS"; }
    @Override public String operationId() { return "EXS_SAMPLE_TX_DELETE"; }
    @Override public Class<DeleteSampleCommand> requestType() { return DeleteSampleCommand.class; }
    @Override public Class<SampleItem> responseType() { return SampleItem.class; }
    @Override public CpfResult<SampleItem> invoke(DeleteSampleCommand request) { return CpfResult.success(service.delete(request.id(), request.request())); }
}
