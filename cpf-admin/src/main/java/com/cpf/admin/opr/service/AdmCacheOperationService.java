package com.cpf.admin.opr.service;

import com.cpf.common.cde.service.CodeCacheService;
import com.cpf.common.cfg.service.ConfigCacheService;
import com.cpf.common.msg.service.MessageCacheService;
import com.cpf.common.msg.service.ResponseCodeCacheService;
import com.cpf.common.utils.TextUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM 캐시 운영 서비스입니다.
 * 운영자가 코드, 메시지, 응답코드, 설정 캐시 상태를 확인하고 refresh 요청을 수행할 수 있게 합니다.
 */
@Service
public class AdmCacheOperationService extends com.cpf.admin.common.base.AdmBaseService {
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
     * 캐시 대표 샘플을 조회합니다.
     *
     * @return 캐시 이름과 샘플 데이터
     */
    public Map<String, Object> summary() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("cacheNames", "codeCache, messageCache, responseCodeCache, configCache");
        response.put("codeSample", codeCacheService.getCodesByKey("USER_STATUS"));
        response.put("messageSample", messageCacheService.getMessageByKeyAndLocale("MCMN000001", "ko"));
        response.put("responseCodeSample", responseCodeCacheService.getResponseCode("ECPF010004"));
        response.put("configSample", configCacheService.getConfigByKey("cpf.LOGIN.MAX_FAIL_COUNT"));
        return response;
    }

    /**
     * 지정된 캐시를 갱신합니다.
     *
     * @param target ALL, CODE, MESSAGE, RESPONSE_CODE, CONFIG 중 하나
     * @return refresh 결과
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
