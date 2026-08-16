package com.cpf.admin.opr.controller;

import com.cpf.core.api.version.CpfPlatformVersion;
import com.cpf.data.persistence.api.annotation.CpfTx;
import com.cpf.foundation.version.CpfPlatformVersionLoader;
import com.cpf.security.api.annotation.CpfPermission;
import com.cpf.web.api.CpfController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** ADM에서 Foundation version loader의 안전한 read-only 결과를 제공하는 운영 Consumer입니다. */
@CpfController
@RequestMapping("/adm/api/platform")
@Tag(name = "ADM-Platform", description = "CPF Platform runtime metadata")
public final class AdmPlatformVersionController extends AdmBaseController {
    private final CpfPlatformVersionLoader versions;

    public AdmPlatformVersionController(CpfPlatformVersionLoader versions) {
        this.versions = versions;
    }

    @GetMapping("/version")
    @CpfPermission("ADM_PLATFORM_READ")
    @CpfTx(id = "OADMPLAT001", name = "ADMPlatformVersion", ownerDomain = "ADM", readOnly = true)
    @Operation(operationId = "admPlatformVersion", summary = "CPF Platform 버전 조회")
    public CpfPlatformVersion version() {
        return versions.load();
    }
}
