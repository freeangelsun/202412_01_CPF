package com.cpf.education.online;

import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.data.persistence.api.CpfCrudRepository;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-19 동시성·Optimistic Lock: stale version을 CPF CONFLICT로 분류하고 blind retry를 막습니다. */
@CpfRestController
@RequestMapping("/edu/online/19-concurrency")
public class Online19OptimisticLockExample {
    private final ObjectProvider<CpfCrudRepository<MemberVersion, String>> repositories;

    public Online19OptimisticLockExample(ObjectProvider<CpfCrudRepository<MemberVersion, String>> repositories) {
        this.repositories = repositories;
    }

    @PutMapping
    @CpfTransactional
    @Operation(operationId = "EDU-ONLINE-19", summary = "동시성·Optimistic Lock 거래")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-19",
            name = "동시성·Optimistic Lock 거래",
            description = "Version 기반 동시갱신 충돌을 CPF CONFLICT(409)로 분류하고 동일 요청의 무조건 재시도를 금지한다.")
    /** update 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public MemberVersion update(@RequestBody MemberVersion command) {
        CpfCrudRepository<MemberVersion, String> repository = repository();
        MemberVersion current = repository.findById(command.memberId())
                .orElseThrow(() -> new CpfNotFoundException("회원이 없습니다: " + command.memberId()));
        if (current.version() != command.version()) {
            throw new CpfBusinessException(CpfErrorCode.CONFLICT, "이미 다른 거래가 변경했습니다. 최신 값을 다시 조회하십시오.");
        }
        return repository.save(new MemberVersion(command.memberId(), command.value(), command.version() + 1));
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record MemberVersion(String memberId, String value, long version) { }

    private CpfCrudRepository<MemberVersion, String> repository() {
        CpfCrudRepository<MemberVersion, String> repository = repositories.getIfAvailable();
        if (repository == null) throw new IllegalStateException("CPF repository provider is not configured");
        return repository;
    }
}
