package com.cpf.backoffice.web.support.api;

import com.cpf.backoffice.web.shared.api.ChannelRequestForwarder;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/backoffice/attachments/**",
        "/api/v1/backoffice/audits/**",
        "/api/v1/backoffice/dashboard/**",
        "/api/v1/backoffice/download-audits/**",
        "/api/v1/backoffice/downloads/**",
        "/api/v1/backoffice/notifications/**",
        "/api/v1/backoffice/saved-searches/**"})
public final class SupportChannelController {
    private final ChannelRequestForwarder requestForwarder;

    public SupportChannelController(ChannelRequestForwarder requestForwarder) {
        this.requestForwarder = requestForwarder;
    }

    @RequestMapping
    ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException, InterruptedException {
        return requestForwarder.forward(request);
    }
}
