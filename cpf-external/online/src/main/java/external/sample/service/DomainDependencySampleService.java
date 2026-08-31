package external.sample.service;

import com.cpf.core.api.result.CpfResult;
import external.sample.client.MemberDomainClient;
import com.cpf.core.api.domain.CpfDomainPingRequest;
import com.cpf.core.api.domain.CpfDomainPingResponse;
import external.base.ExternalBaseService;
import com.cpf.foundation.annotation.CpfService;

/** Generated Domain dependency Client를 실제 업무 Bean 주입 경로에서 소비하는 Sample Service입니다. */
@CpfService
public class DomainDependencySampleService extends ExternalBaseService {
    private final MemberDomainClient memberDomainClient;
    public DomainDependencySampleService(MemberDomainClient memberDomainClient) {
        this.memberDomainClient = memberDomainClient;
    }
    /** MBR/ping Domain Operation을 실제 Consumer 경로에서 호출합니다. */
    public CpfResult<CpfDomainPingResponse> pingMember(String requestId) {
        return memberDomainClient.ping(new CpfDomainPingRequest(requestId));
    }
}
