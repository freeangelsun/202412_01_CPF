package com.cpf.backoffice.web.shared.error;

public final class UnsupportedBackofficeOperationException extends RuntimeException {
    public UnsupportedBackofficeOperationException(String route) { super("Unsupported Backoffice public route: " + route); }
}
