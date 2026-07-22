package com.cpf.common.utils;

import com.cpf.common.dto.HeaderDTO;

/**
 * 공통 유효성 검사 유틸리티입니다.
 *
 * <p>프레임워크 표준 헤더처럼 여러 모듈에서 반복 검증하는 값은 이 유틸리티로 동일한 기준을 적용합니다.</p>
 */
public class ValidationUtils {

    /**
     * HeaderDTO의 필수 필드를 검증합니다.
     *
     * @param header 검증할 HeaderDTO 객체
     * @throws IllegalArgumentException 필수 값이 없을 때 발생
     */
    public static void validateHeader(HeaderDTO header) {
        if (header.getTransactionId() == null || header.getTransactionId().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID는 필수 값입니다.");
        }
        if (header.getInitialChannelCode() == null || header.getInitialChannelCode().isEmpty()) {
            throw new IllegalArgumentException("Initial Channel Code는 필수 값입니다.");
        }
        if (header.getChannelCode() == null || header.getChannelCode().isEmpty()) {
            throw new IllegalArgumentException("Channel Code는 필수 값입니다.");
        }
        if (header.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp는 필수 값입니다.");
        }
    }
}

