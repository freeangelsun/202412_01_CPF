package com.cpf.education.online.validation;

import com.cpf.common.message.api.CpfMessageSource;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-04 Validation·표준 오류처리 거래: Controller별 try/catch 없이 CPF 공통 오류 변환 경계를 사용합니다. */
@CpfRestController
@RequestMapping("/edu/online/member-validation")
public class MemberValidationController {
    private final CpfMessageSource messages;

    public MemberValidationController(CpfMessageSource messages) {
        this.messages = messages;
    }

    @PostMapping
    @Operation(operationId = "EDU_MEMBER_VALIDATION", summary = "Validation·표준 오류")
    @CpfOnlineTransaction(
            operationId = "EDU_MEMBER_VALIDATION",
            name = "Validation·표준 오류 거래",
            description = "Jakarta Validation, 업무 Validation, 400/404/409 표준 CPF 오류와 MessageSource를 사용한다.")
    /** validate 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public Map<String, String> validate(@Valid @RequestBody Command command) {
        switch (command.scenario()) {
            case "invalid" -> throw new CpfValidationException("업무 검증에 실패했습니다.");
            case "missing" -> throw new CpfNotFoundException("요청 회원을 찾을 수 없습니다.");
            case "conflict" -> throw new CpfBusinessException(CpfErrorCode.CONFLICT, "다른 요청이 먼저 변경했습니다.");
            default -> { }
        }
        return Map.of(
                "message", messages.getMessage("member.validation.ok", Locale.KOREAN),
                "memberId", command.memberId());
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record Command(
            @NotBlank String memberId,
            @Size(min = 2, max = 40) String name,
            @NotBlank String scenario) { }
}
