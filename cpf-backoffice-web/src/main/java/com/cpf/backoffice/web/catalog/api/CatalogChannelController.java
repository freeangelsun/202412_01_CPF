package com.cpf.backoffice.web.catalog.api;

import com.cpf.backoffice.web.shared.api.ChannelRequestForwarder;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/backoffice/common-catalog/**",
        "/api/v1/backoffice/common/**"})
public final class CatalogChannelController {
    private final ChannelRequestForwarder requestForwarder;

    public CatalogChannelController(ChannelRequestForwarder requestForwarder) {
        this.requestForwarder = requestForwarder;
    }

    @RequestMapping
    ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException, InterruptedException {
        return requestForwarder.forward(request);
    }
}
