package com.cpf.core.spi.fixedlength;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * charset별 엄격한 encode/decode를 제공하는 확장 SPI입니다.
 *
 * <p>silent replacement를 허용하지 않으며 구현체는 thread-safe여야 합니다.</p>
 */
public interface CpfFixedLengthEncoding {
    byte[] encode(String value, Charset charset) throws CharacterCodingException;

    String decode(byte[] value, Charset charset) throws CharacterCodingException;
}
