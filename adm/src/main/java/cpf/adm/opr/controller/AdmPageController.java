package cpf.adm.opr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ADM 愿由ъ옄 ?뺤쟻 ?붾㈃ 吏꾩엯 而⑦듃濡ㅻ윭?낅땲??
 */
@Controller
public class AdmPageController {

    /**
     * /adm ?묎렐 ???뺤쟻 愿由ъ옄 ?붾㈃?쇰줈 ?꾨떖?⑸땲??
     *
     * @return ?뺤쟻 HTML forward 寃쎈줈
     */
    @GetMapping({"/adm", "/adm/"})
    public String adminPage() {
        return "forward:/adm/index.html";
    }
}

