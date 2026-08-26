package com.cpf.common.template;

import com.cpf.foundation.api.CpfBaseService;
import com.cpf.common.template.api.CpfTemplateService;

import java.util.Map;
import java.util.Objects;

/** Versioned template provider와 renderer를 결합하는 고객 업무공통 서비스입니다. */
public final class CmnTemplateService extends CpfBaseService implements CpfTemplateService {
    private final CmnTemplateProvider provider;
    private final CmnTemplateRenderer renderer;

    public CmnTemplateService(CmnTemplateProvider provider, CmnTemplateRenderer renderer) {
        this.provider = Objects.requireNonNull(provider);
        this.renderer = Objects.requireNonNullElseGet(renderer, CmnTemplateRenderer::new);
    }

    @Override
    public String render(String code, String channel, Map<String, ?> variables) {
        CmnTemplateDefinition definition = provider.findActive(code, channel)
                .filter(value -> value.active())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active template not found: " + code + "/" + channel));
        return renderer.render(definition, variables);
    }
}
