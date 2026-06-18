package cpf.acc.tst.controller;

import cpf.cmn.cde.dto.CommonCodeRequest;
import cpf.cmn.cde.service.CodeCacheService;
import cpf.cmn.cfg.dto.CommonConfigRequest;
import cpf.cmn.cfg.service.ConfigCacheService;
import cpf.cmn.msg.dto.CommonMessageRequest;
import cpf.cmn.msg.service.MessageCacheService;
import cpf.pfw.common.logging.CpfTransaction;
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
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@RestController
@Validated
@RequestMapping("/acc/cmn")
@Tag(name = "ACC-TST CMN Management", description = "Common code, message, config CRUD and cache refresh sample APIs")
public class CmnManagementController {
    private final CodeCacheService codeCacheService;
    private final MessageCacheService messageCacheService;
    private final ConfigCacheService configCacheService;

    public CmnManagementController(
            CodeCacheService codeCacheService,
            MessageCacheService messageCacheService,
            ConfigCacheService configCacheService) {
        // CPF 기능 설명입니다.
        this.codeCacheService = codeCacheService;
        // CPF 기능 설명입니다.
        this.messageCacheService = messageCacheService;
        // CPF 기능 설명입니다.
        this.configCacheService = configCacheService;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @GetMapping("/codes")
    @CpfTransaction(id = "ACC09TST0010", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> getCodes(
            @RequestParam(name = "codeId", required = false) Long codeId,
            @RequestParam(name = "codeKey", required = false) String codeKey) {

        Object data;
        if (codeId != null) {
            // CPF 기능 설명입니다.
            data = codeCacheService.getCodeById(codeId);
        } else if (hasText(codeKey)) {
            // CPF 기능 설명입니다.
            data = codeCacheService.getCodesByKey(codeKey);
        } else {
            // CPF 기능 설명입니다.
            data = codeCacheService.getAllCodes();
        }

        return ok("codes fetched", data);
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @PostMapping("/codes")
    @CpfTransaction(id = "ACC09TST0011", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> createCode(@Valid @RequestBody CommonCodeRequest request) {
        // CPF 기능 설명입니다.
        return ok("code created", codeCacheService.createCode(request));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @PutMapping("/codes")
    @CpfTransaction(id = "ACC09TST0012", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> updateCode(
            @RequestParam("codeId") Long codeId,
            @Valid @RequestBody CommonCodeRequest request) {
        // CPF 기능 설명입니다.
        return ok("code updated", codeCacheService.updateCode(codeId, request));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @DeleteMapping("/codes")
    @CpfTransaction(id = "ACC09TST0013", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> deleteCode(@RequestParam("codeId") Long codeId) {
        // CPF 기능 설명입니다.
        return ok("code deleted", codeCacheService.deleteCode(codeId));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @PostMapping("/codes/refresh")
    @CpfTransaction(id = "ACC09TST0014", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> refreshCodes() {
        // CPF 기능 설명입니다.
        return ok("code cache refreshed", codeCacheService.refreshCodesAndPublish());
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @GetMapping("/messages")
    @CpfTransaction(id = "ACC09TST0020", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> getMessages(
            @RequestParam(name = "messageId", required = false) Long messageId,
            @RequestParam(name = "messageKey", required = false) String messageKey,
            @RequestParam(name = "locale", required = false) String locale,
            @RequestParam(name = "messageType", required = false) String messageType) {

        Object data;
        if (messageId != null) {
            data = messageCacheService.getMessageById(messageId);
        } else if (hasText(messageKey) && hasText(locale) && hasText(messageType)) {
            // CPF 기능 설명입니다.
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
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @PostMapping("/messages")
    @CpfTransaction(id = "ACC09TST0021", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> createMessage(@Valid @RequestBody CommonMessageRequest request) {
        return ok("message created", messageCacheService.createMessage(request));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @PutMapping("/messages")
    @CpfTransaction(id = "ACC09TST0022", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> updateMessage(
            @RequestParam("messageId") Long messageId,
            @Valid @RequestBody CommonMessageRequest request) {
        return ok("message updated", messageCacheService.updateMessage(messageId, request));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @DeleteMapping("/messages")
    @CpfTransaction(id = "ACC09TST0023", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> deleteMessage(@RequestParam("messageId") Long messageId) {
        return ok("message deleted", messageCacheService.deleteMessage(messageId));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @PostMapping("/messages/refresh")
    @CpfTransaction(id = "ACC09TST0024", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> refreshMessages() {
        return ok("message cache refreshed", messageCacheService.refreshMessagesAndPublish());
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @GetMapping("/configs")
    @CpfTransaction(id = "ACC09TST0030", name = "CPF 처리 기준입니다.")
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
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @PostMapping("/configs")
    @CpfTransaction(id = "ACC09TST0031", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> createConfig(@Valid @RequestBody CommonConfigRequest request) {
        return ok("config created", configCacheService.createConfig(request));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @PutMapping("/configs")
    @CpfTransaction(id = "ACC09TST0032", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @RequestParam("configId") Long configId,
            @Valid @RequestBody CommonConfigRequest request) {
        return ok("config updated", configCacheService.updateConfig(configId, request));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @DeleteMapping("/configs")
    @CpfTransaction(id = "ACC09TST0033", name = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> deleteConfig(@RequestParam("configId") Long configId) {
        return ok("config deleted", configCacheService.deleteConfig(configId));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @PostMapping("/configs/refresh")
    @CpfTransaction(id = "ACC09TST0034", name = "CPF 처리 기준입니다.")
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

