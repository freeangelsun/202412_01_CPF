package cpf.bza.backoffice.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** BZA 백오피스의 공식 화면 진입점을 제공합니다. */
@Controller
public class BzaPageController extends cpf.bza.common.base.BzaBaseController {
    @GetMapping({"/bza", "/bza/"})
    @Operation(operationId = "bzaPageIndex", summary = "BZA 화면 진입", hidden = true)
    public String index() {
        return "forward:/bza/index.html";
    }
}
