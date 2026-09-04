package com.cpf.backoffice.web.shared.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Backoffice Web(MBW Channel Front) SPA 진입 화면을 제공합니다.
 *
 * <p>production bundle 은 {@code classpath:/static/mbw} 로 실린다. Spring Boot 는 root 의
 * welcome page 만 자동 해석하므로 하위 경로의 SPA 진입점은 명시적으로 전달해야 한다.
 * 이것이 없으면 사용자는 {@code /mbw/index.html} 을 직접 입력해야만 화면을 열 수 있다.</p>
 */
@Controller
public class BackofficeWebPageController {

    /** 슬래시 없는 진입 경로. */
    @GetMapping("/mbw")
    public String backofficeWebPage() {
        return "forward:/mbw/index.html";
    }

    /** 슬래시로 끝나는 진입 경로도 같은 화면을 제공한다. */
    @GetMapping("/mbw/")
    public String backofficeWebPageIndex() {
        return "forward:/mbw/index.html";
    }
}
