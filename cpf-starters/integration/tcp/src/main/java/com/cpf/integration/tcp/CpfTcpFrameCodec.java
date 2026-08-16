package com.cpf.integration.tcp;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class CpfTcpFrameCodec {
    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int DLE = 0x10;
    private final CpfTcpProperties properties;

    public CpfTcpFrameCodec(CpfTcpProperties properties) {
        this.properties = properties;
    }

    /** Validates all deterministic frame constraints before any provider I/O starts. */
    public void validateForWrite(byte[] payload) throws IOException {
        if (payload == null) throw new IllegalArgumentException("payload is required");
        if (payload.length > properties.getMaxFrameBytes()) throw new IOException("frame exceeds limit");
        if (properties.getFrame() == CpfTcpProperties.Frame.FIXED
                && payload.length != properties.getFixedLength()) {
            throw new IOException("fixed frame length mismatch");
        }
    }

    public void write(OutputStream output, byte[] payload) throws IOException {
        validateForWrite(payload);
        switch (properties.getFrame()) {
            case FIXED -> output.write(payload);
            case LENGTH_HEADER -> {
                output.write(ByteBuffer.allocate(4).putInt(payload.length).array());
                output.write(payload);
            }
            case STX_ETX -> {
                output.write(STX);
                for (byte value : payload) {
                    int unsigned = value & 0xff;
                    if (unsigned == STX || unsigned == ETX || unsigned == DLE) output.write(DLE);
                    output.write(unsigned);
                }
                output.write(ETX);
            }
            case CRLF -> {
                output.write(payload);
                output.write('\r');
                output.write('\n');
            }
        }
        output.flush();
    }

    public byte[] read(InputStream input) throws IOException {
        return switch (properties.getFrame()) {
            case FIXED -> readExactly(input, properties.getFixedLength());
            case LENGTH_HEADER -> {
                int length = ByteBuffer.wrap(readExactly(input, 4)).getInt();
                validateLength(length);
                yield readExactly(input, length);
            }
            case STX_ETX -> readStxEtx(input);
            case CRLF -> readCrlf(input);
        };
    }

    private byte[] readStxEtx(InputStream input) throws IOException {
        int current;
        do {
            current = input.read();
            if (current < 0) throw new EOFException();
        } while (current != STX);

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        boolean escaped = false;
        while ((current = input.read()) >= 0) {
            if (escaped) {
                payload.write(current);
                escaped = false;
            } else if (current == DLE) {
                escaped = true;
            } else if (current == ETX) {
                return payload.toByteArray();
            } else if (current == STX) {
                throw new IOException("unexpected STX inside frame");
            } else {
                payload.write(current);
            }
            if (payload.size() > properties.getMaxFrameBytes()) throw new IOException("frame exceeds limit");
        }
        if (escaped) throw new EOFException("truncated DLE escape");
        throw new EOFException();
    }

    private byte[] readCrlf(InputStream input) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        int previous = -1;
        int current;
        while ((current = input.read()) >= 0) {
            if (previous == '\r' && current == '\n') {
                byte[] all = payload.toByteArray();
                return Arrays.copyOf(all, Math.max(0, all.length - 1));
            }
            payload.write(current);
            previous = current;
            if (payload.size() > properties.getMaxFrameBytes() + 2) throw new IOException("frame exceeds limit");
        }
        throw new EOFException();
    }

    private void validateLength(int length) throws IOException {
        if (length < 0 || length > properties.getMaxFrameBytes()) throw new IOException("invalid frame length: " + length);
    }

    private static byte[] readExactly(InputStream input, int length) throws IOException {
        byte[] bytes = input.readNBytes(length);
        if (bytes.length != length) throw new EOFException();
        return bytes;
    }
}
