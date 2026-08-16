package com.cpf.education.common.utility.controller;
import com.cpf.foundation.api.page.CpfPage;
import com.cpf.foundation.api.page.CpfPageRequest;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.CpfDates;
import com.cpf.foundation.api.page.CpfPages;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.education.base.EducationBaseController;
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
        "/api/education/foundation",
        "/education/edu/foundation",
        "/ref/api/edu/foundation"
})
@Tag(name = "EDU Education 16. Foundation", description = "CPF Public Utility/Page/transactionId 사용 예제")
public class EduFoundationApiEduController extends EducationBaseController {
    private final CpfTransactionIdGenerator transactionIds;

    /** EduFoundationApiEduController 작업을 CPF 표준 계약에 따라 수행한다. */
    public EduFoundationApiEduController(CpfTransactionIdGenerator transactionIds) {
        this.transactionIds = transactionIds;
    }

    @GetMapping("/page")
    @CpfOnlineTransaction(id = "OEDUUT0001", name = "EDUFoundationPage", ownerDomain="EDU")
    @Operation(operationId = "refFoundationPage", summary = "CpfPage/CpfPageRequest 사용 예제")
    /** page 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CpfPage<String>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        List<String> all = List.of("ALPHA","BETA","GAMMA");
        CpfPageRequest request = CpfPages.request(page, size);
        return ResponseEntity.ok(CpfPages.offsetPage(all, request));
    }

    @PostMapping("/normalize")
    @CpfOnlineTransaction(id = "OEDUUT0002", name = "EDUFoundationNormalize", ownerDomain="EDU")
    @Operation(
            operationId = "refFoundationNormalize",
            summary = "CpfStrings/CpfDates/transactionId 사용 예제",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            examples = @ExampleObject(value = """
                                    {"name":"  CPF  ","baseDate":"2026-07-25","incomingTransactionId":""}
                                    """))))
    /** normalize 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> normalize(@RequestBody Map<String,String> body) {
        LocalDate baseDate = CpfDates.parse(body.get("baseDate"));
        return ResponseEntity.ok(Map.of(
                "name", CpfStrings.requireText(body.get("name"), "name"),
                "baseDate", CpfDates.formatBasic(baseDate),
                "transactionId", transactionIds.generateOrUse(body.get("incomingTransactionId"))));
    }
}
