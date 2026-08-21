package com.cpf.common.message.api;

import com.cpf.common.message.dto.CommonMessageRequest;
import com.cpf.common.message.dto.CommonResponseCodeRequest;

/** MBW Backoffice가 Owner Table을 직접 갱신하지 않고 소비하는 Common Error/Message 관리 API입니다. */
public interface CpfCommonCatalogManagementService {
    CpfCatalogPage<CpfResponseCodeRecord> searchResponseCodes(String query, Boolean active, int page, int size);
    default CpfCatalogPage<CpfResponseCodeRecord> searchResponseCodes(String query, int page, int size) {
        return searchResponseCodes(query, null, page, size);
    }
    CpfResponseCodeRecord getResponseCode(String responseCode);
    CpfResponseCodeRecord createResponseCode(CommonResponseCodeRequest request, String actor, String reason);
    CpfResponseCodeRecord updateResponseCode(String responseCode, long expectedVersion,
                                               CommonResponseCodeRequest request, String actor, String reason);
    void deleteResponseCode(String responseCode, long expectedVersion, String actor, String reason);

    CpfCatalogPage<CpfMessageRecord> searchMessages(String query, String locale, Boolean active, int page, int size);
    default CpfCatalogPage<CpfMessageRecord> searchMessages(String query, String locale, int page, int size) {
        return searchMessages(query, locale, null, page, size);
    }
    CpfMessageRecord getMessage(long messageId);
    CpfMessageRecord createMessage(CommonMessageRequest request, String actor, String reason);
    CpfMessageRecord updateMessage(long messageId, long expectedVersion,
                                   CommonMessageRequest request, String actor, String reason);
    void deleteMessage(long messageId, long expectedVersion, String actor, String reason);

    void refreshCaches(String actor, String reason);
}
