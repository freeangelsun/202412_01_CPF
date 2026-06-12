package fps.acc.tst.controller;

import fps.cmn.cde.dto.CommonCodeRequest;
import fps.cmn.cde.service.CodeCacheService;
import fps.cmn.cfg.dto.CommonConfigRequest;
import fps.cmn.cfg.service.ConfigCacheService;
import fps.cmn.msg.dto.CommonMessageRequest;
import fps.cmn.msg.service.MessageCacheService;
import fps.pfw.common.logging.FpsTransaction;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ACC에서 CMN 공통 관리 기능을 호출하는 샘플 컨트롤러입니다.
 *
 * <p>CMN은 코드, 메시지, 설정값 같은 개발 공통 데이터를 관리합니다.
 * 이 컨트롤러는 ACC 업무단에서 CMN 서비스를 직접 주입받아 조회/등록/수정/삭제/리프레시를 호출하는
 * 표준 사용 예시입니다.</p>
 *
 * <p>CRUD가 성공하면 각 CMN 서비스가 내부 캐시를 즉시 리프레시하므로,
 * 같은 WAS 안에서는 다음 조회부터 최신 DB 값이 반영됩니다.</p>
 */
@RestController
@Validated
@RequestMapping("/acc/cmn")
@Tag(name = "ACC-TST CMN 공통관리", description = "코드/메시지/설정값 CRUD와 캐시 리프레시 샘플 API")
public class CmnManagementController {
    private final CodeCacheService codeCacheService;
    private final MessageCacheService messageCacheService;
    private final ConfigCacheService configCacheService;

    public CmnManagementController(
            CodeCacheService codeCacheService,
            MessageCacheService messageCacheService,
            ConfigCacheService configCacheService) {
        // 공통 코드 조회/CRUD/캐시 리프레시 서비스입니다.
        this.codeCacheService = codeCacheService;
        // 공통 메시지 조회/CRUD/캐시 리프레시 서비스입니다.
        this.messageCacheService = messageCacheService;
        // 공통 설정값 조회/CRUD/캐시 리프레시 서비스입니다.
        this.configCacheService = configCacheService;
    }

    /**
     * 공통 코드를 조회합니다.
     *
     * @param codeId 코드 ID입니다. 있으면 단건 조회를 수행합니다.
     * @param codeKey 코드 키입니다. codeId가 없고 codeKey가 있으면 같은 코드 키 목록을 조회합니다.
     * @return 코드 단건, 코드 키 목록, 또는 전체 코드 목록입니다.
     */
    @GetMapping("/codes")
    @FpsTransaction(id = "ACC09TST0010", name = "공통코드조회샘플")
    public ResponseEntity<Map<String, Object>> getCodes(
            @RequestParam(name = "codeId", required = false) Long codeId,
            @RequestParam(name = "codeKey", required = false) String codeKey) {

        Object data;
        if (codeId != null) {
            // codeId가 있으면 PK 기준 단건 조회를 수행합니다.
            data = codeCacheService.getCodeById(codeId);
        } else if (hasText(codeKey)) {
            // codeKey가 있으면 같은 코드 키에 속한 코드 목록을 조회합니다.
            data = codeCacheService.getCodesByKey(codeKey);
        } else {
            // 조건이 없으면 전체 코드 목록을 조회합니다.
            data = codeCacheService.getAllCodes();
        }

        return ok("codes fetched", data);
    }

    /**
     * 공통 코드를 등록합니다.
     *
     * @param request 등록할 코드 정보입니다.
     * @return 등록된 코드 정보입니다.
     */
    @PostMapping("/codes")
    @FpsTransaction(id = "ACC09TST0011", name = "공통코드등록샘플")
    public ResponseEntity<Map<String, Object>> createCode(@Valid @RequestBody CommonCodeRequest request) {
        // 등록 후 CodeCacheService가 코드 캐시를 즉시 리프레시합니다.
        return ok("code created", codeCacheService.createCode(request));
    }

    /**
     * 공통 코드를 수정합니다.
     *
     * @param codeId 수정할 코드 ID입니다.
     * @param request 수정할 코드 정보입니다.
     * @return 수정된 코드 정보입니다.
     */
    @PutMapping("/codes")
    @FpsTransaction(id = "ACC09TST0012", name = "공통코드수정샘플")
    public ResponseEntity<Map<String, Object>> updateCode(
            @RequestParam("codeId") Long codeId,
            @Valid @RequestBody CommonCodeRequest request) {
        // 수정 후 CodeCacheService가 코드 캐시를 즉시 리프레시합니다.
        return ok("code updated", codeCacheService.updateCode(codeId, request));
    }

    /**
     * 공통 코드를 삭제합니다.
     *
     * @param codeId 삭제할 코드 ID입니다.
     * @return 삭제 후 최신 코드 목록입니다.
     */
    @DeleteMapping("/codes")
    @FpsTransaction(id = "ACC09TST0013", name = "공통코드삭제샘플")
    public ResponseEntity<Map<String, Object>> deleteCode(@RequestParam("codeId") Long codeId) {
        // 삭제 후 CodeCacheService가 코드 캐시를 즉시 리프레시합니다.
        return ok("code deleted", codeCacheService.deleteCode(codeId));
    }

    /**
     * 공통 코드 캐시를 즉시 리프레시합니다.
     *
     * @return 최신 코드 목록입니다.
     */
    @PostMapping("/codes/refresh")
    @FpsTransaction(id = "ACC09TST0014", name = "공통코드캐시리프레시샘플")
    public ResponseEntity<Map<String, Object>> refreshCodes() {
        // 운영자가 관리 화면에서 수동 리프레시 버튼을 누르는 상황을 가정한 샘플입니다.
        return ok("code cache refreshed", codeCacheService.refreshCodesAndPublish());
    }

    /**
     * 공통 메시지를 조회합니다.
     *
     * @param messageId 메시지 ID입니다. 있으면 단건 조회를 수행합니다.
     * @param messageKey 메시지 키입니다.
     * @param locale 언어 코드입니다. messageKey와 함께 있으면 키+언어 단건 조회를 수행합니다.
     * @param messageType 메시지 유형입니다. EXTERNAL 또는 INTERNAL입니다.
     * @return 메시지 단건 또는 전체 메시지 목록입니다.
     */
    @GetMapping("/messages")
    @FpsTransaction(id = "ACC09TST0020", name = "공통메시지조회샘플")
    public ResponseEntity<Map<String, Object>> getMessages(
            @RequestParam(name = "messageId", required = false) Long messageId,
            @RequestParam(name = "messageKey", required = false) String messageKey,
            @RequestParam(name = "locale", required = false) String locale,
            @RequestParam(name = "messageType", required = false) String messageType) {

        Object data;
        if (messageId != null) {
            data = messageCacheService.getMessageById(messageId);
        } else if (hasText(messageKey) && hasText(locale) && hasText(messageType)) {
            // 오류 메시지는 EXTERNAL/INTERNAL 유형을 나눠 조회할 수 있습니다.
            data = messageCacheService.getMessageByKeyLocaleType(messageKey, locale, messageType);
        } else if (hasText(messageKey) && hasText(locale)) {
            data = messageCacheService.getMessageByKeyAndLocale(messageKey, locale);
        } else if (hasText(messageKey)) {
            data = messageCacheService.getMessageByKey(messageKey);
        } else {
            data = messageCacheService.getAllMessages();
        }

        return ok("messages fetched", data);
    }

    /**
     * 공통 메시지를 등록합니다.
     *
     * @param request 등록할 메시지 정보입니다.
     * @return 등록된 메시지 정보입니다.
     */
    @PostMapping("/messages")
    @FpsTransaction(id = "ACC09TST0021", name = "공통메시지등록샘플")
    public ResponseEntity<Map<String, Object>> createMessage(@Valid @RequestBody CommonMessageRequest request) {
        return ok("message created", messageCacheService.createMessage(request));
    }

    /**
     * 공통 메시지를 수정합니다.
     *
     * @param messageId 수정할 메시지 ID입니다.
     * @param request 수정할 메시지 정보입니다.
     * @return 수정된 메시지 정보입니다.
     */
    @PutMapping("/messages")
    @FpsTransaction(id = "ACC09TST0022", name = "공통메시지수정샘플")
    public ResponseEntity<Map<String, Object>> updateMessage(
            @RequestParam("messageId") Long messageId,
            @Valid @RequestBody CommonMessageRequest request) {
        return ok("message updated", messageCacheService.updateMessage(messageId, request));
    }

    /**
     * 공통 메시지를 삭제합니다.
     *
     * @param messageId 삭제할 메시지 ID입니다.
     * @return 삭제 후 최신 메시지 목록입니다.
     */
    @DeleteMapping("/messages")
    @FpsTransaction(id = "ACC09TST0023", name = "공통메시지삭제샘플")
    public ResponseEntity<Map<String, Object>> deleteMessage(@RequestParam("messageId") Long messageId) {
        return ok("message deleted", messageCacheService.deleteMessage(messageId));
    }

    /**
     * 공통 메시지 캐시를 즉시 리프레시합니다.
     *
     * @return 최신 메시지 목록입니다.
     */
    @PostMapping("/messages/refresh")
    @FpsTransaction(id = "ACC09TST0024", name = "공통메시지캐시리프레시샘플")
    public ResponseEntity<Map<String, Object>> refreshMessages() {
        return ok("message cache refreshed", messageCacheService.refreshMessagesAndPublish());
    }

    /**
     * 공통 설정값을 조회합니다.
     *
     * @param configId 설정 ID입니다. 있으면 단건 조회를 수행합니다.
     * @param configKey 설정 키입니다. configId가 없고 configKey가 있으면 키 기준 단건 조회를 수행합니다.
     * @return 설정값 단건 또는 전체 설정값 목록입니다.
     */
    @GetMapping("/configs")
    @FpsTransaction(id = "ACC09TST0030", name = "공통설정조회샘플")
    public ResponseEntity<Map<String, Object>> getConfigs(
            @RequestParam(name = "configId", required = false) Long configId,
            @RequestParam(name = "configKey", required = false) String configKey) {

        Object data;
        if (configId != null) {
            data = configCacheService.getConfigById(configId);
        } else if (hasText(configKey)) {
            data = configCacheService.getConfigByKey(configKey);
        } else {
            data = configCacheService.getAllConfigs();
        }

        return ok("configs fetched", data);
    }

    /**
     * 공통 설정값을 등록합니다.
     *
     * @param request 등록할 설정값 정보입니다.
     * @return 등록된 설정값 정보입니다.
     */
    @PostMapping("/configs")
    @FpsTransaction(id = "ACC09TST0031", name = "공통설정등록샘플")
    public ResponseEntity<Map<String, Object>> createConfig(@Valid @RequestBody CommonConfigRequest request) {
        return ok("config created", configCacheService.createConfig(request));
    }

    /**
     * 공통 설정값을 수정합니다.
     *
     * @param configId 수정할 설정 ID입니다.
     * @param request 수정할 설정값 정보입니다.
     * @return 수정된 설정값 정보입니다.
     */
    @PutMapping("/configs")
    @FpsTransaction(id = "ACC09TST0032", name = "공통설정수정샘플")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @RequestParam("configId") Long configId,
            @Valid @RequestBody CommonConfigRequest request) {
        return ok("config updated", configCacheService.updateConfig(configId, request));
    }

    /**
     * 공통 설정값을 삭제합니다.
     *
     * @param configId 삭제할 설정 ID입니다.
     * @return 삭제 후 최신 설정값 목록입니다.
     */
    @DeleteMapping("/configs")
    @FpsTransaction(id = "ACC09TST0033", name = "공통설정삭제샘플")
    public ResponseEntity<Map<String, Object>> deleteConfig(@RequestParam("configId") Long configId) {
        return ok("config deleted", configCacheService.deleteConfig(configId));
    }

    /**
     * 공통 설정값 캐시를 즉시 리프레시합니다.
     *
     * @return 최신 설정값 목록입니다.
     */
    @PostMapping("/configs/refresh")
    @FpsTransaction(id = "ACC09TST0034", name = "공통설정캐시리프레시샘플")
    public ResponseEntity<Map<String, Object>> refreshConfigs() {
        return ok("config cache refreshed", configCacheService.refreshConfigsAndPublish());
    }

    private ResponseEntity<Map<String, Object>> ok(String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
