package com.cpf.web.context;

import com.cpf.core.api.error.CpfFrameworkErrorCode;

/** Protocol-safe header validation failure raised before business Controller execution. */
public final class CpfHeaderValidationException extends IllegalArgumentException {
    private final CpfFrameworkErrorCode errorCode;
    private final String headerName;
    private final int httpStatus;
    private final String category;

    public CpfHeaderValidationException(CpfFrameworkErrorCode errorCode, String headerName, String message) {
        this(errorCode, headerName, message, 400, "HEADER_INVALID");
    }

    public CpfHeaderValidationException(CpfFrameworkErrorCode errorCode, String headerName, String message,
            int httpStatus, String category) {
        super(message);
        this.errorCode = errorCode;
        this.headerName = headerName;
        this.httpStatus = httpStatus;
        this.category = category == null || category.isBlank() ? "HEADER_INVALID" : category;
    }

    public CpfFrameworkErrorCode errorCode() { return errorCode; }
    public String headerName() { return headerName; }
    public int httpStatus() { return httpStatus; }
    public String category() { return category; }
}
