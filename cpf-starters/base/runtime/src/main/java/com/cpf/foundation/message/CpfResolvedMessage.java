package com.cpf.foundation.message;
/** 외부 응답용 안전 메시지와 내부 운영용 메시지를 분리한 불변 값입니다. */
public record CpfResolvedMessage(String externalMessage, String internalMessage) { }
