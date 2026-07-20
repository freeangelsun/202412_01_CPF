package cpf.adm.opr.service;

import cpf.pfw.common.transaction.CpfTransactionMetaRepository;
import cpf.pfw.common.transaction.CpfTransactionMetaScanResult;
import cpf.pfw.common.transaction.CpfTransactionMetaScanner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM 거래 메타 운영 서비스입니다.
 */
@Service
public class AdmTransactionMetaService extends cpf.adm.common.base.AdmBaseService {
    private final CpfTransactionMetaRepository repository;
    private final ObjectProvider<CpfTransactionMetaScanner> scannerProvider;

    public AdmTransactionMetaService(
            CpfTransactionMetaRepository repository,
            ObjectProvider<CpfTransactionMetaScanner> scannerProvider) {
        this.repository = repository;
        this.scannerProvider = scannerProvider;
    }

    public Map<String, Object> findTransactions(String moduleCode, String activeYn, String transactionId, int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", repository.tableAvailable());
        response.put("items", repository.findAll(moduleCode, activeYn, transactionId, limit));
        return response;
    }

    public Map<String, Object> findTransaction(String transactionId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", repository.tableAvailable());
        response.put("item", repository.findById(transactionId).orElse(Map.of()));
        return response;
    }

    public CpfTransactionMetaScanResult scan(String requestUser) {
        CpfTransactionMetaScanner scanner = scannerProvider.getIfAvailable();
        if (scanner == null) {
            return new CpfTransactionMetaScanResult(false, 0, 0, 0, java.util.List.of(), "Spring MVC 거래 메타 스캐너를 사용할 수 없습니다.");
        }
        return scanner.scanAndUpsert(requestUser);
    }

    public Map<String, Object> inactivate(String transactionId, String requestUser) {
        return repository.inactivate(transactionId, requestUser);
    }
}
