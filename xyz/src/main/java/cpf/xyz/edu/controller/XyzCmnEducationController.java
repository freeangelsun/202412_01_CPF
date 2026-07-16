package cpf.xyz.edu.controller;

import cpf.cmn.cde.dto.CommonCodeRequest;
import cpf.cmn.cde.service.CodeCacheService;
import cpf.cmn.cfg.dto.CommonConfigRequest;
import cpf.cmn.cfg.service.ConfigCacheService;
import cpf.cmn.msg.dto.CommonMessageRequest;
import cpf.cmn.msg.service.MessageCacheService;
import cpf.cmn.msg.service.ResponseCodeCacheService;
import cpf.cmn.utils.IdUtils;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.execution.CpfOnlineTransaction;
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
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 02. CMN Cache", description = "Common code, message, response code, and config cache samples")
public class XyzCmnEducationController {
    private final CodeCacheService codeCacheService;
    private final MessageCacheService messageCacheService;
    private final ResponseCodeCacheService responseCodeCacheService;
    private final ConfigCacheService configCacheService;

    public XyzCmnEducationController(
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
    @CpfOnlineTransaction(id = "OXYZAA0015", name = "XYZCmnCacheLookup")
    @Operation(operationId = "xyzCmnEducationGetCacheSamples", summary = "CMN cache lookup", description = "Looks up code, message, response code, and config cache entries.")
    public ResponseEntity<Map<String, Object>> getCacheSamples(
            @RequestParam(defaultValue = "USER_STATUS") String codeKey,
            @RequestParam(defaultValue = "MCMN000001") String messageKey,
            @RequestParam(defaultValue = "EPFW010004") String responseCode,
            @RequestParam(defaultValue = "cpf.LOGIN.MAX_FAIL_COUNT") String configKey) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", codeCacheService.getCodesByKey(codeKey));
        response.put("message", messageCacheService.getMessageByKeyAndLocale(messageKey, "ko"));
        response.put("responseCode", responseCodeCacheService.getResponseCode(TextUtils.normalizeCode(responseCode)));
        response.put("config", configCacheService.getConfigByKey(configKey));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/response-code")
    @CpfOnlineTransaction(id = "OXYZAA0026", name = "XYZResponseCodeCacheSample")
    @Operation(operationId = "xyzCmnEducationGetResponseCodeCacheSample", summary = "Response code cache sample", description = "Shows response_code and linked message_code resolution data.")
    public ResponseEntity<Map<String, Object>> getResponseCodeCacheSample(
            @RequestParam(defaultValue = "EXYZ010001") String responseCode,
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
    @CpfOnlineTransaction(id = "OXYZAA0027", name = "XYZMessageFormatSample")
    @Operation(operationId = "xyzCmnEducationGetMessageFormatSample", summary = "Message format sample", description = "Shows fixed and indexed message rows. Indexed messages use {0}, {1}, ... placeholders.")
    public ResponseEntity<Map<String, Object>> getMessageFormatSample(
            @RequestParam(defaultValue = "MXYZ090001") String indexedMessageCode,
            @RequestParam(defaultValue = "MCMN000001") String fixedMessageCode,
            @RequestParam(defaultValue = "ko") String locale) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("indexed", messageCacheService.getMessageByKeyAndLocale(TextUtils.normalizeCode(indexedMessageCode), locale));
        response.put("fixed", messageCacheService.getMessageByKeyAndLocale(TextUtils.normalizeCode(fixedMessageCode), locale));
        response.put("indexedArguments", Map.of("0", "memberNo", "1", "M0001"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/refresh")
    @CpfOnlineTransaction(id = "OXYZAA0016", name = "XYZCmnCacheRefresh")
    @Operation(operationId = "xyzCmnEducationRefreshCaches", summary = "CMN cache refresh", description = "Refreshes CMN caches and publishes refresh events.")
    public ResponseEntity<Map<String, Object>> refreshCaches() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("codes", codeCacheService.refreshCodesAndPublish());
        response.put("messages", messageCacheService.refreshMessagesAndPublish());
        response.put("responseCodes", responseCodeCacheService.refreshResponseCodesAndPublish());
        response.put("configs", configCacheService.refreshConfigsAndPublish());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cmn/code")
    @CpfOnlineTransaction(id = "OXYZAA0010", name = "XYZCmnCodeCreate")
    @Operation(operationId = "xyzCmnEducationCreateCommonCode", summary = "CMN code create sample", description = "Creates a sample common code row through CMN.")
    public ResponseEntity<Map<String, Object>> createCommonCode(
            @RequestParam(required = false) String codeKey,
            @RequestParam(defaultValue = "READY") String codeValue) {

        CommonCodeRequest request = new CommonCodeRequest();
        request.setCodeKey(TextUtils.hasText(codeKey)
                ? TextUtils.normalizeCode(codeKey)
                : "XYZ_SAMPLE_" + IdUtils.uuid32().substring(0, 8).toUpperCase());
        request.setCodeValue(TextUtils.normalizeCode(codeValue));
        request.setDescription("XYZ education common code sample");
        request.setUseYn("Y");
        request.setRequestUser("XYZ_EDU");
        return ResponseEntity.ok(codeCacheService.createCode(request));
    }

    @PostMapping("/cmn/message")
    @CpfOnlineTransaction(id = "OXYZAA0020", name = "XYZCmnMessageCreate")
    @Operation(operationId = "xyzCmnEducationCreateCommonMessage", summary = "CMN message create sample", description = "Creates a sample message row with external/internal templates.")
    public ResponseEntity<Map<String, Object>> createCommonMessage() {
        CommonMessageRequest request = new CommonMessageRequest();
        request.setMessageCode("MXYZ0900" + IdUtils.uuid32().substring(0, 2).toUpperCase());
        request.setLocale("ko");
        request.setMessageFormatType("INDEXED");
        request.setExternalMessage("XYZ education message: {0}");
        request.setInternalMessage("XYZ education internal message sampleName={0}");
        request.setParameterCount(1);
        request.setParameterSample("[\"sample\"]");
        request.setDescription("XYZ education common message sample");
        request.setUseYn("Y");
        request.setRequestUser("XYZ_EDU");
        return ResponseEntity.ok(messageCacheService.createMessage(request));
    }

    @PostMapping("/cmn/config")
    @CpfOnlineTransaction(id = "OXYZAA0030", name = "XYZCmnConfigCreate")
    @Operation(operationId = "xyzCmnEducationCreateCommonConfig", summary = "CMN config create sample", description = "Creates a sample common config row through CMN.")
    public ResponseEntity<Map<String, Object>> createCommonConfig() {
        CommonConfigRequest request = new CommonConfigRequest();
        request.setConfigKey("XYZ.EDU.FEATURE." + IdUtils.uuid32().substring(0, 8).toUpperCase() + ".ENABLED");
        request.setConfigValue("Y");
        request.setConfigType("BOOLEAN");
        request.setDescription("XYZ education feature flag");
        request.setEncryptedYn("N");
        request.setUseYn("Y");
        request.setRequestUser("XYZ_EDU");
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
