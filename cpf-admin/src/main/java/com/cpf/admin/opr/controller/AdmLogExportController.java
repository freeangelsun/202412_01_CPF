package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.AdmLogExportRequest;
import com.cpf.admin.opr.dto.AdmLogExportResponse;
import com.cpf.admin.opr.service.AdmLogExportService;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.cpf.web.api.CpfController;

import java.nio.charset.StandardCharsets;

/** 권한·사유·감사·만료 Artifact를 강제하는 로그 상세 Export API입니다. */
@CpfController
@RequestMapping("/adm/api/log-exports")
@Tag(name = "ADM-Log-Export", description = "감사되는 로그 상세 Export API")
public class AdmLogExportController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmLogExportService service;

    public AdmLogExportController(AdmLogExportService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('LOG_EXPORT') or hasAuthority('ADM_LOG_EXPORT')")
    @CpfOnlineTransaction(id = "OADMLG0101", name = "ADMLogExportCreate", ownerDomain="ADM")
    @Operation(operationId = "admLogExportCreate", summary = "로그 상세 Export 생성",
            description = "서버에서 민감정보를 마스킹하고 감사 로그와 15분 만료 Artifact를 생성합니다.")
    public ResponseEntity<AdmLogExportResponse> create(
            @RequestBody AdmLogExportRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(service.create(request, actor(servletRequest), servletRequest.getRemoteAddr()));
    }

    @GetMapping("/{exportId}/artifact")
    @PreAuthorize("hasAuthority('LOG_EXPORT') or hasAuthority('ADM_LOG_EXPORT')")
    @CpfOnlineTransaction(id = "OADMLG0102", name = "ADMLogExportDownload", ownerDomain="ADM")
    @Operation(operationId = "admLogExportDownload", summary = "만료 로그 Artifact 다운로드")
    public ResponseEntity<byte[]> download(
            @PathVariable String exportId,
            HttpServletRequest servletRequest) {
        AdmLogExportService.DownloadArtifact artifact =
                service.read(exportId, actor(servletRequest), servletRequest.getRemoteAddr());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(artifact.fileName(), StandardCharsets.UTF_8).build().toString())
                .body(artifact.content());
    }

    private String actor(HttpServletRequest request) {
        Object value = request.getAttribute("adm.operatorId");
        return value instanceof String actor && !actor.isBlank() ? actor : null;
    }
}
