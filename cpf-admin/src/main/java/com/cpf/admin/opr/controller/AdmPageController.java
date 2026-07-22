package com.cpf.admin.opr.controller;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** ADM 운영 콘솔의 SPA 진입 화면을 제공합니다. */
@Controller
public class AdmPageController extends com.cpf.admin.common.base.AdmBaseController {

    /** 정적 ADM 애플리케이션으로 요청을 전달합니다. */
    @GetMapping({"/adm", "/adm/"})
    @Operation(operationId = "admPageAdminPage", summary = "ADM 운영 콘솔 화면 조회")
    public String adminPage() {
        return "forward:/adm/index.html";
    }
}

