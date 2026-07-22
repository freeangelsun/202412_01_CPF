package com.cpf.gateway.controller;

import com.cpf.core.common.header.CpfHeaderNames;
import com.cpf.gateway.service.CpfGatewayProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 표준 실행 ID header 또는 URI를 받는 단일 CPF Gateway 진입점입니다. */
@RestController
@RequestMapping("/cpf/execute")
@Tag(name = "CPF Gateway", description = "10자리 O 유형 표준 실행 ID 기반 선택 runtime")
public class CpfGatewayController {
    private final CpfGatewayProxyService proxyService;

    public CpfGatewayController(CpfGatewayProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @PostMapping
    @Operation(operationId = "executeCpfGatewayByHeader", summary = "표준 실행 ID header로 거래 실행")
    public ResponseEntity<byte[]> executeByHeader(
            @RequestHeader HttpHeaders headers,
            @RequestHeader(CpfHeaderNames.STANDARD_EXECUTION_ID) String executionId,
            @RequestBody(required = false) byte[] body) {
        return proxyService.execute(executionId, headers, body);
    }

    @PostMapping("/{executionId}")
    @Operation(operationId = "executeCpfGatewayByPath", summary = "표준 실행 ID URI로 거래 실행")
    public ResponseEntity<byte[]> executeByPath(
            @PathVariable String executionId,
            @RequestHeader HttpHeaders headers,
            @RequestParam(name = "validateHeader", defaultValue = "true") boolean validateHeader,
            @RequestBody(required = false) byte[] body) {
        String headerId = headers.getFirst(CpfHeaderNames.STANDARD_EXECUTION_ID);
        if (validateHeader && headerId != null && !headerId.equals(executionId)) {
            throw new IllegalArgumentException("URI와 header의 표준 실행 ID가 일치하지 않습니다.");
        }
        return proxyService.execute(executionId, headers, body);
    }
}
