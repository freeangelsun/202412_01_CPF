package com.cpf.bzachannel.shared.api;

import com.cpf.bzachannel.shared.client.BusinessApiHttpClient;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
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
}
