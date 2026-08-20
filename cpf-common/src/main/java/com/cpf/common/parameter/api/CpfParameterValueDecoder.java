package com.cpf.common.parameter.api;

/** Security capability가 encrypted Common parameter를 해석할 때 제공하는 Provider boundary입니다. */
@FunctionalInterface
public interface CpfParameterValueDecoder {
    String decode(String key, String storedValue);
}
