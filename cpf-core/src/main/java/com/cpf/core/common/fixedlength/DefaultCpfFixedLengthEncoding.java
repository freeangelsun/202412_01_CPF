package com.cpf.core.common.fixedlength;

import com.cpf.core.spi.fixedlength.CpfFixedLengthEncoding;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;

/**
 * malformed/unmappable 문자를 replacement 없이 거부하는 기본 encoding 구현입니다.
 */
@Component
public final class DefaultCpfFixedLengthEncoding implements CpfFixedLengthEncoding {
    @Override
    public byte[] encode(String value, Charset charset) throws CharacterCodingException {
        if (value == null) {
            return new byte[0];
        }
        Charset effectiveCharset = requireCharset(charset);
        ByteBuffer encoded = effectiveCharset.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .encode(CharBuffer.wrap(value));
        byte[] bytes = new byte[encoded.remaining()];
        encoded.get(bytes);
        return bytes;
    }

    @Override
    public String decode(byte[] value, Charset charset) throws CharacterCodingException {
        if (value == null || value.length == 0) {
            return "";
        }
        Charset effectiveCharset = requireCharset(charset);
        return effectiveCharset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(value))
                .toString();
    }

    private Charset requireCharset(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("고정길이 전문 charset은 필수입니다.");
        }
        return charset;
    }
}
