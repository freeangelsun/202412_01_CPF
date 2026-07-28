package com.cpf.gateway.controller;

import com.cpf.core.api.header.CpfHeaderNames;
import com.cpf.gateway.service.CpfGatewayProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 표준 실행 ID header 또는 URI를 받는 단일 CPF Gateway 진입점입니다. */
@RestController
@RequestMapping("/cpf/execute")
@Tag(name = "CPF Gateway", description = "10자리 O 유형 표준 실행 ID 기반 선택 runtime")
public class CpfGatewayController {
    private final CpfGatewayProxyService proxyService;

    public CpfGatewayController(CpfGatewayProxyService proxyService) { this.proxyService = proxyService; }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    @Operation(operationId = "executeCpfGatewayByHeader", summary = "표준 실행 ID header로 거래 실행")
    public ResponseEntity<byte[]> executeByHeader(
            @RequestHeader HttpHeaders headers,
            @RequestHeader(CpfHeaderNames.STANDARD_EXECUTION_ID) String executionId,
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request) {
        return proxyService.execute(executionId, request.getMethod(), headers, body,
                request.getRemoteAddr(), certificateSerial(request));
    }

    @RequestMapping(path = "/{executionId}", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    @Operation(operationId = "executeCpfGatewayByPath", summary = "표준 실행 ID URI로 거래 실행")
    public ResponseEntity<byte[]> executeByPath(
            @PathVariable String executionId,
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request) {
        String headerId = headers.getFirst(CpfHeaderNames.STANDARD_EXECUTION_ID);
        if (headerId != null && !headerId.equals(executionId)) {
            throw new IllegalArgumentException("URI와 header의 표준 실행 ID가 일치하지 않습니다.");
        }
        return proxyService.execute(executionId, request.getMethod(), headers, body,
                request.getRemoteAddr(), certificateSerial(request));
    }
    @RequestMapping(method = RequestMethod.OPTIONS)
    @Operation(operationId = "preflightCpfGatewayByHeader", summary = "Gateway CORS preflight")
    public ResponseEntity<byte[]> preflightByHeader(
            @RequestHeader HttpHeaders headers,
            @RequestHeader(CpfHeaderNames.STANDARD_EXECUTION_ID) String executionId) {
        return proxyService.preflight(executionId, headers);
    }

    @RequestMapping(path = "/{executionId}", method = RequestMethod.OPTIONS)
    @Operation(operationId = "preflightCpfGatewayByPath", summary = "Gateway CORS preflight")
    public ResponseEntity<byte[]> preflightByPath(
            @PathVariable String executionId,
            @RequestHeader HttpHeaders headers) {
        String headerId = headers.getFirst(CpfHeaderNames.STANDARD_EXECUTION_ID);
        if (headerId != null && !headerId.equals(executionId)) {
            throw new IllegalArgumentException("URI와 header의 표준 실행 ID가 일치하지 않습니다.");
        }
        return proxyService.preflight(executionId, headers);
    }

    private String certificateSerial(HttpServletRequest request) {
        Object value = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (value instanceof X509Certificate[] certificates && certificates.length > 0 && certificates[0] != null) {
            return certificates[0].getSerialNumber().toString(16).toUpperCase();
        }
        return "";
    }

}
