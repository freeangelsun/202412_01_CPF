package com.cpf.backoffice.web.shared.api;

import com.cpf.backoffice.web.shared.client.BusinessApiHttpClient;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public final class ChannelRequestForwarder {
    private final BusinessApiHttpClient businessApiHttpClient;

    public ChannelRequestForwarder(BusinessApiHttpClient businessApiHttpClient) {
        this.businessApiHttpClient = businessApiHttpClient;
    }

    public ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException, InterruptedException {
        return businessApiHttpClient.forward(request);
    }

    public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] bodyOverride, Map<String, String> bffHeaders)
            throws IOException, InterruptedException {
        return businessApiHttpClient.forward(request, bodyOverride, bffHeaders);
    }
}
