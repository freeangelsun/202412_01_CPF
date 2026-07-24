package com.cpf.core.api.fixedlength;

import java.nio.charset.Charset;

/**
 * {@link CpfFixedLengthField} 어노테이션 DTO와 Core parser/writer를 연결합니다.
 */
public interface CpfFixedLengthDtoMapper {
    CpfFixedLengthLayout layoutFromDto(Class<?> dtoType);

    CpfFixedLengthLayout layoutFromDto(Class<?> dtoType, Charset charset);

    CpfFixedLengthWriteResult writeFromDto(Object dto);

    CpfFixedLengthWriteResult writeFromDto(Object dto, Charset charset);

    CpfFixedLengthParseResult parseToMap(String message, Class<?> dtoType);

    CpfFixedLengthParseResult parseToMap(String message, Class<?> dtoType, Charset charset);

    <T> T parseToDto(String message, Class<T> dtoType);

    <T> T parseToDto(String message, Class<T> dtoType, Charset charset);
}
