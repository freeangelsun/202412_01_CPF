package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfMessageRecord;
import com.cpf.common.message.api.CpfResponseCodeRecord;
import com.cpf.common.message.dto.CommonMessageRequest;
import com.cpf.common.message.dto.CommonResponseCodeRequest;

import java.time.Instant;
import java.util.List;

/** Common Error Catalog runtime/management가 공유하는 internal persistence boundary입니다. */
interface CmnErrorCatalogRepository {
    CpfResponseCodeRecord findResponseCode(String code);
    CpfMessageRecord findMessage(String messageCode, String locale);
    CpfMessageRecord findMessage(long id);
    List<CpfResponseCodeRecord> searchResponseCodes(String query);
    List<CpfMessageRecord> searchMessages(String query, String locale);
    CpfResponseCodeRecord insertResponseCode(CommonResponseCodeRequest request, String actor);
    CpfResponseCodeRecord updateResponseCode(String code, long expectedVersion, CommonResponseCodeRequest request, String actor);
    boolean disableResponseCode(String code, long expectedVersion, String actor);
    CpfMessageRecord insertMessage(CommonMessageRequest request, String actor);
    CpfMessageRecord updateMessage(long id, long expectedVersion, CommonMessageRequest request, String actor);
    boolean disableMessage(long id, long expectedVersion, String actor);
    CatalogFence readFence();

    record FencePart(long rowCount, long versionSum, Instant maxUpdated) { }
    record CatalogFence(FencePart responses, FencePart messages) { }
}
