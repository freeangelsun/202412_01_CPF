package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmAuditLogService;
import cpf.cmn.msg.dto.CommonMessageRequest;
import cpf.cmn.msg.service.MessageCacheService;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/messages")
@Tag(name = "ADM-PFW Messages", description = "PFW 공통 메시지 관리 API")
public class AdmMessageController {
    private final MessageCacheService messageCacheService;
    private final AdmAuditLogService auditLogService;

    public AdmMessageController(MessageCacheService messageCacheService, AdmAuditLogService auditLogService) {
        this.messageCacheService = messageCacheService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADM-MSG-01-0010", name = "ADMMessageList")
    @Operation(operationId = "admMessageFindMessages", summary = "공통 메시지 목록 조회", description = "pfw_message 기준 메시지를 locale별로 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMessages() {
        return ResponseEntity.ok(messageCacheService.getAllMessages());
    }

    @GetMapping("/{messageId}")
    @CpfOnlineTransaction(id = "OADM-MSG-01-0011", name = "ADMMessageDetail")
    @Operation(operationId = "admMessageFindMessage", summary = "공통 메시지 상세 조회", description = "메시지 ID로 pfw_message 상세 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findMessage(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageCacheService.getMessageById(messageId));
    }

    @PostMapping
    @CpfOnlineTransaction(id = "OADM-MSG-02-0012", name = "ADMMessageCreate")
    @Operation(operationId = "admMessageCreateMessage", summary = "공통 메시지 등록", description = "pfw_message에 신규 메시지를 등록하고 메시지 캐시를 갱신합니다.")
    public ResponseEntity<Map<String, Object>> createMessage(
            @Valid @RequestBody CommonMessageRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.getReason());
        Map<String, Object> created = messageCacheService.createMessage(request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.getRequestUser()),
                "MESSAGE_CREATE",
                "pfw_message",
                String.valueOf(created.getOrDefault("messageId", request.getEffectiveMessageCode())),
                reason,
                null,
                String.valueOf(created),
                String.valueOf(created),
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{messageId}")
    @CpfOnlineTransaction(id = "OADM-MSG-03-0013", name = "ADMMessageUpdate")
    @Operation(operationId = "admMessageUpdateMessage", summary = "공통 메시지 수정", description = "pfw_message를 수정하고 메시지 캐시를 갱신합니다.")
    public ResponseEntity<Map<String, Object>> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody CommonMessageRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.getReason());
        Map<String, Object> before = messageCacheService.getMessageById(messageId);
        Map<String, Object> updated = messageCacheService.updateMessage(messageId, request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.getRequestUser()),
                "MESSAGE_UPDATE",
                "pfw_message",
                String.valueOf(messageId),
                reason,
                String.valueOf(before),
                String.valueOf(updated),
                "메시지 수정",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{messageId}")
    @CpfOnlineTransaction(id = "OADM-MSG-04-0014", name = "ADMMessageDisable")
    @Operation(operationId = "admMessageDeleteMessage", summary = "공통 메시지 비활성", description = "pfw_message를 비활성화하고 메시지 캐시를 갱신합니다.")
    public ResponseEntity<List<Map<String, Object>>> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "SYSTEM") String requestUser,
            HttpServletRequest servletRequest) {
        String requiredReason = auditLogService.requireReason(reason);
        Map<String, Object> before = messageCacheService.getMessageById(messageId);
        List<Map<String, Object>> latest = messageCacheService.deleteMessage(messageId);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, requestUser),
                "MESSAGE_DISABLE",
                "pfw_message",
                String.valueOf(messageId),
                requiredReason,
                String.valueOf(before),
                null,
                "메시지 비활성",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(latest);
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
