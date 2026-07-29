package com.cpf.member.sampleitem.validation;

import com.cpf.member.sampleitem.dto.MemberSearchRequest;
import com.cpf.core.api.error.CpfValidationException;
import org.springframework.stereotype.Component;

/**
 * Member 조회 API 입력값을 검증합니다.
 */
@Component
public class MemberSearchValidator {
    public void validate(MemberSearchRequest request) {
        if (request == null) {
            throw new CpfValidationException("Member 조회 조건은 필수입니다.");
        }
        if (request.size() != null && request.size() > 200) {
            throw new CpfValidationException("페이지 크기는 200 이하여야 합니다.");
        }
    }
}