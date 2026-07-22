package com.cpf.reference.cmn.controller;

import com.cpf.common.cde.dto.CommonCodeRequest;
import com.cpf.common.cde.service.CodeCacheService;
import com.cpf.common.cfg.dto.CommonConfigRequest;
import com.cpf.common.cfg.service.ConfigCacheService;
import com.cpf.common.msg.dto.CommonMessageRequest;
import com.cpf.common.msg.service.MessageCacheService;
import com.cpf.common.msg.service.ResponseCodeCacheService;
import com.cpf.common.utils.IdUtils;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 02. CMN Cache", description = "Common code, message, response code, and config cache samples")
public class ReferenceCmnEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final CodeCacheService codeCacheService;
    private final MessageCacheService messageCacheService;
    private final ResponseCodeCacheService responseCodeCacheService;
    private final ConfigCacheService configCacheService;

    public ReferenceCmnEducationController(
            CodeCacheService codeCacheService,
            MessageCacheService messageCacheService,
            ResponseCodeCacheService responseCodeCacheService,
            ConfigCacheService configCacheService) {
        this.codeCacheService = codeCacheService;
        this.messageCacheService = messageCacheService;
        this.responseCodeCacheService = responseCodeCacheService;
        this.configCacheService = configCacheService;
    }

    @GetMapping("/cache")
    @CpfOnlineTransaction(id = "OREFAA0015", name = "REFCmnCacheLookup")
    @Operation(operationId = "refCmnEducationGetCacheSamples", summary = "CMN cache lookup", description = "Looks up code, message, response code, and config cache entries.")
    public ResponseEntity<Map<String, Object>> getCacheSamples(
            @RequestParam(defaultValue = "USER_STATUS") String codeKey,
            @RequestParam(defaultValue = "MCMN000001") String messageKey,
            @RequestParam(defaultValue = "ECPF010004") String responseCode,
            @RequestParam(defaultValue = "cpf.LOGIN.MAX_FAIL_COUNT") String configKey) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", codeCacheService.getCodesByKey(codeKey));
        response.put("message", messageCacheService.getMessageByKeyAndLocale(messageKey, "ko"));
        response.put("responseCode", responseCodeCacheService.getResponseCode(TextUtils.normalizeCode(responseCode)));
        response.put("config", configCacheService.getConfigByKey(configKey));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/response-code")
    @CpfOnlineTransaction(id = "OREFAA0026", name = "REFResponseCodeCacheSample")
    @Operation(operationId = "refCmnEducationGetResponseCodeCacheSample", summary = "Response code cache sample", description = "Shows response_code and linked message_code resolution data.")
    public ResponseEntity<Map<String, Object>> getResponseCodeCacheSample(
            @RequestParam(defaultValue = "EREF010001") String responseCode,
            @RequestParam(defaultValue = "ko") String locale) {

        Map<String, Object> code = responseCodeCacheService.getResponseCode(TextUtils.normalizeCode(responseCode));
        String messageCode = value(code, "message_code");
        Map<String, Object> message = messageCacheService.getMessageByKeyAndLocale(messageCode, locale);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("responseCode", code);
        response.put("message", message);
        response.put("usage", "throw new CpfBusinessException(\"" + TextUtils.normalizeCode(responseCode) + "\", detail, args)");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/message-format")
    @CpfOnlineTransaction(id = "OREFAA0027", name = "REFMessageFormatSample")
    @Operation(operationId = "refCmnEducationGetMessageFormatSample", summary = "Message format sample", description = "Shows fixed and indexed message rows. Indexed messages use {0}, {1}, ... placeholders.")
    public ResponseEntity<Map<String, Object>> getMessageFormatSample(
            @RequestParam(defaultValue = "MREF090001") String indexedMessageCode,
            @RequestParam(defaultValue = "MCMN000001") String fixedMessageCode,
            @RequestParam(defaultValue = "ko") String locale) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("indexed", messageCacheService.getMessageByKeyAndLocale(TextUtils.normalizeCode(indexedMessageCode), locale));
        response.put("fixed", messageCacheService.getMessageByKeyAndLocale(TextUtils.normalizeCode(fixedMessageCode), locale));
        response.put("indexedArguments", Map.of("0", "memberNo", "1", "M0001"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/refresh")
    @CpfOnlineTransaction(id = "OREFAA0016", name = "REFCmnCacheRefresh")
    @Operation(operationId = "refCmnEducationRefreshCaches", summary = "CMN cache refresh", description = "Refreshes CMN caches and publishes refresh events.")
    public ResponseEntity<Map<String, Object>> refreshCaches() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("codes", codeCacheService.refreshCodesAndPublish());
        response.put("messages", messageCacheService.refreshMessagesAndPublish());
        response.put("responseCodes", responseCodeCacheService.refreshResponseCodesAndPublish());
        response.put("configs", configCacheService.refreshConfigsAndPublish());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cmn/code")
    @CpfOnlineTransaction(id = "OREFAA0010", name = "REFCmnCodeCreate")
    @Operation(operationId = "refCmnEducationCreateCommonCode", summary = "CMN code create sample", description = "Creates a sample common code row through CMN.")
    public ResponseEntity<Map<String, Object>> createCommonCode(
            @RequestParam(required = false) String codeKey,
            @RequestParam(defaultValue = "READY") String codeValue) {

        CommonCodeRequest request = new CommonCodeRequest();
        request.setCodeKey(TextUtils.hasText(codeKey)
                ? TextUtils.normalizeCode(codeKey)
                : "REF_SAMPLE_" + IdUtils.uuid32().substring(0, 8).toUpperCase());
        request.setCodeValue(TextUtils.normalizeCode(codeValue));
        request.setDescription("REF education common code sample");
        request.setUseYn("Y");
        request.setRequestUser("REF_EDU");
        return ResponseEntity.ok(codeCacheService.createCode(request));
    }

    @PostMapping("/cmn/message")
    @CpfOnlineTransaction(id = "OREFAA0020", name = "REFCmnMessageCreate")
    @Operation(operationId = "refCmnEducationCreateCommonMessage", summary = "CMN message create sample", description = "Creates a sample message row with external/internal templates.")
    public ResponseEntity<Map<String, Object>> createCommonMessage() {
        CommonMessageRequest request = new CommonMessageRequest();
        request.setMessageCode("MREF0900" + IdUtils.uuid32().substring(0, 2).toUpperCase());
        request.setLocale("ko");
        request.setMessageFormatType("INDEXED");
        request.setExternalMessage("REF education message: {0}");
        request.setInternalMessage("REF education internal message sampleName={0}");
        request.setParameterCount(1);
        request.setParameterSample("[\"sample\"]");
        request.setDescription("REF education common message sample");
        request.setUseYn("Y");
        request.setRequestUser("REF_EDU");
        return ResponseEntity.ok(messageCacheService.createMessage(request));
    }

    @PostMapping("/cmn/config")
    @CpfOnlineTransaction(id = "OREFAA0030", name = "REFCmnConfigCreate")
    @Operation(operationId = "refCmnEducationCreateCommonConfig", summary = "CMN config create sample", description = "Creates a sample common config row through CMN.")
    public ResponseEntity<Map<String, Object>> createCommonConfig() {
        CommonConfigRequest request = new CommonConfigRequest();
        request.setConfigKey("REF.EDU.FEATURE." + IdUtils.uuid32().substring(0, 8).toUpperCase() + ".ENABLED");
        request.setConfigValue("Y");
        request.setConfigType("BOOLEAN");
        request.setDescription("REF education feature flag");
        request.setEncryptedYn("N");
        request.setUseYn("Y");
        request.setRequestUser("REF_EDU");
        return ResponseEntity.ok(configCacheService.createConfig(request));
    }

    private String value(Map<String, Object> source, String key) {
        if (source == null) {
            return "";
        }
        Object value = source.get(key);
        if (value == null) {
            value = source.get(key.toUpperCase());
        }
        return value == null ? "" : String.valueOf(value);
    }
}
