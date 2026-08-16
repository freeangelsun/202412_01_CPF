package com.cpf.education.scenarios.online.integrated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 온라인 A→B→C(/D) education flow의 실제 HTTP Channel adapter입니다.
 * {@code cpf-education-online-abcd} profile에서만 노출되어 제품 업무 API와 혼동되지 않습니다.
 */
@RestController
@Profile({"cpf-education-online-abcd","cpf-education-online-abcd-jdbc"})
@RequestMapping("/education/online/abcd")
public final class OnlineAbcdEducationHttpController {
    private final OnlineAbcdEducationFlow.Controller controller;
    private final ObjectProvider<OnlineAbcdSpringTransactionService> transactional;

    /**
     * HTTP adapter를 domain controller와 선택적 Spring transaction service에 연결합니다.
     * @param controller A→B→C domain controller
     * @param transactional JDBC profile에서만 존재하는 REQUIRED transaction service provider
     */
    public OnlineAbcdEducationHttpController(
            OnlineAbcdEducationFlow.Controller controller,
            ObjectProvider<OnlineAbcdSpringTransactionService> transactional) {
        this.controller = controller;
        this.transactional = transactional;
    }

    /**
     * 인가 ingress에서 확정된 transactionId를 education domain chain으로 전달합니다.
     * JDBC profile에서는 반드시 Spring REQUIRED transaction service를 통과합니다.
     * @param request transactionId/businessKey/payload/attempt 요청
     * @return A→B→C(/D) 실행 결과
     * @throws IllegalArgumentException 필수 identity가 비어 있거나 attempt가 1 미만인 경우
     */
    @PostMapping
    public OnlineAbcdEducationFlow.Result execute(@RequestBody OnlineAbcdEducationFlow.Request request) {
        OnlineAbcdSpringTransactionService tx = transactional.getIfAvailable();
        return tx == null ? controller.execute(request) : tx.execute(request);
    }
}
