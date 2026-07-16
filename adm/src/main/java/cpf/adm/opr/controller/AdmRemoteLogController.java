package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmAuditLogService;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.remotelog.CpfRemoteLogArtifact;
import cpf.pfw.common.remotelog.CpfRemoteLogBundle;
import cpf.pfw.common.remotelog.CpfRemoteLogArtifactPort;
import cpf.pfw.common.remotelog.CpfRemoteLogArtifactSearch;
import cpf.pfw.common.remotelog.CpfRemoteLogBundleJob;
import cpf.pfw.common.remotelog.CpfRemoteLogBundleJobPort;
import cpf.pfw.common.remotelog.CpfRemoteLogDownloadGrant;
import cpf.pfw.common.remotelog.CpfRemoteLogPreview;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** ADM에서 PFW 로그 아티팩트 port를 통해 안전한 로그 조회와 다운로드를 제공합니다. */
@RestController
@RequestMapping("/adm/api/remote-logs")
@Tag(name = "ADM-OPR Remote Log", description = "실행 인스턴스 로그 아티팩트 조회, 미리보기, 다운로드")
public class AdmRemoteLogController {

    private final CpfRemoteLogArtifactPort remoteLogArtifactPort;
    private final CpfRemoteLogBundleJobPort remoteLogBundleJobPort;
    private final AdmAuditLogService auditLogService;

    public AdmRemoteLogController(
            CpfRemoteLogArtifactPort remoteLogArtifactPort,
            CpfRemoteLogBundleJobPort remoteLogBundleJobPort,
            AdmAuditLogService auditLogService) {
        this.remoteLogArtifactPort = remoteLogArtifactPort;
        this.remoteLogBundleJobPort = remoteLogBundleJobPort;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADMRL0006", name = "ADM원격로그목록조회")
    @Operation(operationId = "admRemoteLogSearch", summary = "로그 아티팩트 목록 조회",
            description = "절대경로를 노출하지 않고 허용된 로그 root 아래의 파일 메타데이터만 조회합니다.")
    public ResponseEntity<List<CpfRemoteLogArtifact>> search(
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String instance,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String standardTransactionId,
            @RequestParam(required = false) String standardBatchId,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String segmentId,
            @RequestParam(required = false) String jobInstanceId,
            @RequestParam(required = false) String jobExecutionId,
            @RequestParam(required = false) String stepExecutionId,
            @RequestParam(required = false) String schedulerId,
            @RequestParam(required = false) Instant modifiedFrom,
            @RequestParam(required = false) Instant modifiedTo,
            @RequestParam(required = false) Long minSize,
            @RequestParam(required = false) Long maxSize,
            @RequestParam(required = false) Boolean compressed,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(remoteLogArtifactPort.search(new CpfRemoteLogArtifactSearch(
                environment, module, service, instance, logType, fileName,
                standardTransactionId, standardBatchId, transactionGlobalId, transactionId, segmentId,
                jobInstanceId, jobExecutionId, stepExecutionId, schedulerId,
                modifiedFrom, modifiedTo, minSize, maxSize, compressed, active, limit)));
    }

    @GetMapping("/{artifactId}/preview")
    @CpfOnlineTransaction(id = "OADMRL0007", name = "ADM원격로그미리보기")
    @Operation(operationId = "admRemoteLogPreview", summary = "로그 아티팩트 미리보기",
            description = "마스킹을 다시 적용한 마지막 N개 로그 행과 검색 결과를 반환합니다.")
    public ResponseEntity<CpfRemoteLogPreview> preview(
            @PathVariable String artifactId,
            @RequestParam(defaultValue = "200") int lastLines,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(remoteLogArtifactPort.preview(artifactId, lastLines, keyword));
    }

    @GetMapping("/{artifactId}/download")
    @CpfOnlineTransaction(id = "OADMRL0002", name = "ADM원격로그다운로드")
    @Operation(operationId = "admRemoteLogDownload", summary = "로그 아티팩트 다운로드",
            description = "권한이 확인된 단일 로그 아티팩트를 안전한 파일명으로 다운로드합니다.")
    public ResponseEntity<FileSystemResource> download(
            @PathVariable String artifactId,
            @RequestParam String reason,
            HttpServletRequest request) throws java.io.IOException {
        String operatorId = String.valueOf(request.getAttribute("adm.operatorId"));
        String requiredReason = auditLogService.requireReason(reason);
        Path path = remoteLogArtifactPort.resolveDownload(artifactId);
        auditLogService.record(
                TransactionContext.currentTransactionId(), operatorId, "REMOTE_LOG_DOWNLOAD",
                "LOG_ARTIFACT", artifactId, requiredReason, request.getRemoteAddr());
        FileSystemResource resource = new FileSystemResource(path);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(path.getFileName().toString(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/bundles")
    @CpfOnlineTransaction(id = "OADMRL0003", name = "ADM원격로그묶음다운로드")
    @Operation(operationId = "admRemoteLogBundleDownload", summary = "선택 로그 ZIP 다운로드",
            description = "여러 인스턴스의 선택 로그를 checksum manifest가 포함된 ZIP으로 만들고 부분 실패 건수를 헤더로 반환합니다.")
    public ResponseEntity<FileSystemResource> bundle(
            @RequestBody BundleRequest bundleRequest,
            HttpServletRequest request) throws java.io.IOException {
        String operatorId = String.valueOf(request.getAttribute("adm.operatorId"));
        String requiredReason = auditLogService.requireReason(bundleRequest.reason());
        CpfRemoteLogBundle bundle = remoteLogArtifactPort.createBundle(bundleRequest.artifactIds());
        auditLogService.record(
                TransactionContext.currentTransactionId(), operatorId, "REMOTE_LOG_BUNDLE_DOWNLOAD",
                "LOG_BUNDLE", bundle.bundleId(), requiredReason, request.getRemoteAddr());
        FileSystemResource resource = new FileSystemResource(bundle.path());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(bundle.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-CPF-Partial-Failure-Count", String.valueOf(bundle.failedArtifactIds().size()))
                .header("X-CPF-Bundle-Expires-At", bundle.expiresAt().toString())
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(resource);
    }

    @PostMapping("/bundle-jobs")
    @CpfOnlineTransaction(id = "OADMRL0001", name = "ADM원격로그비동기묶음요청")
    @Operation(operationId = "admRemoteLogBundleJobCreate", summary = "비동기 로그 ZIP 작업 등록",
            description = "선택 로그 ZIP 생성을 queue에 등록하고 소유 운영자에게만 노출되는 작업 ID를 반환합니다.")
    public ResponseEntity<CpfRemoteLogBundleJob> createBundleJob(
            @RequestBody BundleRequest bundleRequest,
            HttpServletRequest request) {
        String operatorId = operatorId(request);
        String requiredReason = auditLogService.requireReason(bundleRequest.reason());
        CpfRemoteLogBundleJob job = remoteLogBundleJobPort.submit(operatorId, bundleRequest.artifactIds());
        auditLogService.record(
                TransactionContext.currentTransactionId(), operatorId, "REMOTE_LOG_BUNDLE_CREATE",
                "LOG_BUNDLE_JOB", job.jobId(), requiredReason, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    @GetMapping("/bundle-jobs/{jobId}")
    @CpfOnlineTransaction(id = "OADMRL0009", name = "ADM원격로그비동기묶음상태조회")
    @Operation(operationId = "admRemoteLogBundleJobFind", summary = "비동기 로그 ZIP 작업 상태 조회",
            description = "현재 운영자가 등록한 작업의 queue, 실행, 완료, 실패 상태와 부분 실패 정보를 조회합니다.")
    public ResponseEntity<CpfRemoteLogBundleJob> findBundleJob(
            @PathVariable String jobId,
            HttpServletRequest request) {
        return ResponseEntity.ok(remoteLogBundleJobPort.find(jobId, operatorId(request)));
    }

    @PostMapping("/bundle-jobs/{jobId}/download-tokens")
    @CpfOnlineTransaction(id = "OADMRL0005", name = "ADM원격로그다운로드토큰발급")
    @Operation(operationId = "admRemoteLogBundleDownloadTokenIssue", summary = "1회성 다운로드 token 발급",
            description = "완료된 비동기 ZIP 작업에 대해 짧은 유효기간의 1회성 token을 발급합니다. 재다운로드는 새 token을 발급해야 합니다.")
    public ResponseEntity<CpfRemoteLogDownloadGrant> issueDownloadToken(
            @PathVariable String jobId,
            @RequestBody ReasonRequest reasonRequest,
            HttpServletRequest request) {
        String operatorId = operatorId(request);
        String requiredReason = auditLogService.requireReason(reasonRequest.reason());
        CpfRemoteLogDownloadGrant grant = remoteLogBundleJobPort.issueDownloadGrant(jobId, operatorId);
        auditLogService.record(
                TransactionContext.currentTransactionId(), operatorId, "REMOTE_LOG_DOWNLOAD_TOKEN_ISSUE",
                "LOG_BUNDLE_JOB", jobId, requiredReason, request.getRemoteAddr());
        return ResponseEntity.ok(grant);
    }

    @GetMapping("/bundle-jobs/{jobId}/download")
    @CpfOnlineTransaction(id = "OADMRL0004", name = "ADM원격로그비동기묶음다운로드")
    @Operation(operationId = "admRemoteLogBundleJobDownload", summary = "비동기 로그 ZIP 다운로드",
            description = "현재 운영자에게 발급된 유효한 1회성 token을 소비해 ZIP 파일을 다운로드합니다.")
    public ResponseEntity<FileSystemResource> downloadBundleJob(
            @PathVariable String jobId,
            @RequestParam String token,
            @RequestParam String reason,
            HttpServletRequest request) throws java.io.IOException {
        String operatorId = operatorId(request);
        String requiredReason = auditLogService.requireReason(reason);
        CpfRemoteLogBundle bundle = remoteLogBundleJobPort.resolveDownload(jobId, operatorId, token);
        auditLogService.record(
                TransactionContext.currentTransactionId(), operatorId, "REMOTE_LOG_BUNDLE_DOWNLOAD",
                "LOG_BUNDLE_JOB", jobId, requiredReason, request.getRemoteAddr());
        FileSystemResource resource = new FileSystemResource(bundle.path());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(bundle.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-CPF-Partial-Failure-Count", String.valueOf(bundle.failedArtifactIds().size()))
                .header("X-CPF-Bundle-Expires-At", bundle.expiresAt().toString())
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(resource);
    }

    @GetMapping("/diagnostics")
    @CpfOnlineTransaction(id = "OADMRL0008", name = "ADM원격로그진단조회")
    @Operation(operationId = "admRemoteLogDiagnostics", summary = "원격 로그 adapter 진단",
            description = "adapter 종류, timeout과 마지막 instance별 부분 실패를 확인합니다. service token 원문은 노출하지 않습니다.")
    public ResponseEntity<Map<String, Object>> diagnostics() {
        Map<String, Object> diagnostics = new LinkedHashMap<>();
        diagnostics.put("artifact", remoteLogArtifactPort.diagnostics());
        diagnostics.put("bundleJob", remoteLogBundleJobPort.diagnostics());
        return ResponseEntity.ok(Map.copyOf(diagnostics));
    }

    private String operatorId(HttpServletRequest request) {
        Object value = request.getAttribute("adm.operatorId");
        if (value == null || value.toString().isBlank()) {
            throw new IllegalStateException("인증된 ADM 운영자 정보가 없습니다.");
        }
        return value.toString();
    }

    public record BundleRequest(List<String> artifactIds, String reason) {
    }

    public record ReasonRequest(String reason) {
    }
}
