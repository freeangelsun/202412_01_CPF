package external.online.external.service;

import external.online.external.client.MemberDomainClient;
import com.cpf.core.api.domain.CpfDomainPingRequest;
import com.cpf.core.api.domain.CpfDomainPingResponse;
import com.cpf.core.api.result.CpfResult;
import com.cpf.foundation.annotation.CpfService;

/** Generated Domain dependency Client를 실제 업무 Bean 주입 경로에서 소비하는 Sample Service입니다. */
@CpfService
public class DomainDependencySampleService {
    private final MemberDomainClient memberDomainClient;
    public DomainDependencySampleService(MemberDomainClient memberDomainClient) {
        this.memberDomainClient = memberDomainClient;
    }
    /** MBR Domain의 Local/Remote 동일 호출을 실제 Consumer로 검증합니다. */
    public CpfResult<CpfDomainPingResponse> probeMember(String requestId) {
        return memberDomainClient.execute(new CpfDomainPingRequest(requestId));
    }
}
