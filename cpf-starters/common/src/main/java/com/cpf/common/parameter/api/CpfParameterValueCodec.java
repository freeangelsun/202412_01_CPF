package com.cpf.common.parameter.api;

/** Security capability가 encrypted Common parameter의 encode/decode를 제공하는 Provider 계약입니다. */
public interface CpfParameterValueCodec extends CpfParameterValueDecoder {
    String encode(String key, String plainValue);
}
