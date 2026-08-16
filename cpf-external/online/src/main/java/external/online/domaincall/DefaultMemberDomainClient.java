package external.online.domaincall;

import com.cpf.core.api.domain.CpfDomainPingRequest;
import com.cpf.core.api.domain.CpfDomainPingResponse;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainClientRouter;
import org.springframework.stereotype.Component;

/** MBR Client를 CPF Domain Binding Resolver의 LOCAL/REMOTE 동일 경로에 연결합니다. */
@Component
public final class DefaultMemberDomainClient implements MemberDomainClient {
    private final CpfDomainClientRouter router;
    public DefaultMemberDomainClient(CpfDomainClientRouter router) { this.router = router; }
    @Override public CpfResult<CpfDomainPingResponse> execute(CpfDomainPingRequest request) {
        return router.invoke("MBR", "ping", request, CpfDomainPingResponse.class);
    }
}
