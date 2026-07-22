package cpf.adm.opr.controller;

import cpf.adm.opr.dto.DownloadAuditLog;
import cpf.adm.opr.dto.DownloadPolicy;
import cpf.adm.opr.dto.DownloadRequest;
import cpf.adm.opr.dto.DownloadResult;
import cpf.adm.opr.service.AdmDownloadService;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ADM 공통 다운로드 API입니다.
 *
 * <p>운영 화면에서 파일을 내려받을 때 다운로드 사유, 마스킹 여부, 감사 로그를 표준화합니다.</p>
 */
@RestController
@RequestMapping("/adm/api/downloads")
@Tag(name = "ADM-Download", description = "ADM 공통 다운로드와 다운로드 감사 API")
public class AdmDownloadController extends cpf.adm.common.base.AdmBaseController {
    private final AdmDownloadService downloadService;

    public AdmDownloadController(AdmDownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping("/policies")
    @CpfOnlineTransaction(id = "OADMDW0001", name = "ADMDownloadPolicyList")
    @Operation(operationId = "admDownloadFindPolicies", summary = "다운로드 정책 조회", description = "ADM 화면에서 사용할 다운로드 유형과 마스킹 정책을 조회합니다.")
    public ResponseEntity<List<DownloadPolicy>> findPolicies() {
        return ResponseEntity.ok(downloadService.findPolicies());
    }

    @GetMapping("/audit-logs")
    @CpfOnlineTransaction(id = "OADMDW0002", name = "ADMDownloadAuditLogList")
    @Operation(operationId = "admDownloadFindDownloadAuditLogs", summary = "다운로드 감사 로그 조회", description = "운영자의 파일 다운로드 이력과 감사 사유를 조회합니다.")
    public ResponseEntity<List<DownloadAuditLog>> findDownloadAuditLogs(
            @RequestParam(required = false) String downloadType,
            @RequestParam(required = false) String adminId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(downloadService.findDownloadAuditLogs(downloadType, adminId, limit));
    }

    @PostMapping("/csv")
    @CpfOnlineTransaction(id = "OADMDW0003", name = "ADMDownloadCsv")
    @Operation(operationId = "admDownloadDownloadCsv", summary = "CSV 다운로드", description = "거래 로그, 오류 로그, 배치 이력, 알림 발송 이력을 CSV로 다운로드합니다.")
    public ResponseEntity<byte[]> downloadCsv(@RequestBody DownloadRequest request, HttpServletRequest servletRequest) {
        DownloadResult result = downloadService.downloadCsv(
                request,
                requestUser(servletRequest, request.requestUser()),
                servletRequest.getRemoteAddr(),
                servletRequest.getHeader(HttpHeaders.USER_AGENT));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(result.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-CPF-Download-Id", String.valueOf(result.downloadId()))
                .header("X-CPF-Download-Masked", result.maskedYn())
                .body(result.content());
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
