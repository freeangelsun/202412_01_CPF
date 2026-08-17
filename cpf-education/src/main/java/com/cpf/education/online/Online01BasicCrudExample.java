package com.cpf.education.online;

import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.data.persistence.api.CpfCrudRepository;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-01 기본 CRUD 거래: CPF Repository와 Validation을 사용하는 실제 업무 CRUD Golden Path입니다. */
@CpfRestController
@RequestMapping("/edu/online/01-members")
public class Online01BasicCrudExample {
    private final MemberService service;

    public Online01BasicCrudExample(MemberService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(operationId = "EDU-ONLINE-01", summary = "기본 CRUD 거래")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-01",
            name = "기본 CRUD 거래",
            description = "CPF Repository를 사용해 생성·조회·수정·삭제를 수행한다.")
    /** execute 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public Map<String, Object> execute(@Valid @RequestBody CrudCommand command) {
        return switch (command.action()) {
            case CREATE -> Map.of("member", service.create(command));
            case READ -> Map.of("member", service.find(command.memberId()));
            case UPDATE -> Map.of("member", service.update(command));
            case DELETE -> {
                service.delete(command.memberId());
                yield Map.of("deleted", command.memberId());
            }
        };
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public enum Action { CREATE, READ, UPDATE, DELETE }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record MemberCommand(@NotBlank String name) { }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record CrudCommand(Action action, @NotBlank String memberId, String name) {
        public CrudCommand {
            if (action == null) throw new IllegalArgumentException("action is required");
        }
    }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record Member(String memberId, String name, long version) { }

    @CpfService
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public static class MemberService {
        private final ObjectProvider<CpfCrudRepository<Member, String>> repositories;

        /** MemberService 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public MemberService(ObjectProvider<CpfCrudRepository<Member, String>> repositories) {
            this.repositories = repositories;
        }

        /** create 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public Member create(CrudCommand command) {
            return repository().save(new Member(command.memberId(), command.name(), 0));
        }

        /** find 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public Member find(String id) {
            return repository().findById(id).orElseThrow(() -> new CpfNotFoundException("회원이 없습니다: " + id));
        }

        /** update 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public Member update(CrudCommand command) {
            Member current = find(command.memberId());
            String name = command.name() == null || command.name().isBlank() ? current.name() : command.name();
            return repository().save(new Member(current.memberId(), name, current.version() + 1));
        }

        /** delete 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public void delete(String id) {
            if (!repository().existsById(id)) throw new CpfNotFoundException("회원이 없습니다: " + id);
            repository().deleteById(id);
        }

        private CpfCrudRepository<Member, String> repository() {
            CpfCrudRepository<Member, String> repository = repositories.getIfAvailable();
            if (repository == null) throw new IllegalStateException("CPF CRUD repository provider is not configured");
            return repository;
        }
    }
}
