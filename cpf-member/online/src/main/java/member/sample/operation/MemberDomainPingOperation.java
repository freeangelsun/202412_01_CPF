package member.sample.operation;

import com.cpf.core.api.domain.CpfDomainPingRequest;
import com.cpf.core.api.domain.CpfDomainPingResponse;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import java.time.Clock;
import org.springframework.stereotype.Component;

/** Local/Remote 동일 Domain Call 경로를 실제 Runtime에서 검증하는 Generated managed operation입니다. */
@Component
public class MemberDomainPingOperation implements CpfDomainOperation<CpfDomainPingRequest, CpfDomainPingResponse> {
    private final Clock clock;
    public MemberDomainPingOperation(Clock clock) { this.clock = clock; }
    @Override public String systemCode() { return "MBR"; }
    @Override public String operationId() { return "ping"; }
    @Override public Class<CpfDomainPingRequest> requestType() { return CpfDomainPingRequest.class; }
    @Override public Class<CpfDomainPingResponse> responseType() { return CpfDomainPingResponse.class; }
    @Override public CpfResult<CpfDomainPingResponse> invoke(CpfDomainPingRequest request) {
        return CpfResult.success(new CpfDomainPingResponse("MBR", request.requestId(), clock.instant()));
    }
}
