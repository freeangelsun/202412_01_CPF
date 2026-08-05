package com.cpf.common.validation;

import com.cpf.common.dto.HeaderDTO;

/** 표준 거래 헤더의 필수값과 형식을 검증하는 CMN 진입점입니다. */
public class HeaderValidator {

    /**
     * CMN 소유 HeaderDTO의 필수 필드를 검증합니다.
     *
     * <p>HeaderDTO는 cpf-common 계약이므로 이 검증은 범용 Core utility로 역이관하지 않습니다.</p>
     */
    public void validate(HeaderDTO header) {
        if (header == null) {
            throw new IllegalArgumentException("Header는 필수 값입니다.");
        }
        if (header.getTransactionId() == null || header.getTransactionId().isBlank()) {
            throw new IllegalArgumentException("Transaction ID는 필수 값입니다.");
        }
        if (header.getInitialChannelCode() == null || header.getInitialChannelCode().isBlank()) {
            throw new IllegalArgumentException("Initial Channel Code는 필수 값입니다.");
        }
        if (header.getChannelCode() == null || header.getChannelCode().isBlank()) {
            throw new IllegalArgumentException("Channel Code는 필수 값입니다.");
        }
        if (header.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp는 필수 값입니다.");
        }
    }
}
