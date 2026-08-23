package com.cpf.admin.opr.controller;

import com.cpf.admin.common.base.AdmBaseController;
import com.cpf.core.api.version.CpfPlatformVersion;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.version.CpfPlatformVersionLoader;
import com.cpf.security.api.annotation.CpfPermission;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** ADM에서 Foundation version loader의 안전한 read-only 결과를 제공하는 운영 Consumer입니다. */
@RestController
@RequestMapping("/adm/api/platform")
@Tag(name = "ADM-Platform", description = "CPF Platform runtime metadata")
public final class AdmPlatformVersionController extends AdmBaseController {
    private final CpfPlatformVersionLoader versions;

    public AdmPlatformVersionController(CpfPlatformVersionLoader versions) {
        this.versions = versions;
    }

    @GetMapping("/version")
    @CpfPermission("hasAuthority('ADM_PLATFORM_READ')")
    @CpfTransactional(readOnly = true)
    @Operation(operationId = "admPlatformVersion", summary = "CPF Platform 버전 조회")
    public CpfPlatformVersion version() {
        return versions.load();
    }
}
