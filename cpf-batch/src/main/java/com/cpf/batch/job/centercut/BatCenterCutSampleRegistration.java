package com.cpf.batch.job.centercut;

import com.cpf.batch.runtime.centercut.BatCenterCutDefinition;
import com.cpf.batch.runtime.centercut.BatCenterCutRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/** BAT 기본 Center-Cut EDU definition을 제품 Runner에 등록합니다. */
@Component
public class BatCenterCutSampleRegistration {
    private final BatCenterCutRegistry registry;
    private final BatCenterCutSampleTargetProvider provider;
    private final BatCenterCutSampleHandler handler;

    public BatCenterCutSampleRegistration(
            BatCenterCutRegistry registry,
            BatCenterCutSampleTargetProvider provider,
            BatCenterCutSampleHandler handler) {
        this.registry = registry;
        this.provider = provider;
        this.handler = handler;
    }

    @PostConstruct
    void register() {
        registry.register(new BatCenterCutDefinition(
                "CPF_BAT_CENTER_CUT_JOB", provider, handler, 100, 10_000, 0.0d));
    }
}
