package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfCatalogPage;
import com.cpf.common.message.api.CpfCommonCatalogManagementService;
import com.cpf.common.management.CpfCommonManagementAuditSink;
import com.cpf.common.message.api.CpfMessageRecord;
import com.cpf.common.message.api.CpfResponseCodeRecord;
import com.cpf.common.message.dto.CommonMessageRequest;
import com.cpf.common.message.dto.CommonResponseCodeRequest;
import com.cpf.core.api.error.CpfBusinessException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** MBW Backoffice와 고객 Application이 소비하는 Common Error/Message 관리 Product Service입니다. */
@Service
public final class CmnCommonCatalogManagementService implements CpfCommonCatalogManagementService {
    private static final String TX = "cpfCommonTransactionManager";
    private final CmnErrorCatalogRepository repository;
    private final CmnErrorCatalogCache cache;
    private final CpfCommonManagementAuditSink audit;
    private final Clock clock;

    public CmnCommonCatalogManagementService(CmnErrorCatalogRepository repository, CmnErrorCatalogCache cache, CpfCommonManagementAuditSink audit) {
        this(repository, cache, audit, Clock.systemUTC());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public CmnCommonCatalogManagementService(CmnErrorCatalogRepository repository, CmnErrorCatalogCache cache, CpfCommonManagementAuditSink audit, Clock clock) {
        this.repository = repository; this.cache = cache; this.audit = audit; this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TX)
    public CpfCatalogPage<CpfResponseCodeRecord> searchResponseCodes(String query, Boolean active, int page, int size) {
        var rows=repository.searchResponseCodes(query);
        if(active!=null) rows=rows.stream().filter(r -> active ? r.activeAt(clock.instant()) : !r.activeAt(clock.instant())).toList();
        return page(rows, page, size);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TX)
    public CpfResponseCodeRecord getResponseCode(String responseCode) {
        CpfResponseCodeRecord row = repository.findResponseCode(responseCode);
        if (row == null) throw notFound("RESPONSE_CODE", responseCode);
        return row;
    }

    @Override
    @Transactional(transactionManager = TX)
    public CpfResponseCodeRecord createResponseCode(CommonResponseCodeRequest request, String actor, String reason) {
        validateRequest(request, actor, reason);
        try {
            CpfResponseCodeRecord result = repository.insertResponseCode(request, actor(actor));
            afterCommit("CREATE", "RESPONSE_CODE", result.responseCode(), actor, reason,
                    Map.of("catalogVersion", result.catalogVersion(), "messageCode", result.messageCode()));
            return result;
        } catch (DataAccessException ex) {
            throw duplicate("RESPONSE_CODE", request == null ? null : request.getResponseCode());
        }
    }

    @Override
    @Transactional(transactionManager = TX)
    public CpfResponseCodeRecord updateResponseCode(String responseCode, long expectedVersion,
                                                     CommonResponseCodeRequest request, String actor, String reason) {
        validateRequest(request, actor, reason);
        if (expectedVersion < 1) throw invalid("expectedVersion");
        try {
            CpfResponseCodeRecord result = repository.updateResponseCode(responseCode, expectedVersion, request, actor(actor));
            if (result == null) throw conflict("RESPONSE_CODE", responseCode, expectedVersion);
            afterCommit("UPDATE", "RESPONSE_CODE", result.responseCode(), actor, reason,
                    Map.of("catalogVersion", result.catalogVersion(), "messageCode", result.messageCode()));
            return result;
        } catch (DataAccessException ex) {
            throw duplicate("RESPONSE_CODE", responseCode);
        }
    }

    @Override
    @Transactional(transactionManager = TX)
    public void deleteResponseCode(String responseCode, long expectedVersion, String actor, String reason) {
        requireActorReason(actor, reason);
        if (expectedVersion < 1) throw invalid("expectedVersion");
        if (!repository.disableResponseCode(responseCode, expectedVersion, actor(actor))) throw conflict("RESPONSE_CODE", responseCode, expectedVersion);
        afterCommit("DISABLE", "RESPONSE_CODE", responseCode, actor, reason, Map.of("catalogVersion", expectedVersion));
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TX)
    public CpfCatalogPage<CpfMessageRecord> searchMessages(String query, String locale, Boolean active, int page, int size) {
        var rows=repository.searchMessages(query, locale);
        if(active!=null) rows=rows.stream().filter(r -> active ? r.activeAt(clock.instant()) : !r.activeAt(clock.instant())).toList();
        return page(rows, page, size);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TX)
    public CpfMessageRecord getMessage(long messageId) {
        CpfMessageRecord row = repository.findMessage(messageId);
        if (row == null) throw notFound("MESSAGE", String.valueOf(messageId));
        return row;
    }

    @Override
    @Transactional(transactionManager = TX)
    public CpfMessageRecord createMessage(CommonMessageRequest request, String actor, String reason) {
        validateRequest(request, actor, reason);
        try {
            CpfMessageRecord result = repository.insertMessage(request, actor(actor));
            afterCommit("CREATE", "MESSAGE", result.messageCode() + ":" + result.locale(), actor, reason,
                    Map.of("messageId", result.messageId(), "catalogVersion", result.catalogVersion()));
            return result;
        } catch (DataAccessException ex) {
            throw duplicate("MESSAGE", request == null ? null : request.getEffectiveMessageCode());
        }
    }

    @Override
    @Transactional(transactionManager = TX)
    public CpfMessageRecord updateMessage(long messageId, long expectedVersion,
                                          CommonMessageRequest request, String actor, String reason) {
        validateRequest(request, actor, reason);
        if (expectedVersion < 1) throw invalid("expectedVersion");
        try {
            CpfMessageRecord result = repository.updateMessage(messageId, expectedVersion, request, actor(actor));
            if (result == null) throw conflict("MESSAGE", String.valueOf(messageId), expectedVersion);
            afterCommit("UPDATE", "MESSAGE", result.messageCode() + ":" + result.locale(), actor, reason,
                    Map.of("messageId", result.messageId(), "catalogVersion", result.catalogVersion()));
            return result;
        } catch (DataAccessException ex) {
            throw duplicate("MESSAGE", request == null ? String.valueOf(messageId) : request.getEffectiveMessageCode());
        }
    }

    @Override
    @Transactional(transactionManager = TX)
    public void deleteMessage(long messageId, long expectedVersion, String actor, String reason) {
        requireActorReason(actor, reason);
        if (expectedVersion < 1) throw invalid("expectedVersion");
        if (!repository.disableMessage(messageId, expectedVersion, actor(actor))) throw conflict("MESSAGE", String.valueOf(messageId), expectedVersion);
        afterCommit("DISABLE", "MESSAGE", String.valueOf(messageId), actor, reason, Map.of("catalogVersion", expectedVersion));
    }

    @Override
    public void refreshCaches(String actor, String reason) {
        requireActorReason(actor, reason);
        cache.invalidateAll();
        audit.record("REFRESH", "ERROR_MESSAGE_CATALOG", "ALL", actor(actor), reason(reason),
                Map.of("responseCacheSize", cache.responseCacheSize(), "messageCacheSize", cache.messageCacheSize()));
    }

    private void afterCommit(String action, String resourceType, String key, String actor, String reason,
                             Map<String, Object> attributes) {
        Runnable work = () -> {
            cache.invalidateAll();
            audit.record(action, resourceType, safe(key), actor(actor), reason(reason), attributes);
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { work.run(); }
            });
        } else {
            // Transaction manager가 비정상적으로 synchronization을 만들지 않은 경우에도 stale cache를 남기지 않습니다.
            work.run();
        }
    }

    private <T> CpfCatalogPage<T> page(List<T> rows, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(500, Math.max(1, size));
        long startLong = (long) safePage * safeSize;
        if (startLong >= rows.size()) return new CpfCatalogPage<>(List.of(), safePage, safeSize, rows.size());
        int start = (int) startLong;
        int end = Math.min(rows.size(), start + safeSize);
        return new CpfCatalogPage<>(rows.subList(start, end), safePage, safeSize, rows.size());
    }

    private void validateRequest(CommonResponseCodeRequest request, String actor, String reason) {
        if (request == null) throw invalid("request");
        requireActorReason(actor, reason);
        String code = request.getResponseCode();
        if (code == null || !code.trim().toUpperCase(Locale.ROOT).matches("[A-Z0-9_.:-]{2,20}")) throw invalid("responseCode");
        String messageCode = request.getMessageCode();
        if (messageCode == null || !messageCode.trim().toUpperCase(Locale.ROOT).matches("[A-Z0-9_.:-]{2,20}")) throw invalid("messageCode");
        if (request.getEffectiveFrom() != null && request.getEffectiveTo() != null && !request.getEffectiveTo().isAfter(request.getEffectiveFrom()))
            throw invalid("effective period");
    }

    private void validateRequest(CommonMessageRequest request, String actor, String reason) {
        if (request == null) throw invalid("request");
        requireActorReason(actor, reason);
        String code = request.getEffectiveMessageCode();
        if (code == null || !code.trim().toUpperCase(Locale.ROOT).matches("[A-Z0-9_.:-]{2,20}")) throw invalid("messageCode");
        if (request.getLocale() == null || !request.getLocale().matches("[A-Za-z]{2,8}([_-][A-Za-z0-9]{2,8})?")) throw invalid("locale");
        if (request.getEffectiveExternalMessage() == null || request.getEffectiveExternalMessage().isBlank()) throw invalid("externalMessage");
        if (request.getEffectiveInternalMessage() == null || request.getEffectiveInternalMessage().isBlank()) throw invalid("internalMessage");
        if (request.getEffectiveFrom() != null && request.getEffectiveTo() != null && !request.getEffectiveTo().isAfter(request.getEffectiveFrom()))
            throw invalid("effective period");
    }

    private void requireActorReason(String actor, String reason) {
        if (actor == null || actor.isBlank()) throw invalid("actor");
        if (reason == null || reason.isBlank()) throw invalid("reason");
    }
    private String actor(String value) { return safe(value); }
    private String reason(String value) { String v=safe(value); return v.length() > 500 ? v.substring(0,500) : v; }
    private String safe(String value) { return value == null ? "-" : value.replace('\r',' ').replace('\n',' ').trim(); }

    private CpfBusinessException duplicate(String type, String key) {
        return new CpfBusinessException("CPF.CMN.CATALOG.DUPLICATE", Map.of("type", safe(type), "key", safe(key)));
    }
    private CpfBusinessException conflict(String type, String key, long version) {
        return new CpfBusinessException("CPF.CMN.CATALOG.CONFLICT", Map.of("type", safe(type), "key", safe(key), "version", version));
    }
    private CpfBusinessException notFound(String type, String key) {
        return new CpfBusinessException("CPF.CMN.CATALOG.NOT_FOUND", Map.of("type", safe(type), "key", safe(key)));
    }
    private CpfBusinessException invalid(String field) {
        return new CpfBusinessException("CPF.CMN.CATALOG.INVALID", Map.of("field", safe(field)));
    }
}
