package com.cpf.core.common.transaction;

import com.cpf.core.api.transaction.CpfTransactionMetaOperations;
import com.cpf.core.api.data.CpfDataRow;
import com.cpf.core.api.transaction.CpfTransactionMetaScanResult;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Optional;

/** Core 내부 repository/scanner를 공개 거래 메타 계약에 연결합니다. */
public final class DefaultCpfTransactionMetaOperations implements CpfTransactionMetaOperations {
    private final CpfTransactionMetaRepository repository;
    private final ObjectProvider<CpfTransactionMetaScanner> scannerProvider;

    public DefaultCpfTransactionMetaOperations(
            CpfTransactionMetaRepository repository,
            ObjectProvider<CpfTransactionMetaScanner> scannerProvider) {
        this.repository = repository;
        this.scannerProvider = scannerProvider;
    }

    @Override
    public boolean tableAvailable() {
        return repository.tableAvailable();
    }

    @Override
    public List<CpfDataRow> findAll(
            String moduleCode,
            String activeYn,
            String transactionId,
            int limit) {
        return repository.findAll(moduleCode, activeYn, transactionId, limit);
    }

    @Override
    public Optional<CpfDataRow> findById(String transactionId) {
        return repository.findById(transactionId);
    }

    @Override
    public TransactionMetaPage findPage(
            String moduleCode,
            String activeYn,
            String transactionId,
            int page,
            int size) {
        return repository.findPage(moduleCode, activeYn, transactionId, page, size);
    }

    @Override
    public CpfTransactionMetaScanResult scanAndUpsert(String requestUser) {
        CpfTransactionMetaScanner scanner = scannerProvider.getIfAvailable();
        if (scanner == null) {
            return new CpfTransactionMetaScanResult(
                    false,
                    0,
                    0,
                    0,
                    List.of(),
                    "Spring MVC 거래 메타 스캐너를 사용할 수 없습니다.");
        }
        return scanner.scanAndUpsert(requestUser);
    }

    @Override
    public CpfDataRow inactivate(String transactionId, String requestUser) {
        return repository.inactivate(transactionId, requestUser);
    }
}
