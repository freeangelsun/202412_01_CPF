package com.cpf.starter.openapi.webmvc.internal;

import com.cpf.starter.openapi.webmvc.api.CpfOpenApiContributor;
import com.cpf.starter.openapi.webmvc.api.CpfOpenApiDocument;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;

final class CpfOpenApiCustomizerAdapter implements OpenApiCustomizer {
    private final CpfOpenApiWebMvcProperties properties;
    private final List<CpfOpenApiContributor> contributors;

    CpfOpenApiCustomizerAdapter(CpfOpenApiWebMvcProperties properties, List<CpfOpenApiContributor> contributors) {
        this.properties = properties;
        this.contributors = List.copyOf(contributors);
    }

    @Override
    public void customise(OpenAPI openApi) {
        CpfOpenApiDocument document = new CpfOpenApiDocument(
                properties.getTitle(), properties.getVersion(), properties.getDescription())
                .extension("x-cpf-framework", "Core Platform Framework")
                .extension("x-cpf-contract-source", "BACKEND_RUNTIME");
        for (CpfOpenApiContributor contributor : contributors) contributor.contribute(document);
        openApi.setInfo(new Info().title(document.title()).version(document.version()).description(document.description()));
        document.extensions().forEach(openApi::addExtension);
    }
}
