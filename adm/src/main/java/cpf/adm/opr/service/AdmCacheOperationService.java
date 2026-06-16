package cpf.adm.opr.service;

import cpf.cmn.cde.service.CodeCacheService;
import cpf.cmn.cfg.service.ConfigCacheService;
import cpf.cmn.msg.service.MessageCacheService;
import cpf.cmn.msg.service.ResponseCodeCacheService;
import cpf.cmn.utils.TextUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM?먯꽌 CMN 肄붾뱶/硫붿떆吏/?ㅼ젙 罹먯떆瑜??섎룞 由ы봽?덉떆?섍린 ?꾪븳 ?쒕퉬?ㅼ엯?덈떎.
 */
@Service
public class AdmCacheOperationService {
    private final CodeCacheService codeCacheService;
    private final MessageCacheService messageCacheService;
    private final ResponseCodeCacheService responseCodeCacheService;
    private final ConfigCacheService configCacheService;

    public AdmCacheOperationService(
            CodeCacheService codeCacheService,
            MessageCacheService messageCacheService,
            ResponseCodeCacheService responseCodeCacheService,
            ConfigCacheService configCacheService) {
        this.codeCacheService = codeCacheService;
        this.messageCacheService = messageCacheService;
        this.responseCodeCacheService = responseCodeCacheService;
        this.configCacheService = configCacheService;
    }

    /**
     * 罹먯떆 愿由??붾㈃??湲곕낯 ?곹깭瑜?議고쉶?⑸땲??
     *
     * @return 罹먯떆蹂??덈궡? ????곗씠??     */
    public Map<String, Object> summary() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("cacheNames", "codeCache, messageCache, responseCodeCache, configCache");
        response.put("codeSample", codeCacheService.getCodesByKey("USER_STATUS"));
        response.put("messageSample", messageCacheService.getMessageByKeyAndLocale("MCMN000001", "ko"));
        response.put("responseCodeSample", responseCodeCacheService.getResponseCode("EPFW010004"));
        response.put("configSample", configCacheService.getConfigByKey("cpf.LOGIN.MAX_FAIL_COUNT"));
        return response;
    }

    /**
     * 罹먯떆瑜??섎룞 由ы봽?덉떆?⑸땲??
     *
     * @param target ALL, CODE, MESSAGE, RESPONSE_CODE, CONFIG
     * @return 由ы봽?덉떆 寃곌낵
     */
    public Map<String, Object> refresh(String target) {
        String normalizedTarget = TextUtils.normalizeCode(target);
        if (!TextUtils.hasText(normalizedTarget)) {
            normalizedTarget = "ALL";
        }

        Map<String, Object> response = new LinkedHashMap<>();
        if ("ALL".equals(normalizedTarget) || "CODE".equals(normalizedTarget)) {
            response.put("codes", codeCacheService.refreshCodesAndPublish());
        }
        if ("ALL".equals(normalizedTarget) || "MESSAGE".equals(normalizedTarget)) {
            response.put("messages", messageCacheService.refreshMessagesAndPublish());
        }
        if ("ALL".equals(normalizedTarget) || "RESPONSE_CODE".equals(normalizedTarget)) {
            response.put("responseCodes", responseCodeCacheService.refreshResponseCodesAndPublish());
        }
        if ("ALL".equals(normalizedTarget) || "CONFIG".equals(normalizedTarget)) {
            response.put("configs", configCacheService.refreshConfigsAndPublish());
        }
        response.put("target", normalizedTarget);
        return response;
    }
}

