package com.cpf.backoffice.online.support.controller;

import com.cpf.web.api.CpfController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;

import com.cpf.backoffice.online.support.service.BackofficeSupportService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** MBW 업무 운영 편의 기능과 권한 분석 API입니다. */
@CpfController
@RequestMapping("/api/v1/backoffice")
@Tag(name = "MBW-Support", description = "MBW 대시보드, 알림, 첨부, 저장 검색, 다운로드 감사, 권한 분석 API")
public class BackofficeSupportController extends com.cpf.backoffice.online.base.BackofficeBaseController {
    private final BackofficeSupportService supportService;

    public BackofficeSupportController(BackofficeSupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/dashboard")    @Operation(operationId = "MBW_SUPPORT_DASHBOARD", summary = "업무 백오피스 대시보드",
            description = "사용자·직원·결재·알림·감사 핵심 건수를 인증 운영자 기준으로 반환합니다.")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_DASHBOARD", name = "업무 백오피스 대시보드", description = "업무 백오피스 대시보드 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String, Object>> dashboard(
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.dashboard(operatorId));
    }

    @GetMapping("/notifications")    @Operation(operationId = "MBW_SUPPORT_FIND_NOTIFICATIONS", summary = "내 알림 조회")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_FIND_NOTIFICATIONS", name = "내 알림 조회", description = "내 알림 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String, Object>>> notifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "100") int limit,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.findNotifications(operatorId, unreadOnly, limit));
    }

    @PostMapping("/notifications")    @Operation(operationId = "MBW_SUPPORT_CREATE_NOTIFICATION", summary = "업무 알림 등록",
            description = "수신 운영자와 업무 참조 정보를 등록하고 사유 기반 업무 감사를 남깁니다.")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_CREATE_NOTIFICATION", name = "업무 알림 등록", description = "업무 알림 등록 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String, Object>> createNotification(
            @RequestBody BackofficeSupportService.NotificationRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.createNotification(request, operatorId));
    }

    @PostMapping("/notifications/{notificationId}/read")    @Operation(operationId = "MBW_SUPPORT_READ_NOTIFICATION", summary = "업무 알림 읽음 처리")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_READ_NOTIFICATION", name = "업무 알림 읽음 처리", description = "업무 알림 읽음 처리 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String, Object>> readNotification(
            @PathVariable long notificationId,
            @RequestParam String reason,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.markNotificationRead(notificationId, reason, operatorId));
    }

    @PostMapping("/notifications/read-all")    @Operation(operationId = "MBW_SUPPORT_READ_ALL_NOTIFICATIONS", summary = "전체 알림 읽음 처리")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_READ_ALL_NOTIFICATIONS", name = "전체 알림 읽음 처리", description = "전체 알림 읽음 처리 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String,Object>> readAllNotifications(
            @RequestParam String reason,@RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.markAllNotificationsRead(reason,operatorId));
    }

    @GetMapping("/attachments")    @Operation(operationId = "MBW_SUPPORT_FIND_ATTACHMENTS", summary = "첨부파일 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_FIND_ATTACHMENTS", name = "첨부파일 목록 조회", description = "첨부파일 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String, Object>>> attachments(@RequestParam String groupId) {
        return ResponseEntity.ok(supportService.findAttachments(groupId));
    }

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)    @Operation(operationId = "MBW_SUPPORT_UPLOAD_ATTACHMENT", summary = "첨부파일 업로드",
            description = "CPF 첨부 저장 port의 경로·확장자·크기 검증과 SHA-256 계산 후 MBW 메타·감사를 기록합니다.")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_UPLOAD_ATTACHMENT", name = "첨부파일 업로드", description = "첨부파일 업로드 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @RequestParam String groupId,
            @RequestParam String reason,
            @RequestPart("file") MultipartFile file,
            @RequestAttribute("backoffice.operatorId") String operatorId) throws IOException {
        return ResponseEntity.ok(supportService.storeAttachment(
                groupId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                reason,
                operatorId));
    }

    @GetMapping("/attachments/{attachmentId}/download")    @Operation(operationId = "MBW_SUPPORT_DOWNLOAD_ATTACHMENT", summary = "첨부파일 다운로드",
            description = "사유·서버 권한·checksum·보안 검사 상태를 확인하고 다운로드 감사와 업무 감사를 기록합니다.")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_DOWNLOAD_ATTACHMENT", name = "첨부파일 다운로드", description = "첨부파일 다운로드 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable long attachmentId,
            @RequestParam String reason,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        BackofficeSupportService.AttachmentDownload download =
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

    @PostMapping("/attachments/{attachmentId}/security")    @Operation(operationId = "MBW_SUPPORT_UPDATE_ATTACHMENT_SECURITY", summary = "첨부 보안 상태 갱신")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_UPDATE_ATTACHMENT_SECURITY", name = "첨부 보안 상태 갱신", description = "첨부 보안 상태 갱신 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String,Object>> updateAttachmentSecurity(
            @PathVariable long attachmentId,@RequestBody BackofficeSupportService.AttachmentSecurityRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.updateAttachmentSecurity(attachmentId,request,operatorId));
    }

    @PostMapping("/attachments/{attachmentId}/recheck")    @Operation(operationId = "MBW_SUPPORT_RECHECK_ATTACHMENT", summary = "첨부 보안 재검사 요청")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_RECHECK_ATTACHMENT", name = "첨부 보안 재검사 요청", description = "첨부 보안 재검사 요청 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String,Object>> recheckAttachment(
            @PathVariable long attachmentId,@RequestParam String reason,@RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.requestAttachmentRecheck(attachmentId,reason,operatorId));
    }

    @GetMapping("/saved-searches")    @Operation(operationId = "MBW_SUPPORT_FIND_SAVED_SEARCHES", summary = "저장 검색 조회")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_FIND_SAVED_SEARCHES", name = "저장 검색 조회", description = "저장 검색 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String, Object>>> savedSearches(
            @RequestParam(required = false) String screenCode,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.findSavedSearches(screenCode, operatorId));
    }

    @PostMapping("/saved-searches")    @Operation(operationId = "MBW_SUPPORT_SAVE_SAVED_SEARCH", summary = "저장 검색 등록·수정",
            description = "검색 조건을 JSON object로 검증하고 소유자·화면·이름 기준으로 저장합니다.")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_SAVE_SAVED_SEARCH", name = "저장 검색 등록·수정", description = "저장 검색 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String, Object>> saveSavedSearch(
            @RequestBody BackofficeSupportService.SavedSearchRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.saveSavedSearch(request, operatorId));
    }

    @PostMapping("/saved-searches/{savedSearchId}/disable")    @Operation(operationId = "MBW_SUPPORT_DISABLE_SAVED_SEARCH", summary = "저장 검색 비활성화")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_DISABLE_SAVED_SEARCH", name = "저장 검색 비활성화", description = "저장 검색 비활성화 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String, Object>> disableSavedSearch(
            @PathVariable long savedSearchId,
            @RequestParam String reason,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.disableSavedSearch(savedSearchId, reason, operatorId));
    }

    @GetMapping("/download-audits")    @Operation(operationId = "MBW_SUPPORT_FIND_DOWNLOAD_AUDITS", summary = "다운로드 감사 조회")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_FIND_DOWNLOAD_AUDITS", name = "다운로드 감사 조회", description = "다운로드 감사 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String, Object>>> downloadAudits(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(supportService.findDownloadAudits(limit));
    }

    @GetMapping("/permissions/compare")    @Operation(operationId = "MBW_SUPPORT_COMPARE_ROLE_PERMISSIONS", summary = "역할 권한 비교",
            description = "두 역할의 화면·버튼·API·데이터 범위 규칙을 같은 권한 키로 정렬해 차이를 반환합니다.")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_COMPARE_ROLE_PERMISSIONS", name = "역할 권한 비교", description = "역할 권한 비교 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String, Object>>> compareRoles(
            @RequestParam String leftRoleCode,
            @RequestParam String rightRoleCode) {
        return ResponseEntity.ok(supportService.compareRoles(leftRoleCode, rightRoleCode));
    }

    @PostMapping("/permissions/simulate")    @Operation(operationId = "MBW_SUPPORT_SIMULATE_PERMISSION", summary = "권한 시뮬레이션",
            description = "역할·메뉴·행위·HTTP 경로·환경·업무 범위를 대입해 일치 규칙과 최종 허용 여부를 감사와 함께 반환합니다.")
    @CpfOnlineTransaction(operationId = "MBW_SUPPORT_SIMULATE_PERMISSION", name = "권한 시뮬레이션", description = "권한 시뮬레이션 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String, Object>> simulatePermission(
            @RequestBody BackofficeSupportService.PermissionSimulationRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(supportService.simulatePermission(request, operatorId));
    }
}
