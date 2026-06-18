package cpf.adm.opr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * CPF 기능 설명입니다.
 */
@Controller
public class AdmPageController {

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @GetMapping({"/adm", "/adm/"})
    public String adminPage() {
        return "forward:/adm/index.html";
    }
}

