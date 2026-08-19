package com.cpf.bzachannel.access.api;

import com.cpf.bzachannel.shared.api.ChannelRequestForwarder;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/bza/admin-users/**",
        "/api/bza/menus/**",
        "/api/bza/permissions/**",
        "/api/bza/roles/**",
        "/api/bza/settings/**"})
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
