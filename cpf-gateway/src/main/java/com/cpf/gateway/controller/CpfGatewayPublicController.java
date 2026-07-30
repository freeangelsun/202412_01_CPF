package com.cpf.gateway.controller;

import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import com.cpf.gateway.service.CpfGatewayProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.security.cert.X509Certificate;

/** Versioned Binding의 Host·Path·Method 계약으로 매칭되는 외부 공개 Gateway 진입점입니다. */
@RestController
@RequestMapping("/gateway/public")
public final class CpfGatewayPublicController {
    private final CpfGatewayRouteSnapshot routes;
    private final CpfGatewayProxyService proxy;

    public CpfGatewayPublicController(CpfGatewayRouteSnapshot routes, CpfGatewayProxyService proxy) {
        this.routes = routes;
        this.proxy = proxy;
    }

    @RequestMapping(path = "/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<StreamingResponseBody> execute(
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) throws IOException {
        String publicPath = request.getRequestURI().substring("/gateway/public".length());
        CpfGatewayRoute route = routes.resolveRequest(
                headers.getFirst("X-CPF-Environment"),
                headers.getFirst(HttpHeaders.HOST),
                publicPath,
                request.getMethod(),
                headers.getFirst("X-CPF-API-Version"));
        return proxy.executeStreaming(
                route.standardExecutionId(), request.getMethod(), headers, request.getInputStream(),
                request.getContentLengthLong(), publicPath, request.getQueryString(), request.getRemoteAddr(), certificateSerial(request));
    }

    private static String certificateSerial(HttpServletRequest request) {
        Object value = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (value instanceof X509Certificate[] certificates && certificates.length > 0 && certificates[0] != null) {
            return certificates[0].getSerialNumber().toString(16).toUpperCase();
        }
        return "";
    }
}
