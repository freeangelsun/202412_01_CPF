package com.cpf.education.integration.telegram.controller;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.integration.fixedlength.api.CpfFixedLengthDtoMapper;
import com.cpf.integration.fixedlength.api.CpfFixedLengthParseResult;
import com.cpf.integration.fixedlength.api.CpfFixedLengthWriteResult;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.education.integration.telegram.dto.EducationFixedLengthEducationTelegram;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CPF 고정길이 전문의 DTO 변환·파싱·직렬화 사용법을 제공하는 EDU 교육 Controller입니다.
 *
 * <p>동일 DTO 정의로 전문 문자열과 자료구조를 상호 변환하여,
 * 업무 Domain이 직접 위치 계산이나 반복적인 문자열 조립을 수행하지 않도록 하는 표준 예제입니다.</p>
 */
@RestController
@RequestMapping({"/api/education", "/education/edu"})
@Tag(name = "EDU Education 06. Fixed Length", description = "Fixed length telegram parse and write samples")
public class EducationTelegramEducationController extends com.cpf.education.base.EducationBaseController {
    private final CpfFixedLengthDtoMapper fixedLengthMapper;

    /** EducationTelegramEducationController 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationTelegramEducationController(CpfFixedLengthDtoMapper fixedLengthMapper) {
        this.fixedLengthMapper = fixedLengthMapper;
    }

    @PostMapping("/fixed-length/parse")
    @CpfOnlineTransaction(id = "OEDUAA0060", name = "EDUFixedLengthParse", ownerDomain="EDU")
    @Operation(operationId = "refTelegramEducationParseFixedLengthTelegram", summary = "Fixed length parse sample", description = "Parses a fixed length string to DTO and map.")
    /** parseFixedLengthTelegram 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> parseFixedLengthTelegram(@RequestParam(required = false) String telegram) {
        String sampleTelegram = CpfStrings.hasText(telegram)
                ? telegram
                : fixedLengthMapper.writeFromDto(defaultTelegramDto()).message();
        CpfFixedLengthParseResult parseResult = fixedLengthMapper.parseToMap(
                sampleTelegram,
                EducationFixedLengthEducationTelegram.class);
        EducationFixedLengthEducationTelegram dto = fixedLengthMapper.parseToDto(
                sampleTelegram,
                EducationFixedLengthEducationTelegram.class);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("telegram", sampleTelegram);
        response.put("dto", dto);
        response.put("parseResult", parseResult);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fixed-length/write")
    @CpfOnlineTransaction(id = "OEDUAA0028", name = "EDUFixedLengthWrite", ownerDomain="EDU")
    @Operation(operationId = "refTelegramEducationWriteFixedLengthTelegram", summary = "Fixed length write sample", description = "Writes a DTO to a fixed length string.")
    /** writeFixedLengthTelegram 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> writeFixedLengthTelegram(
            @RequestBody(required = false) EducationFixedLengthEducationTelegram request) {
        EducationFixedLengthEducationTelegram dto = request == null ? defaultTelegramDto() : request;
        CpfFixedLengthWriteResult writeResult = fixedLengthMapper.writeFromDto(dto);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("dto", dto);
        response.put("telegram", writeResult.message());
        response.put("length", writeResult.byteLength());
        response.put("parsedAgain", fixedLengthMapper.parseToDto(
                writeResult.message(),
                EducationFixedLengthEducationTelegram.class));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fixed-length/marshal")
    @CpfOnlineTransaction(id = "OEDUAA0033", name = "EDUFixedLengthMarshal", ownerDomain="EDU")
    @Operation(operationId = "refTelegramEducationMarshalFixedLengthTelegram", summary = "Fixed length marshal sample", description = "Alias for fixed-length write.")
    /** marshalFixedLengthTelegram 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> marshalFixedLengthTelegram(
            @RequestBody(required = false) EducationFixedLengthEducationTelegram request) {
        return writeFixedLengthTelegram(request);
    }

    @PostMapping("/fixed-length/unmarshal")
    @CpfOnlineTransaction(id = "OEDUAA0034", name = "EDUFixedLengthUnmarshal", ownerDomain="EDU")
    @Operation(operationId = "refTelegramEducationUnmarshalFixedLengthTelegram", summary = "Fixed length unmarshal sample", description = "Alias for fixed-length parse.")
    /** unmarshalFixedLengthTelegram 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> unmarshalFixedLengthTelegram(@RequestParam(required = false) String telegram) {
        return parseFixedLengthTelegram(telegram);
    }

    private EducationFixedLengthEducationTelegram defaultTelegramDto() {
        return new EducationFixedLengthEducationTelegram(
                "EDU0000001",
                "Education Item",
                new BigDecimal("12345.67"),
                true,
                LocalDate.now());
    }
}
