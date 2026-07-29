package com.cpf.core.api.error;

/** 외부 응답용 메시지와 내부 운영용 메시지를 분리한 공개 값 계약입니다. */
public record CpfResolvedMessage(String externalMessage, String internalMessage) {
}
