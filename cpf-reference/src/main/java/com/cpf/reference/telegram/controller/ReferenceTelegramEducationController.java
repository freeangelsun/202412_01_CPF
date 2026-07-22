package com.cpf.reference.telegram.controller;

import com.cpf.common.tlm.core.CmnTelegramParseResult;
import com.cpf.common.tlm.service.CmnTelegramService;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.reference.telegram.dto.ReferenceFixedLengthMemberTelegram;
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

@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 06. Fixed Length", description = "Fixed length telegram parse and write samples")
public class ReferenceTelegramEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final CmnTelegramService telegramService;

    public ReferenceTelegramEducationController(CmnTelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @PostMapping("/fixed-length/parse")
    @CpfOnlineTransaction(id = "OREFAA0060", name = "REFFixedLengthParse")
    @Operation(operationId = "refTelegramEducationParseFixedLengthTelegram", summary = "Fixed length parse sample", description = "Parses a fixed length string to DTO and map.")
    public ResponseEntity<Map<String, Object>> parseFixedLengthTelegram(@RequestParam(required = false) String telegram) {
        String sampleTelegram = TextUtils.hasText(telegram) ? telegram : telegramService.writeFromDto(defaultTelegramDto());
        CmnTelegramParseResult parseResult = telegramService.parseToMap(sampleTelegram, telegramService.schemaFromDto(ReferenceFixedLengthMemberTelegram.class));
        ReferenceFixedLengthMemberTelegram dto = telegramService.parseToDto(sampleTelegram, ReferenceFixedLengthMemberTelegram.class);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("telegram", sampleTelegram);
        response.put("dto", dto);
        response.put("parseResult", parseResult);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fixed-length/write")
    @CpfOnlineTransaction(id = "OREFAA0028", name = "REFFixedLengthWrite")
    @Operation(operationId = "refTelegramEducationWriteFixedLengthTelegram", summary = "Fixed length write sample", description = "Writes a DTO to a fixed length string.")
    public ResponseEntity<Map<String, Object>> writeFixedLengthTelegram(
            @RequestBody(required = false) ReferenceFixedLengthMemberTelegram request) {
        ReferenceFixedLengthMemberTelegram dto = request == null ? defaultTelegramDto() : request;
        String telegram = telegramService.writeFromDto(dto);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("dto", dto);
        response.put("telegram", telegram);
        response.put("length", telegram.length());
        response.put("parsedAgain", telegramService.parseToDto(telegram, ReferenceFixedLengthMemberTelegram.class));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fixed-length/marshal")
    @CpfOnlineTransaction(id = "OREFAA0033", name = "REFFixedLengthMarshal")
    @Operation(operationId = "refTelegramEducationMarshalFixedLengthTelegram", summary = "Fixed length marshal sample", description = "Alias for fixed-length write.")
    public ResponseEntity<Map<String, Object>> marshalFixedLengthTelegram(
            @RequestBody(required = false) ReferenceFixedLengthMemberTelegram request) {
        return writeFixedLengthTelegram(request);
    }

    @PostMapping("/fixed-length/unmarshal")
    @CpfOnlineTransaction(id = "OREFAA0034", name = "REFFixedLengthUnmarshal")
    @Operation(operationId = "refTelegramEducationUnmarshalFixedLengthTelegram", summary = "Fixed length unmarshal sample", description = "Alias for fixed-length parse.")
    public ResponseEntity<Map<String, Object>> unmarshalFixedLengthTelegram(@RequestParam(required = false) String telegram) {
        return parseFixedLengthTelegram(telegram);
    }

    private ReferenceFixedLengthMemberTelegram defaultTelegramDto() {
        return new ReferenceFixedLengthMemberTelegram("M000000001", "Sample User", new BigDecimal("12345.67"), true, LocalDate.now());
    }
}
