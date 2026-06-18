package cpf.xyz.edu.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Service
public class XyzAuditSampleService {
    private final List<String> auditMessages = new ArrayList<>();

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void writeAuditRequiresNew(String message) {
        auditMessages.add(message);
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    public List<String> getAuditMessages() {
        return List.copyOf(auditMessages);
    }
}

