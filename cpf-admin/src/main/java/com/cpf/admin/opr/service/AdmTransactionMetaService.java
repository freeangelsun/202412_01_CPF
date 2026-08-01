package com.cpf.admin.opr.service;

import com.cpf.core.api.transaction.CpfTransactionMetaOperations;
import com.cpf.core.api.data.CpfDataRow;
import com.cpf.core.api.transaction.CpfTransactionMetaScanResult;
import org.springframework.stereotype.Service;


/**
 * ADM 거래 메타 운영 서비스입니다.
 */
@Service
public class AdmTransactionMetaService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfTransactionMetaOperations operations;

    public AdmTransactionMetaService(CpfTransactionMetaOperations operations) {
        this.operations = operations;
    }

    public CpfDataRow findTransactions(String moduleCode, String activeYn, String transactionId, int limit) {
        CpfDataRow response = new CpfDataRow();
        response.put("available", operations.tableAvailable());
        response.put("items", operations.findAll(moduleCode, activeYn, transactionId, limit));
        return response;
    }

    public CpfTransactionMetaOperations.TransactionMetaPage findPage(
            String moduleCode,
            String activeYn,
            String transactionId,
            int page,
            int size) {
        return operations.findPage(moduleCode, activeYn, transactionId, page, size);
    }

    public CpfDataRow findTransaction(String transactionId) {
        CpfDataRow response = new CpfDataRow();
        response.put("available", operations.tableAvailable());
        response.put("item", operations.findById(transactionId).orElse(CpfDataRow.of()));
        return response;
    }

    public CpfTransactionMetaScanResult scan(String requestUser) {
        return operations.scanAndUpsert(requestUser);
    }

    public CpfDataRow inactivate(String transactionId, String requestUser) {
        return operations.inactivate(transactionId, requestUser);
    }
}
