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

/** 온라인-09 REQUIRED 전파: 서로 다른 Service Bean이 같은 Transaction에 참여하고 자식 실패 시 전체 rollback됩니다. */
@CpfRestController
@RequestMapping("/edu/online/09-required")
public class Online09RequiredTransactionExample {
    private final ParentService parentService;

    public Online09RequiredTransactionExample(ParentService parentService) {
        this.parentService = parentService;
    }

    @PostMapping
    @Operation(operationId = "EDU-ONLINE-09", summary = "Transaction REQUIRED")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-09",
            name = "Transaction REQUIRED 전파 거래",
            description = "상·하위 Service Bean이 동일 Spring Transaction에 참여하고 RuntimeException 시 두 저장을 모두 rollback한다.")
    /** execute 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public Map<String, String> execute(@RequestBody Command command) {
        parentService.execute(command);
        return Map.of("parentId", command.parentId(), "childId", command.childId(), "result", "COMMITTED");
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record Command(String parentId, String childId, boolean failChild) { }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record TxRecord(String id, String type, String value) { }

    @CpfService
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public static class ParentService {
        private final ChildService childService;
        private final ObjectProvider<CpfCrudRepository<TxRecord, String>> repositories;

        /** ParentService 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public ParentService(
                ChildService childService,
                ObjectProvider<CpfCrudRepository<TxRecord, String>> repositories) {
            this.childService = childService;
            this.repositories = repositories;
        }

        @CpfTransactional(propagation = Propagation.REQUIRED)
        /** execute 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public void execute(Command command) {
            repository().save(new TxRecord(command.parentId(), "PARENT", "saved"));
            childService.saveChild(command);
        }

        private CpfCrudRepository<TxRecord, String> repository() {
            CpfCrudRepository<TxRecord, String> repository = repositories.getIfAvailable();
            if (repository == null) throw new IllegalStateException("CPF transaction repository is not configured");
            return repository;
        }
    }

    @CpfService
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public static class ChildService {
        private final ObjectProvider<CpfCrudRepository<TxRecord, String>> repositories;

        /** ChildService 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public ChildService(ObjectProvider<CpfCrudRepository<TxRecord, String>> repositories) {
            this.repositories = repositories;
        }

        @CpfTransactional(propagation = Propagation.REQUIRED)
        /** saveChild 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public void saveChild(Command command) {
            CpfCrudRepository<TxRecord, String> repository = repositories.getIfAvailable();
            if (repository == null) throw new IllegalStateException("CPF transaction repository is not configured");
            repository.save(new TxRecord(command.childId(), "CHILD", "saved"));
            if (command.failChild()) throw new IllegalStateException("child failure triggers REQUIRED rollback");
        }
    }
}
