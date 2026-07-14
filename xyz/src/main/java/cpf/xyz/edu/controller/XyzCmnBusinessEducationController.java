package cpf.xyz.edu.controller;

import cpf.cmn.biz.log.CmnBusinessLogRequest;
import cpf.cmn.biz.log.CmnBusinessLogService;
import cpf.cmn.biz.notification.CmnNotificationLogRequest;
import cpf.cmn.biz.notification.CmnNotificationLogService;
import cpf.cmn.biz.sequence.CmnSequenceIssueRequest;
import cpf.cmn.biz.sequence.CmnSequenceService;
import cpf.cmn.utils.IdUtils;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CMN 업무 공통 DB 기능을 체험하는 교육 API입니다.
 *
 * <p>PFW 메타 데이터가 아니라 여러 업무 모듈이 함께 쓰는 공통 채번, 알림 로그,
 * 업무 로그 기능을 어떤 상황에서 호출하는지 보여줍니다.</p>
 */
@RestController
@RequestMapping("/xyz/edu/cmn-business")
@Tag(name = "XYZ-EDU 12. CMN 업무 공통", description = "채번, 알림 로그, 공통 업무 로그 교육 샘플")
public class XyzCmnBusinessEducationController {
    private final CmnSequenceService sequenceService;
    private final CmnNotificationLogService notificationLogService;
    private final CmnBusinessLogService businessLogService;

    public XyzCmnBusinessEducationController(CmnSequenceService sequenceService,
                                             CmnNotificationLogService notificationLogService,
                                             CmnBusinessLogService businessLogService) {
        this.sequenceService = sequenceService;
        this.notificationLogService = notificationLogService;
        this.businessLogService = businessLogService;
    }

    @PostMapping("/sequence/issue")
    @CpfTransaction(id = "XYZ12EDU0001", name = "XYZ CMN 공통 채번 발급")
    @Operation(operationId = "xyzCmnBusinessEducationIssueSequence", summary = "CMN 공통 채번 발급", description = "cmn_sequence를 row lock으로 잠그고 중복 없는 업무 번호를 발급합니다.")
    public ResponseEntity<Map<String, Object>> issueSequence(
            @RequestParam(required = false) String sequenceKey,
            @RequestParam(defaultValue = "CMN_EDU") String businessArea,
            @RequestParam(defaultValue = "ORDER") String businessKey,
            @RequestParam(defaultValue = "ORDER_NO") String sequenceKind,
            @RequestParam(defaultValue = "WEB") String channelCode,
            @RequestParam(defaultValue = "XYZ_EDU") String requestUser,
            @RequestParam(defaultValue = "WEB") String requestChannel) {

        if (!sequenceService.isEnabled()) {
            return ResponseEntity.ok(disabledResponse("CMN 업무 공통 DB가 비활성화되어 있습니다."));
        }

        CmnSequenceIssueRequest request = new CmnSequenceIssueRequest(
                TextUtils.hasText(sequenceKey) ? TextUtils.normalizeCode(sequenceKey) : null,
                TextUtils.normalizeCode(businessArea),
                TextUtils.normalizeCode(businessKey),
                TextUtils.normalizeCode(sequenceKind),
                TextUtils.normalizeCode(channelCode),
                TextUtils.normalizeCode(requestChannel),
                requestUser,
                "XYZ-CMN-BIZ-" + IdUtils.uuid32().substring(0, 12),
                "TRACE-" + IdUtils.uuid32().substring(0, 12));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", sequenceService.issue(request));
        response.put("guide", "주문번호, 신청번호, 문서번호처럼 여러 WAS에서 중복되면 안 되는 번호에 사용합니다. sequenceKey를 생략하면 업무영역/업무키/채번종류/채널 조합으로 기준을 찾습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notification-log")
    @CpfTransaction(id = "XYZ12EDU0002", name = "XYZ CMN 알림 로그 등록")
    @Operation(operationId = "xyzCmnBusinessEducationRegisterNotificationLog", summary = "CMN 알림 로그 등록", description = "메일, SMS, 푸시 같은 알림 요청 이력을 공통 포맷으로 저장합니다.")
    public ResponseEntity<Map<String, Object>> registerNotificationLog(
            @RequestParam(defaultValue = "EMAIL") String notificationType,
            @RequestParam(defaultValue = "developer@example.com") String receiver,
            @RequestParam(defaultValue = "CMN 알림 교육 샘플") String title) {

        if (!notificationLogService.isEnabled()) {
            return ResponseEntity.ok(disabledResponse("CMN 업무 공통 DB가 비활성화되어 있습니다."));
        }

        CmnNotificationLogRequest request = new CmnNotificationLogRequest(
                TextUtils.normalizeCode(notificationType),
                receiver,
                title,
                "CMN 공통 알림 로그 교육 샘플 메시지입니다.",
                "XYZ_EDU",
                "XYZ-CMN-NOTI-" + IdUtils.uuid32().substring(0, 12),
                "TRACE-" + IdUtils.uuid32().substring(0, 12));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", notificationLogService.register(request));
        response.put("guide", "실제 발송 어댑터를 붙이기 전에 알림 요청과 처리 상태를 같은 테이블에 남길 때 사용합니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/business-log")
    @CpfTransaction(id = "XYZ12EDU0003", name = "XYZ CMN 업무 로그 등록")
    @Operation(operationId = "xyzCmnBusinessEducationRegisterBusinessLog", summary = "CMN 공통 업무 로그 등록", description = "기술 거래 로그와 별도로 업무 의미가 있는 이벤트를 저장합니다.")
    public ResponseEntity<Map<String, Object>> registerBusinessLog(
            @RequestParam(defaultValue = "CMN_EDU") String businessArea,
            @RequestParam(defaultValue = "ORDER_SAMPLE") String businessKey,
            @RequestParam(defaultValue = "STATUS_CHANGE") String logType) {

        if (!businessLogService.isEnabled()) {
            return ResponseEntity.ok(disabledResponse("CMN 업무 공통 DB가 비활성화되어 있습니다."));
        }

        CmnBusinessLogRequest request = new CmnBusinessLogRequest(
                TextUtils.normalizeCode(businessArea),
                TextUtils.normalizeCode(businessKey),
                TextUtils.normalizeCode(logType),
                "CMN 공통 업무 로그 교육 샘플입니다.",
                "{\"status\":\"READY\"}",
                "XYZ_EDU",
                "XYZ-CMN-BLOG-" + IdUtils.uuid32().substring(0, 12),
                "TRACE-" + IdUtils.uuid32().substring(0, 12));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", businessLogService.register(request));
        response.put("guide", "거래 로그보다 업무 의미가 중요한 상태 변경, 연계 요청, 승인 이벤트에 사용합니다.");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> disabledResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("enabled", false);
        response.put("message", message);
        response.put("enableGuide", "cpf.cmn.business-db.enabled=true와 spring.datasource.cmn-business.* 설정을 확인하세요.");
        return response;
    }
}
