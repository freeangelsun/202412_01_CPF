package com.cpf.core.common.header;

import com.cpf.core.common.logging.TransactionIdGenerator;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * CPF 표준 수신 헤더 검증기입니다.
 */
public class CpfInboundHeaderValidator {
    private final int transactionIdSequenceDigits;

    public CpfInboundHeaderValidator(int transactionIdSequenceDigits) {
        this.transactionIdSequenceDigits = transactionIdSequenceDigits;
    }

    public List<String> missingRequiredHeaders(HttpServletRequest request) {
        List<String> missingHeaders = new ArrayList<>();
        require(request, CpfHeaderNames.TRANSACTION_ID, missingHeaders);
        require(request, CpfHeaderNames.REQUEST_TYPE, missingHeaders);
        require(request, CpfHeaderNames.ORIGINAL_CHANNEL_CODE, missingHeaders);
        require(request, CpfHeaderNames.CHANNEL_CODE, missingHeaders);
        return missingHeaders;
    }

    public boolean isValidTransactionId(String transactionId) {
        return TransactionIdGenerator.isValid(transactionId, transactionIdSequenceDigits);
    }

    public List<String> invalidExtensionHeaders(HttpServletRequest request) {
        List<String> invalidHeaders = new ArrayList<>();
        if (request == null) {
            return invalidHeaders;
        }
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (CpfExtensionHeaderPolicy.isExtensionHeader(headerName)
                    && !CpfExtensionHeaderPolicy.isAllowedExtensionHeader(headerName)) {
                invalidHeaders.add(headerName);
            }
        }
        return invalidHeaders;
    }

    private void require(HttpServletRequest request, String headerName, List<String> missingHeaders) {
        String value = request != null ? request.getHeader(headerName) : null;
        if (value == null || value.isBlank()) {
            missingHeaders.add(headerName);
        }
    }
}
