package com.cpf.admin.opr.controller;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** ADM 운영 콘솔의 SPA 진입 화면을 제공합니다. */
@Controller
public class AdmPageController extends com.cpf.admin.common.base.AdmBaseController {

    /**
     * 정적 ADM 애플리케이션으로 요청을 전달합니다.
     *
     * <p>경로마다 별도의 operationId 를 선언한다. 하나의 {@code @Operation} 으로 두 경로를 매핑하면
     * springdoc 이 중복을 피하려고 {@code admPageAdminPage_1} 처럼 접미사를 붙이는데, CPF 정본
     * operationId 규격은 {@code [A-Za-z][A-Za-z0-9]{5,}} 라 밑줄을 허용하지 않는다. 실제로 Runtime
     * OpenAPI 계약 검증이 {@code invalid operationId=admPageAdminPage_1} 로 실패했다.</p>
     */
    @GetMapping("/adm")
    @Operation(operationId = "admPageAdminPage", summary = "ADM 운영 콘솔 화면 조회")
    public String adminPage() {
        return "forward:/adm/index.html";
    }

    /** 슬래시로 끝나는 경로도 같은 SPA 진입 화면을 제공합니다. */
    @GetMapping("/adm/")
    @Operation(operationId = "admPageAdminPageIndex", summary = "ADM 운영 콘솔 화면 조회(슬래시 경로)")
    public String adminPageIndex() {
        return "forward:/adm/index.html";
    }
}

