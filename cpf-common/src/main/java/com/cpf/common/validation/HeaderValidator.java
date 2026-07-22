package com.cpf.common.validation;

import com.cpf.common.dto.HeaderDTO;
import com.cpf.common.utils.ValidationUtils;

/** 표준 거래 헤더의 필수값과 형식을 검증하는 CMN 진입점입니다. */
public class HeaderValidator {

    /** 공통 검증 규칙에 따라 거래 헤더를 검증하고 위반 시 표준 검증 예외를 발생시킵니다. */
    public void validate(HeaderDTO header) {
        ValidationUtils.validateHeader(header);
    }
}

