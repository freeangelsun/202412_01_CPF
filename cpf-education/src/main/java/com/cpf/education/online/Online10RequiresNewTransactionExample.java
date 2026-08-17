package com.cpf.education.online;

import com.cpf.data.persistence.api.CpfCrudRepository;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-10 REQUIRES_NEW: 외부 Bean의 독립 감사 저장은 상위 Transaction rollback과 분리됩니다. */
@CpfRestController
@RequestMapping("/edu/online/10-requires-new")
public class Online10RequiresNewTransactionExample {
    private final OuterService outerService;

    public Online10RequiresNewTransactionExample(OuterService outerService) {
        this.outerService = outerService;
    }

    @PostMapping
    @Operation(operationId = "EDU-ONLINE-10", summary = "Transaction REQUIRES_NEW")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-10",
            name = "Transaction REQUIRES_NEW 독립 거래",
            description = "상위 Transaction을 suspend하고 독립 감사 Transaction을 commit한 뒤 상위 rollback과 분리되는 실제 Bean 경계를 보여준다.")
    /** execute 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public Map<String, String> execute(@RequestBody Command command) {
        outerService.execute(command);
        return Map.of("businessId", command.businessId(), "auditId", command.auditId(), "result", "COMMITTED");
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record Command(String businessId, String auditId, boolean failOuter) { }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record TxRecord(String id, String type, String value) { }

    @CpfService
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public static class OuterService {
        private final IndependentAuditService auditService;
        private final ObjectProvider<CpfCrudRepository<TxRecord, String>> repositories;

        /** OuterService 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public OuterService(
                IndependentAuditService auditService,
                ObjectProvider<CpfCrudRepository<TxRecord, String>> repositories) {
            this.auditService = auditService;
            this.repositories = repositories;
        }

        @CpfTransactional(propagation = Propagation.REQUIRED)
        /** execute 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public void execute(Command command) {
            CpfCrudRepository<TxRecord, String> repository = repositories.getIfAvailable();
            if (repository == null) throw new IllegalStateException("CPF transaction repository is not configured");
            repository.save(new TxRecord(command.businessId(), "BUSINESS", "pending"));
            auditService.writeAudit(command.auditId());
            if (command.failOuter()) throw new IllegalStateException("outer rollback after independent commit");
            repository.save(new TxRecord(command.businessId(), "BUSINESS", "completed"));
        }
    }

    @CpfService
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public static class IndependentAuditService {
        private final ObjectProvider<CpfCrudRepository<TxRecord, String>> repositories;

        /** IndependentAuditService 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public IndependentAuditService(ObjectProvider<CpfCrudRepository<TxRecord, String>> repositories) {
            this.repositories = repositories;
        }

        @CpfTransactional(propagation = Propagation.REQUIRES_NEW)
        /** writeAudit 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public void writeAudit(String auditId) {
            CpfCrudRepository<TxRecord, String> repository = repositories.getIfAvailable();
            if (repository == null) throw new IllegalStateException("CPF transaction repository is not configured");
            repository.save(new TxRecord(auditId, "AUDIT", "independent-commit"));
        }
    }
}
