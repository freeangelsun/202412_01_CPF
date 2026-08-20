package com.cpf.backoffice.web.access.api;

import com.cpf.backoffice.web.shared.api.ChannelRequestForwarder;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/backoffice/admin-users/**",
        "/api/v1/backoffice/menus/**",
        "/api/v1/backoffice/permissions/**",
        "/api/v1/backoffice/roles/**",
        "/api/v1/backoffice/settings/**"})
public final class AccessChannelController {
    private final ChannelRequestForwarder requestForwarder;

    public AccessChannelController(ChannelRequestForwarder requestForwarder) {
        this.requestForwarder = requestForwarder;
    }

    @RequestMapping
    ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException, InterruptedException {
        return requestForwarder.forward(request);
    }
}
