package com.cpf.admin.opr.service;

import com.cpf.core.api.transaction.CpfTransactionMetaOperations;
import com.cpf.core.api.transaction.CpfTransactionMetaScanResult;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM 거래 메타 운영 서비스입니다.
 */
@Service
public class AdmTransactionMetaService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfTransactionMetaOperations operations;

    public AdmTransactionMetaService(CpfTransactionMetaOperations operations) {
        this.operations = operations;
    }

    public Map<String, Object> findTransactions(String moduleCode, String activeYn, String transactionId, int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", operations.tableAvailable());
        response.put("items", operations.findAll(moduleCode, activeYn, transactionId, limit));
        return response;
    }

    public Map<String, Object> findTransaction(String transactionId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", operations.tableAvailable());
        response.put("item", operations.findById(transactionId).orElse(Map.of()));
        return response;
    }

    public CpfTransactionMetaScanResult scan(String requestUser) {
        return operations.scanAndUpsert(requestUser);
    }

    public Map<String, Object> inactivate(String transactionId, String requestUser) {
        return operations.inactivate(transactionId, requestUser);
    }
}
