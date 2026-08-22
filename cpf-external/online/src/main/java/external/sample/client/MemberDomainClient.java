package external.sample.client;

import com.cpf.core.api.result.CpfResult;
import com.cpf.core.api.domain.CpfDomainPingRequest;
import com.cpf.core.api.domain.CpfDomainPingResponse;

/** MBR 논리 Domain의 선택 Operation을 배포 위치와 무관하게 호출하는 Generated Typed Client입니다. */
public interface MemberDomainClient {
    CpfResult<CpfDomainPingResponse> ping(CpfDomainPingRequest request);
}
