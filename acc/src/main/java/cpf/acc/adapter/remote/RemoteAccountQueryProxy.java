package cpf.acc.adapter.remote;

import cpf.acc.dto.AccountSearchRequest;
import cpf.acc.port.AccountQueryPort;
import cpf.pfw.common.http.CpfWebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

/**
 * 분리 배포된 Account 서비스에 PFW 표준 서비스 호출 경계로 접근하는 remote proxy입니다.
 *
 * <p>프로젝트 설정에서 remote 모드를 선택할 때만 Bean으로 등록합니다.</p>
 */
public class RemoteAccountQueryProxy implements AccountQueryPort {
    private final CpfWebClient webClient;

    public RemoteAccountQueryProxy(CpfWebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Map<String, Object> search(AccountSearchRequest request) {
        return webClient.get(
                "ACC",
                uriBuilder -> {
                    uriBuilder.path("/api/v1/acc")
                            .queryParam("sortBy", request.sortBy())
                            .queryParam("sortDirection", request.sortDirection())
                            .queryParam("page", request.page())
                            .queryParam("size", request.size());
                    if (request.keyword() != null && !request.keyword().isBlank()) {
                        uriBuilder.queryParam("keyword", request.keyword());
                    }
                    return uriBuilder.build();
                },
                new ParameterizedTypeReference<Map<String, Object>>() { });
    }
}