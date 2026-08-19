package com.cpf.bzachannel.shared.error;

public final class UnsupportedBzaOperationException extends RuntimeException {
    public UnsupportedBzaOperationException(String route) { super("Unsupported BZA public route: " + route); }
}
