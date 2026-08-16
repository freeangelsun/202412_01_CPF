package com.cpf.education.integration.servicecall;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * EDU가 typed client로 서비스 경계를 호출하는 기본 교육 샘플입니다.
 *
 * <p>업무 개발자는 중립 요청 키만 전달합니다. 레지스트리, URI, timeout, retry, failover,
 * circuit과 segment 기록은 CPF 및 remote adapter가 처리합니다. 대상은 EDU 자체
 * 시뮬레이터이므로 특정 Generated Domain의 존재를 전제하지 않습니다.</p>
 */
@Component
public class EducationServiceCallEngineEducationSample {
    private final EducationServiceEchoClient echoClient;

    /**
     * 교육 샘플을 생성합니다.
     *
     * @param echoClient local/remote 공통 typed client
     */
    public EducationServiceCallEngineEducationSample(EducationServiceEchoClient echoClient) {
        this.echoClient = Objects.requireNonNull(echoClient, "echoClient는 필수입니다.");
    }

    /**
     * 제품 기본 정책으로 EDU 자체 중립 응답을 조회합니다.
     *
     * @param requestKey 요청 식별 키
     * @return typed 중립 응답
     */
    public EducationServiceEchoResponse callEcho(String requestKey) {
        return echoClient.execute(new EducationServiceEchoRequest(requestKey));
    }
}
