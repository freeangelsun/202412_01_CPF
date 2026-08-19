package com.cpf.bzachannel.support.api;

import com.cpf.bzachannel.shared.api.ChannelRequestForwarder;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/bza/attachments/**",
        "/api/bza/audits/**",
        "/api/bza/dashboard/**",
        "/api/bza/download-audits/**",
        "/api/bza/downloads/**",
        "/api/bza/notifications/**",
        "/api/bza/saved-searches/**"})
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
