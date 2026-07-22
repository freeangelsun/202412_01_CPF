package com.cpf.bizadmin.support.controller;

import com.cpf.bizadmin.support.service.BzaSupportService;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** BZA 업무 운영 편의 기능과 권한 분석 API입니다. */
@RestController
@RequestMapping("/api/bza")
@Tag(name = "BZA-Support", description = "BZA 대시보드, 알림, 첨부, 저장 검색, 다운로드 감사, 권한 분석 API")
public class BzaSupportController extends com.cpf.bizadmin.common.base.BzaBaseController {
    private final BzaSupportService supportService;

    public BzaSupportController(BzaSupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/dashboard")
    @CpfOnlineTransaction(id = "OBZADS0001", name = "BzaDashboard")
    @Operation(operationId = "bzaSupportDashboard", summary = "업무 백오피스 대시보드",
            description = "사용자·직원·결재·알림·감사 핵심 건수를 인증 운영자 기준으로 반환합니다.")
    public ResponseEntity<Map<String, Object>> dashboard(
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.dashboard(operatorId));
    }

    @GetMapping("/notifications")
    @CpfOnlineTransaction(id = "OBZANT0001", name = "BzaNotificationList")
    @Operation(operationId = "bzaSupportFindNotifications", summary = "내 알림 조회")
    public ResponseEntity<List<Map<String, Object>>> notifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "100") int limit,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.findNotifications(operatorId, unreadOnly, limit));
    }

    @PostMapping("/notifications")
    @CpfOnlineTransaction(id = "OBZANT0002", name = "BzaNotificationCreate")
    @Operation(operationId = "bzaSupportCreateNotification", summary = "업무 알림 등록",
            description = "수신 운영자와 업무 참조 정보를 등록하고 사유 기반 업무 감사를 남깁니다.")
    public ResponseEntity<Map<String, Object>> createNotification(
            @RequestBody BzaSupportService.NotificationRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.createNotification(request, operatorId));
    }

    @PostMapping("/notifications/{notificationId}/read")
    @CpfOnlineTransaction(id = "OBZANT0003", name = "BzaNotificationRead")
    @Operation(operationId = "bzaSupportReadNotification", summary = "업무 알림 읽음 처리")
    public ResponseEntity<Map<String, Object>> readNotification(
            @PathVariable long notificationId,
            @RequestParam String reason,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.markNotificationRead(notificationId, reason, operatorId));
    }

    @GetMapping("/attachments")
    @CpfOnlineTransaction(id = "OBZAAT0001", name = "BzaAttachmentList")
    @Operation(operationId = "bzaSupportFindAttachments", summary = "첨부파일 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> attachments(@RequestParam String groupId) {
        return ResponseEntity.ok(supportService.findAttachments(groupId));
    }

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CpfOnlineTransaction(id = "OBZAAT0002", name = "BzaAttachmentUpload")
    @Operation(operationId = "bzaSupportUploadAttachment", summary = "첨부파일 업로드",
            description = "CPF 첨부 저장 port의 경로·확장자·크기 검증과 SHA-256 계산 후 BZA 메타·감사를 기록합니다.")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @RequestParam String groupId,
            @RequestParam String reason,
            @RequestPart("file") MultipartFile file,
            @RequestAttribute("bza.operatorId") String operatorId) throws IOException {
        return ResponseEntity.ok(supportService.storeAttachment(
                groupId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                reason,
                operatorId));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @CpfOnlineTransaction(id = "OBZAAT0003", name = "BzaAttachmentDownload")
    @Operation(operationId = "bzaSupportDownloadAttachment", summary = "첨부파일 다운로드",
            description = "사유·서버 권한·checksum·보안 검사 상태를 확인하고 다운로드 감사와 업무 감사를 기록합니다.")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable long attachmentId,
            @RequestParam String reason,
            @RequestAttribute("bza.operatorId") String operatorId) {
        BzaSupportService.AttachmentDownload download =
                supportService.downloadAttachment(attachmentId, reason, operatorId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(download.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Checksum-Sha256", download.checksumSha256())
                .body(download.content());
    }

    @GetMapping("/saved-searches")
    @CpfOnlineTransaction(id = "OBZASC0001", name = "BzaSavedSearchList")
    @Operation(operationId = "bzaSupportFindSavedSearches", summary = "저장 검색 조회")
    public ResponseEntity<List<Map<String, Object>>> savedSearches(
            @RequestParam(required = false) String screenCode,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.findSavedSearches(screenCode, operatorId));
    }

    @PostMapping("/saved-searches")
    @CpfOnlineTransaction(id = "OBZASC0002", name = "BzaSavedSearchSave")
    @Operation(operationId = "bzaSupportSaveSavedSearch", summary = "저장 검색 등록·수정",
            description = "검색 조건을 JSON object로 검증하고 소유자·화면·이름 기준으로 저장합니다.")
    public ResponseEntity<Map<String, Object>> saveSavedSearch(
            @RequestBody BzaSupportService.SavedSearchRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.saveSavedSearch(request, operatorId));
    }

    @PostMapping("/saved-searches/{savedSearchId}/disable")
    @CpfOnlineTransaction(id = "OBZASC0003", name = "BzaSavedSearchDisable")
    @Operation(operationId = "bzaSupportDisableSavedSearch", summary = "저장 검색 비활성화")
    public ResponseEntity<Map<String, Object>> disableSavedSearch(
            @PathVariable long savedSearchId,
            @RequestParam String reason,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.disableSavedSearch(savedSearchId, reason, operatorId));
    }

    @GetMapping("/download-audits")
    @CpfOnlineTransaction(id = "OBZADW0002", name = "BzaDownloadAuditList")
    @Operation(operationId = "bzaSupportFindDownloadAudits", summary = "다운로드 감사 조회")
    public ResponseEntity<List<Map<String, Object>>> downloadAudits(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(supportService.findDownloadAudits(limit));
    }

    @GetMapping("/permissions/compare")
    @CpfOnlineTransaction(id = "OBZAPE0003", name = "BzaRolePermissionCompare")
    @Operation(operationId = "bzaSupportCompareRolePermissions", summary = "역할 권한 비교",
            description = "두 역할의 화면·버튼·API·데이터 범위 규칙을 같은 권한 키로 정렬해 차이를 반환합니다.")
    public ResponseEntity<List<Map<String, Object>>> compareRoles(
            @RequestParam String leftRoleCode,
            @RequestParam String rightRoleCode) {
        return ResponseEntity.ok(supportService.compareRoles(leftRoleCode, rightRoleCode));
    }

    @PostMapping("/permissions/simulate")
    @CpfOnlineTransaction(id = "OBZAPE0004", name = "BzaPermissionSimulation")
    @Operation(operationId = "bzaSupportSimulatePermission", summary = "권한 시뮬레이션",
            description = "역할·메뉴·행위·HTTP 경로·환경·업무 범위를 대입해 일치 규칙과 최종 허용 여부를 감사와 함께 반환합니다.")
    public ResponseEntity<Map<String, Object>> simulatePermission(
            @RequestBody BzaSupportService.PermissionSimulationRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.simulatePermission(request, operatorId));
    }
}
