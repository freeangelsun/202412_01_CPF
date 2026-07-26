package com.cpf.reference.utility.controller;

import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.transaction.CpfTransactionIdGenerator;
import com.cpf.core.api.util.CpfDates;
import com.cpf.core.api.util.CpfPages;
import com.cpf.core.api.util.CpfStrings;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.reference.common.base.ReferenceBaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * CPF Public Foundation API 사용법을 보여주는 EDU Controller입니다.
 *
 * <p>업무 개발자가 JDK 유틸/임의 Page DTO를 다시 만들지 않고 Core의 공개 API를
 * 실제 Controller에서 사용하는 최소 예제입니다.</p>
 */
@RestController
@RequestMapping({
        "/api/reference/foundation",
        "/reference/edu/foundation",
        "/ref/api/edu/foundation"
})
@Tag(name = "REF Reference 16. Foundation", description = "CPF Public Utility/Page/transactionId 사용 예제")
public class RefFoundationApiEduController extends ReferenceBaseController {
    private final CpfTransactionIdGenerator transactionIds;

    public RefFoundationApiEduController(CpfTransactionIdGenerator transactionIds) {
        this.transactionIds = transactionIds;
    }

    @GetMapping("/page")
    @CpfOnlineTransaction(id = "OREFUT0001", name = "REFFoundationPage")
    @Operation(operationId = "refFoundationPage", summary = "CpfPage/CpfPageRequest 사용 예제")
    public ResponseEntity<CpfPage<String>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        List<String> all = List.of("ALPHA","BETA","GAMMA");
        CpfPageRequest request = CpfPages.request(page, size);
        return ResponseEntity.ok(CpfPages.offsetPage(all, request));
    }

    @PostMapping("/normalize")
    @CpfOnlineTransaction(id = "OREFUT0002", name = "REFFoundationNormalize")
    @Operation(
            operationId = "refFoundationNormalize",
            summary = "CpfStrings/CpfDates/transactionId 사용 예제",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            examples = @ExampleObject(value = """
                                    {"name":"  CPF  ","baseDate":"2026-07-25","incomingTransactionId":""}
                                    """))))
    public ResponseEntity<Map<String,Object>> normalize(@RequestBody Map<String,String> body) {
        LocalDate baseDate = CpfDates.parse(body.get("baseDate"));
        return ResponseEntity.ok(Map.of(
                "name", CpfStrings.requireText(body.get("name"), "name"),
                "baseDate", CpfDates.formatBasic(baseDate),
                "transactionId", transactionIds.generateOrUse(body.get("incomingTransactionId"))));
    }
}
