package com.cpf.education.common.cmn.controller;
import com.cpf.common.code.api.CpfCodeService;
import com.cpf.common.calendar.api.CpfCalendarService;
import com.cpf.common.message.api.CpfMessageService;
import com.cpf.common.management.CpfCommonManagementApi;
import com.cpf.common.management.CpfCommonMutation;
import com.cpf.common.management.CpfCommonResource;
import com.cpf.common.message.api.CpfCommonCatalogManagementService;
import com.cpf.common.parameter.api.CpfParameterService;
import com.cpf.common.template.api.CpfTemplateService;
import com.cpf.foundation.id.CpfIds;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.platform.operations.observability.api.logging.CpfStructuredLogger;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 실제 CMN 코드·메시지·응답코드·설정 API를 사용하는 EDU 교육 Controller입니다.
 *
 * <p>조회뿐 아니라 생성과 cache refresh까지 제품 Service를 그대로 사용하여,
 * 업무 Domain에서 공통 기준정보와 실시간 캐시 동기화를 적용하는 방법을 설명합니다.</p>
 */
@RestController
@RequestMapping({"/api/education", "/education/edu"})
@Tag(name = "EDU Education 02. CMN Cache", description = "Common code, message, response code, and config cache samples")
public class EducationCmnEducationController extends com.cpf.education.base.EducationBaseController {
    private final CpfCodeService codeService;
    private final CpfMessageService messageService;
    private final CpfParameterService parameterService;
    private final CpfCalendarService calendarService;
    private final ObjectProvider<CpfTemplateService> templateServiceProvider;
    private final Clock clock;
    private final CpfStructuredLogger structuredLogger;
    private final CpfCommonManagementApi commonManagementApi;
    private final CpfCommonCatalogManagementService commonCatalogManagementService;

    /** EducationCmnEducationController 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationCmnEducationController(
            CpfCodeService codeService,
            CpfMessageService messageService,
            CpfParameterService parameterService,
            CpfCalendarService calendarService,
            ObjectProvider<CpfTemplateService> templateServiceProvider,
            Clock clock,
            CpfStructuredLogger structuredLogger,
            CpfCommonManagementApi commonManagementApi,
            CpfCommonCatalogManagementService commonCatalogManagementService) {
        this.codeService = codeService;
        this.messageService = messageService;
        this.parameterService = parameterService;
        this.calendarService = calendarService;
        this.templateServiceProvider = templateServiceProvider;
        this.clock = clock;
        this.structuredLogger = structuredLogger;
        this.commonManagementApi = commonManagementApi;
        this.commonCatalogManagementService = commonCatalogManagementService;
    }

    /**
     * 고객 Domain이 직접 사용하는 Common Product Service Golden Path를 한 번에 보여줍니다.
     * 관리/캐시 구현체가 아니라 공개 {@code Cpf*Service} 계약만 소비합니다.
     */
    @GetMapping("/common/product-services")
    @CpfOnlineTransaction(id = "OEDUAA0009", name = "EDUCommonProductServices", ownerDomain="EDU")
    @Operation(operationId = "eduCommonProductServices", summary = "Common Product Service Golden Path",
            description = "CpfCodeService, CpfMessageService, CpfParameterService, CpfCalendarService와 선택 Template Service를 실제 업무 코드에서 사용하는 예제입니다.")
    public ResponseEntity<Map<String, Object>> commonProductServices(
            @RequestParam(defaultValue = "USER_STATUS") String codeGroup,
            @RequestParam(defaultValue = "READY") String codeValue,
            @RequestParam(defaultValue = "MCMN000001") String messageCode,
            @RequestParam(defaultValue = "ko") String locale,
            @RequestParam(defaultValue = "cpf.LOGIN.MAX_FAIL_COUNT") String parameterKey,
            @RequestParam(defaultValue = "DEFAULT") String calendarId,
            @RequestParam(required = false) String templateCode,
            @RequestParam(defaultValue = "WEB") String templateChannel) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", codeService.find(codeGroup, codeValue).orElse(null));
        response.put("message", messageService.resolve(messageCode, Locale.forLanguageTag(locale), Map.of()));
        response.put("parameter", parameterService.find(parameterKey).orElse(null));
        response.put("nextBusinessDay", calendarService.nextBusinessDay(calendarId, LocalDate.now(clock)));

        CpfTemplateService templateService = templateServiceProvider.getIfAvailable();
        response.put("templateAvailable", templateService != null);
        if (templateService != null && CpfStrings.hasText(templateCode)) {
            response.put("renderedTemplate", templateService.render(templateCode, templateChannel,
                    Map.of("sample", "CPF")));
        }
        structuredLogger.business("education.common.product-services", Map.of(
                "codeGroup", codeGroup,
                "parameterKey", parameterKey,
                "calendarId", calendarId,
                "templateRequested", CpfStrings.hasText(templateCode)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache")
    @CpfOnlineTransaction(id = "OEDUAA0015", name = "EDUCmnCacheLookup", ownerDomain="EDU")
    @Operation(operationId = "refCmnEducationGetCacheSamples", summary = "CMN cache lookup", description = "Looks up code, message, response code, and config cache entries.")
    public ResponseEntity<Map<String, Object>> getCacheSamples(
            @RequestParam(defaultValue = "USER_STATUS") String codeKey,
            @RequestParam(defaultValue = "MCMN000001") String messageKey,
            @RequestParam(defaultValue = "ECPF010004") String responseCode,
            @RequestParam(defaultValue = "cpf.LOGIN.MAX_FAIL_COUNT") String configKey) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", codeService.values(codeKey));
        response.put("message", messageService.resolve(messageKey, Locale.KOREAN));
        response.put("responseCode", commonManagementApi.get(CpfCommonResource.RESPONSE_CODE,
                Map.of("response_code", CpfStrings.normalizeCode(responseCode))));
        response.put("config", parameterService.find(configKey).orElse(null));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/response-code")
    @CpfOnlineTransaction(id = "OEDUAA0026", name = "EDUResponseCodeCacheSample", ownerDomain="EDU")
    @Operation(operationId = "refCmnEducationGetResponseCodeCacheSample", summary = "Response code cache sample", description = "Shows response_code and linked message_code resolution data.")
    public ResponseEntity<Map<String, Object>> getResponseCodeCacheSample(
            @RequestParam(defaultValue = "EEDU010001") String responseCode,
            @RequestParam(defaultValue = "ko") String locale) {

        Map<String, Object> code = commonManagementApi.get(CpfCommonResource.RESPONSE_CODE,
                Map.of("response_code", CpfStrings.normalizeCode(responseCode)));
        String messageCode = value(code, "message_code");
        String message = CpfStrings.hasText(messageCode)
                ? messageService.resolve(messageCode, Locale.forLanguageTag(locale))
                : "";

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("responseCode", code);
        response.put("message", message);
        response.put("usage", "throw new CpfBusinessException(\"" + CpfStrings.normalizeCode(responseCode) + "\", detail, args)");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/message-format")
    @CpfOnlineTransaction(id = "OEDUAA0027", name = "EDUMessageFormatSample", ownerDomain="EDU")
    @Operation(operationId = "refCmnEducationGetMessageFormatSample", summary = "Message format sample", description = "Shows fixed and indexed message rows. Indexed messages use {0}, {1}, ... placeholders.")
    public ResponseEntity<Map<String, Object>> getMessageFormatSample(
            @RequestParam(defaultValue = "EDU090001") String indexedMessageCode,
            @RequestParam(defaultValue = "MCMN000001") String fixedMessageCode,
            @RequestParam(defaultValue = "ko") String locale) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("indexed", messageService.resolve(CpfStrings.normalizeCode(indexedMessageCode),
                Locale.forLanguageTag(locale), Map.of("0", "memberNo", "1", "M0001")));
        response.put("fixed", messageService.resolve(CpfStrings.normalizeCode(fixedMessageCode), Locale.forLanguageTag(locale)));
        response.put("indexedArguments", Map.of("0", "memberNo", "1", "M0001"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/refresh")
    @CpfOnlineTransaction(id = "OEDUAA0016", name = "EDUCmnCacheRefresh", ownerDomain="EDU")
    @Operation(operationId = "refCmnEducationRefreshCaches", summary = "CMN cache refresh", description = "Refreshes CMN caches and publishes refresh events.")
    /** refreshCaches 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> refreshCaches() {
        commonCatalogManagementService.refreshCaches("EDU_EDU", "EDU Common Product Service cache refresh");
        return ResponseEntity.ok(Map.of("status", "REFRESH_REQUESTED", "owner", "CpfCommonCatalogManagementService"));
    }

    @PostMapping("/cmn/code")
    @CpfOnlineTransaction(id = "OEDUAA0010", name = "EDUCmnCodeCreate", ownerDomain="EDU")
    @Operation(operationId = "refCmnEducationCreateCommonCode", summary = "CMN code create sample", description = "Creates a sample common code row through CMN.")
    /** createCommonCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> createCommonCode(
            @RequestParam(required = false) String codeKey,
            @RequestParam(defaultValue = "READY") String codeValue) {

        String key = CpfStrings.hasText(codeKey)
                ? CpfStrings.normalizeCode(codeKey)
                : "EDU_SAMPLE_" + CpfIds.uuid32().substring(0, 8).toUpperCase();
        Map<String, Object> created = commonManagementApi.create(CpfCommonResource.CODE,
                new CpfCommonMutation(Map.of(), Map.of(
                        "code_key", key,
                        "code_value", CpfStrings.normalizeCode(codeValue),
                        "description", "EDU education common code sample",
                        "use_yn", "Y"), null, "EDU Common Code 생성"), "EDU_EDU");
        return ResponseEntity.ok(created);
    }

    @PostMapping("/cmn/message")
    @CpfOnlineTransaction(id = "OEDUAA0020", name = "EDUCmnMessageCreate", ownerDomain="EDU")
    @Operation(operationId = "refCmnEducationCreateCommonMessage", summary = "CMN message create sample", description = "Creates a sample message row with external/internal templates.")
    /** createCommonMessage 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> createCommonMessage() {
        String messageCode = "EDU0900" + CpfIds.uuid32().substring(0, 2).toUpperCase();
        Map<String, Object> created = commonManagementApi.create(CpfCommonResource.MESSAGE,
                new CpfCommonMutation(Map.of(), Map.of(
                        "message_code", messageCode,
                        "locale", "ko",
                        "message_format_type", "INDEXED",
                        "external_message", "EDU education message: {0}",
                        "internal_message", "EDU education internal message sampleName={0}",
                        "parameter_count", 1,
                        "parameter_sample", "[\"sample\"]",
                        "description", "EDU education common message sample",
                        "use_yn", "Y"), null, "EDU Common Message 생성"), "EDU_EDU");
        return ResponseEntity.ok(created);
    }

    @PostMapping("/cmn/config")
    @CpfOnlineTransaction(id = "OEDUAA0030", name = "EDUCmnConfigCreate", ownerDomain="EDU")
    @Operation(operationId = "refCmnEducationCreateCommonConfig", summary = "CMN config create sample", description = "Creates a sample common config row through CMN.")
    /** createCommonConfig 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> createCommonConfig() {
        String configKey = "EDU.EDU.FEATURE." + CpfIds.uuid32().substring(0, 8).toUpperCase() + ".ENABLED";
        Map<String, Object> created = commonManagementApi.create(CpfCommonResource.PARAMETER,
                new CpfCommonMutation(Map.of(), Map.of(
                        "config_key", configKey,
                        "config_value", "Y",
                        "config_type", "BOOLEAN",
                        "description", "EDU education feature flag",
                        "encrypted_yn", "N",
                        "use_yn", "Y"), null, "EDU Common Parameter 생성"), "EDU_EDU");
        return ResponseEntity.ok(created);
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
