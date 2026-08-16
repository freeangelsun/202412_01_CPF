package com.cpf.batch.runtime;

import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.web.api.CpfHeaders;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * BAT Runtime의 모든 RestClient 호출에서 직전 서비스/인스턴스 신원을 서버가 확정한 값으로
 * 덮어씁니다. 외부 요청이 넣은 caller header를 다음 hop으로 전달하지 않습니다.
 */
public final class BatRuntimeIdentityRestClientCustomizer
        implements RestClientCustomizer, Ordered {
    private final RuntimeRegistration registration;

    public BatRuntimeIdentityRestClientCustomizer(RuntimeRegistration registration) {
        this.registration = registration;
    }

    @Override
    public void customize(RestClient.Builder builder) {
        builder.requestInterceptor((request, body, execution) -> {
            applyIdentity(request.getHeaders());
            return execution.execute(request, body);
        });
    }

    @Override
    public int getOrder() {
        // CPF Core의 일반 전파 interceptor가 있더라도 BAT Runtime의 실제 등록 신원이 최종 값입니다.
        return Ordered.LOWEST_PRECEDENCE;
    }

    void applyIdentity(HttpHeaders headers) {
        String callerService = registration.moduleId() == null
                || registration.moduleId().isBlank()
                ? "BAT"
                : registration.moduleId().trim();
        String instanceId = registration.instanceId() == null ? "" : registration.instanceId().trim();
        headers.set(CpfHeaders.caller(), instanceId.isBlank() ? callerService : callerService + "@" + instanceId);
    }
}
