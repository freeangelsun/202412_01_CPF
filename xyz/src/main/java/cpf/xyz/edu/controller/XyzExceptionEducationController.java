package cpf.xyz.edu.controller;

import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfBusinessException;
import cpf.pfw.common.exception.CpfDynamicErrorCode;
import cpf.pfw.common.exception.CpfExternalServiceException;
import cpf.pfw.common.exception.CpfValidationException;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
public class XyzExceptionEducationController {

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @GetMapping("/exception")
    @CpfTransaction(id = "XYZ09EDU0003", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
    public ResponseEntity<String> throwStandardException(@RequestParam(defaultValue = "validation") String type) {
        String normalizedType = TextUtils.normalizeCode(type);
        if ("BUSINESS".equals(normalizedType)) {
            throw new CpfBusinessException("CPF 처리 기준입니다." + type);
        }
        if ("EXTERNAL".equals(normalizedType)) {
            throw new CpfExternalServiceException("CPF 처리 기준입니다.", new IllegalStateException("sample external failure"));
        }
        throw new CpfValidationException("CPF 처리 기준입니다." + type);
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @GetMapping("/exception/dynamic-message")
    @CpfTransaction(id = "XYZ09EDU0009", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
    public ResponseEntity<String> throwDynamicMessageException(
            @RequestParam(defaultValue = "CPF 처리 기준입니다.") String fieldName,
            @RequestParam(defaultValue = "M0001") String fieldValue) {

        String externalMessage = "CPF 처리 기준입니다.";
        String internalMessage = "CPF 처리 기준입니다.";
        Map<String, Object> messageArguments = Map.of(
                "fieldName", fieldName,
                "fieldValue", fieldValue);

        throw new CpfBusinessException(
                CpfDynamicErrorCode.duplicate("MXYZ090001", externalMessage, internalMessage),
                externalMessage,
                internalMessage,
                "CPF 처리 기준입니다.",
                messageArguments);
    }

    @GetMapping("/exception/response-code")
    @CpfTransaction(id = "XYZ09EDU0013", name = "XYZResponseCodeExceptionSample")
    @Operation(summary = "Response code exception sample", description = "Throws only a standard response code. PFW resolves pfw_response_code and pfw_message from cache and fills response/log metadata.")
    public ResponseEntity<String> throwResponseCodeException(
            @RequestParam(defaultValue = "EACC010001") String responseCode,
            @RequestParam(defaultValue = "accountId") String fieldName) {

        throw new CpfBusinessException(
                TextUtils.normalizeCode(responseCode),
                "XYZ response-code based exception sample. field=" + fieldName,
                Map.of("0", fieldName));
    }

    @GetMapping("/exception/indexed-message")
    @CpfTransaction(id = "XYZ09EDU0014", name = "XYZIndexedMessageExceptionSample")
    @Operation(summary = "Indexed message exception sample", description = "Demonstrates common indexed placeholders such as {0} and {1}.")
    public ResponseEntity<String> throwIndexedMessageException(
            @RequestParam(defaultValue = "memberNo") String fieldName,
            @RequestParam(defaultValue = "M0001") String fieldValue) {

        throw new CpfBusinessException(
                "EPFW010003",
                "XYZ indexed message exception sample. field=" + fieldName + ", value=" + fieldValue,
                Map.of("0", fieldName, "1", fieldValue));
    }
}

