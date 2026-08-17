package com.cpf.education.online;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-05 동일 Application 내부 Service 호출: self HTTP 없이 Bean→Bean으로 Context와 Transaction을 유지합니다. */
@CpfRestController
@RequestMapping("/edu/online/05-local-service")
public class Online05LocalServiceCallExample {
    private final EntryService entryService;

    public Online05LocalServiceCallExample(EntryService entryService) {
        this.entryService = entryService;
    }

    @PostMapping
    @Operation(operationId = "EDU-ONLINE-05", summary = "동일 Application Service 호출")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-05",
            name = "동일 Application Service 호출",
            description = "Self HTTP 없이 같은 JVM의 Service Bean을 직접 호출하고 CPF Context와 REQUIRED Transaction을 유지한다.")
    /** call 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public Map<String, String> call(@RequestBody String value) {
        String before = CpfContexts.transactionId();
        String result = entryService.process(value);
        return Map.of("transactionId", before, "result", result);
    }

    @CpfService
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public static class EntryService {
        private final ChildService childService;
        /** EntryService 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public EntryService(ChildService childService) { this.childService = childService; }

        @CpfTransactional(propagation = Propagation.REQUIRED)
        /** process 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public String process(String value) {
            return childService.process(value, CpfContexts.transactionId());
        }
    }

    @CpfService
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public static class ChildService {
        @CpfTransactional(propagation = Propagation.REQUIRED)
        /** process 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public String process(String value, String expectedTransactionId) {
            if (!expectedTransactionId.equals(CpfContexts.transactionId())) {
                throw new IllegalStateException("CPF context continuity failed");
            }
            if ("FAIL".equalsIgnoreCase(value)) throw new IllegalArgumentException("업무 처리 실패 예시");
            return "processed:" + value;
        }
    }
}
