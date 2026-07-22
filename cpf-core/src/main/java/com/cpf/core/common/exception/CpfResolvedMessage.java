package com.cpf.core.common.exception;

/** 외부 응답용 메시지와 내부 운영용 메시지를 분리해 보관합니다. */
public record CpfResolvedMessage(String externalMessage, String internalMessage) {
}

