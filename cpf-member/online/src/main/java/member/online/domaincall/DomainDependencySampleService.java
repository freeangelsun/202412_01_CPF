package member.online.domaincall;

import com.cpf.core.api.domain.CpfDomainPingRequest;
import com.cpf.core.api.domain.CpfDomainPingResponse;
import com.cpf.core.api.result.CpfResult;
import com.cpf.foundation.annotation.CpfService;

/** Generated Domain dependency Client를 실제 업무 Bean 주입 경로에서 소비하는 Sample Service입니다. */
@CpfService
public class DomainDependencySampleService {
    private final ExternalDomainClient externalDomainClient;
    public DomainDependencySampleService(ExternalDomainClient externalDomainClient) {
        this.externalDomainClient = externalDomainClient;
    }
    /** EXS Domain의 Local/Remote 동일 호출을 실제 Consumer로 검증합니다. */
    public CpfResult<CpfDomainPingResponse> probeExternal(String requestId) {
        return externalDomainClient.execute(new CpfDomainPingRequest(requestId));
    }
}
