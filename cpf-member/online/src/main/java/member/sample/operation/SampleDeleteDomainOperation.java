package member.sample.operation;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import member.sample.service.SampleTransactionService;
import member.sample.dto.DeleteSampleCommand;
import member.sample.model.SampleItem;
import org.springframework.stereotype.Component;

/** @CpfOnlineTransaction MBR_SAMPLE_TX_DELETE의 typed Same-JVM/Remote 공통 adapter입니다. */
@Component
public class SampleDeleteDomainOperation implements CpfDomainOperation<DeleteSampleCommand, SampleItem> {
    private final SampleTransactionService service;
    public SampleDeleteDomainOperation(SampleTransactionService service) { this.service=service; }
    @Override public String systemCode() { return "MBR"; }
    @Override public String operationId() { return "MBR_SAMPLE_TX_DELETE"; }
    @Override public Class<DeleteSampleCommand> requestType() { return DeleteSampleCommand.class; }
    @Override public Class<SampleItem> responseType() { return SampleItem.class; }
    @Override public CpfResult<SampleItem> invoke(DeleteSampleCommand request) { return CpfResult.success(service.delete(request.id(), request.request())); }
}
