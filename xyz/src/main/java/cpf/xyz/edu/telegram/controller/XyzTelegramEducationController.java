package cpf.xyz.edu.controller;

import cpf.cmn.tlm.core.CmnTelegramParseResult;
import cpf.cmn.tlm.service.CmnTelegramService;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.xyz.edu.dto.XyzFixedLengthMemberTelegram;
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
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 06. Fixed Length", description = "Fixed length telegram parse and write samples")
public class XyzTelegramEducationController {
    private final CmnTelegramService telegramService;

    public XyzTelegramEducationController(CmnTelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @PostMapping("/fixed-length/parse")
    @CpfOnlineTransaction(id = "OXYZAA0060", name = "XYZFixedLengthParse")
    @Operation(operationId = "xyzTelegramEducationParseFixedLengthTelegram", summary = "Fixed length parse sample", description = "Parses a fixed length string to DTO and map.")
    public ResponseEntity<Map<String, Object>> parseFixedLengthTelegram(@RequestParam(required = false) String telegram) {
        String sampleTelegram = TextUtils.hasText(telegram) ? telegram : telegramService.writeFromDto(defaultTelegramDto());
        CmnTelegramParseResult parseResult = telegramService.parseToMap(sampleTelegram, telegramService.schemaFromDto(XyzFixedLengthMemberTelegram.class));
        XyzFixedLengthMemberTelegram dto = telegramService.parseToDto(sampleTelegram, XyzFixedLengthMemberTelegram.class);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("telegram", sampleTelegram);
        response.put("dto", dto);
        response.put("parseResult", parseResult);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fixed-length/write")
    @CpfOnlineTransaction(id = "OXYZAA0028", name = "XYZFixedLengthWrite")
    @Operation(operationId = "xyzTelegramEducationWriteFixedLengthTelegram", summary = "Fixed length write sample", description = "Writes a DTO to a fixed length string.")
    public ResponseEntity<Map<String, Object>> writeFixedLengthTelegram(
            @RequestBody(required = false) XyzFixedLengthMemberTelegram request) {
        XyzFixedLengthMemberTelegram dto = request == null ? defaultTelegramDto() : request;
        String telegram = telegramService.writeFromDto(dto);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("dto", dto);
        response.put("telegram", telegram);
        response.put("length", telegram.length());
        response.put("parsedAgain", telegramService.parseToDto(telegram, XyzFixedLengthMemberTelegram.class));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fixed-length/marshal")
    @CpfOnlineTransaction(id = "OXYZAA0033", name = "XYZFixedLengthMarshal")
    @Operation(operationId = "xyzTelegramEducationMarshalFixedLengthTelegram", summary = "Fixed length marshal sample", description = "Alias for fixed-length write.")
    public ResponseEntity<Map<String, Object>> marshalFixedLengthTelegram(
            @RequestBody(required = false) XyzFixedLengthMemberTelegram request) {
        return writeFixedLengthTelegram(request);
    }

    @PostMapping("/fixed-length/unmarshal")
    @CpfOnlineTransaction(id = "OXYZAA0034", name = "XYZFixedLengthUnmarshal")
    @Operation(operationId = "xyzTelegramEducationUnmarshalFixedLengthTelegram", summary = "Fixed length unmarshal sample", description = "Alias for fixed-length parse.")
    public ResponseEntity<Map<String, Object>> unmarshalFixedLengthTelegram(@RequestParam(required = false) String telegram) {
        return parseFixedLengthTelegram(telegram);
    }

    private XyzFixedLengthMemberTelegram defaultTelegramDto() {
        return new XyzFixedLengthMemberTelegram("M000000001", "Sample User", new BigDecimal("12345.67"), true, LocalDate.now());
    }
}
