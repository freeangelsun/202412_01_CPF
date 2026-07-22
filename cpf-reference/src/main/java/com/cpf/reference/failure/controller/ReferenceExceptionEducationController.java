package com.cpf.reference.failure.controller;

import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.exception.CpfBusinessException;
import com.cpf.core.common.exception.CpfDynamicErrorCode;
import com.cpf.core.common.exception.CpfExternalServiceException;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 표준 예외와 응답코드 기반 오류 처리를 학습하는 EDU API입니다.
 */
@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 07. 예외 처리", description = "표준 예외, 응답코드, 다국어 메시지 사용 샘플")
public class ReferenceExceptionEducationController extends com.cpf.reference.common.base.ReferenceBaseController {

    @GetMapping("/exception")
    @CpfOnlineTransaction(id = "OREFAA0017", name = "REFStandardExceptionSample")
    @Operation(operationId = "refExceptionEducationThrowStandardException", summary = "표준 예외 샘플", description = "업무/외부/검증 오류별 표준 예외 변환 흐름을 확인합니다.")
    public ResponseEntity<String> throwStandardException(@RequestParam(defaultValue = "validation") String type) {
        String normalizedType = TextUtils.normalizeCode(type);
        if ("BUSINESS".equals(normalizedType)) {
            throw new CpfBusinessException("업무 예외 샘플입니다. type=" + type);
        }
        if ("EXTERNAL".equals(normalizedType)) {
            throw new CpfExternalServiceException("외부 연계 예외 샘플입니다.", new IllegalStateException("sample external failure"));
        }
        throw new CpfValidationException("검증 예외 샘플입니다. type=" + type);
    }

    @GetMapping("/exception/dynamic-message")
    @CpfOnlineTransaction(id = "OREFAA0024", name = "REFDynamicMessageExceptionSample")
    @Operation(operationId = "refExceptionEducationThrowDynamicMessageException", summary = "동적 메시지 예외 샘플", description = "응답코드와 메시지 인자를 함께 전달하는 예외 흐름을 확인합니다.")
    public ResponseEntity<String> throwDynamicMessageException(
            @RequestParam(defaultValue = "회원번호") String fieldName,
            @RequestParam(defaultValue = "M0001") String fieldValue) {

        String externalMessage = "중복된 값이 존재합니다.";
        String internalMessage = "중복 검증 예외 샘플입니다.";
        Map<String, Object> messageArguments = Map.of(
                "fieldName", fieldName,
                "fieldValue", fieldValue);

        throw new CpfBusinessException(
                CpfDynamicErrorCode.duplicate("MREF090001", externalMessage, internalMessage),
                externalMessage,
                internalMessage,
                "중복 값 검증",
                messageArguments);
    }

    @GetMapping("/exception/response-code")
    @CpfOnlineTransaction(id = "OREFAA0072", name = "REFResponseCodeExceptionSample")
    @Operation(operationId = "refExceptionEducationThrowResponseCodeException", summary = "응답코드 예외 샘플", description = "응답코드만 전달했을 때 CPF가 메시지와 HTTP 상태를 해석하는 흐름을 확인합니다.")
    public ResponseEntity<String> throwResponseCodeException(
            @RequestParam(defaultValue = "EREF010001") String responseCode,
            @RequestParam(defaultValue = "accountId") String fieldName) {

        throw new CpfBusinessException(
                TextUtils.normalizeCode(responseCode),
                "응답코드 기반 예외 샘플입니다. field=" + fieldName,
                Map.of("0", fieldName));
    }

    @GetMapping("/exception/indexed-message")
    @CpfOnlineTransaction(id = "OREFAA0073", name = "REFIndexedMessageExceptionSample")
    @Operation(operationId = "refExceptionEducationThrowIndexedMessageException", summary = "인덱스 메시지 예외 샘플", description = "{0}, {1} 같은 메시지 인자 치환 기준을 확인합니다.")
    public ResponseEntity<String> throwIndexedMessageException(
            @RequestParam(defaultValue = "memberNo") String fieldName,
            @RequestParam(defaultValue = "M0001") String fieldValue) {

        throw new CpfBusinessException(
                "ECPF010003",
                "인덱스 메시지 예외 샘플입니다. field=" + fieldName + ", value=" + fieldValue,
                Map.of("0", fieldName, "1", fieldValue));
    }
}
