package com.cpf.web.error;

import com.cpf.core.api.error.CpfErrorCatalogResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfGlobalExceptionHandlerResponseStatusTest {
    @Test
    void preservesIntentionalHttpStatusAndUsesOnlyCanonicalSafeContent() {
        var beans = new StaticListableBeanFactory();
        var handler = new CpfGlobalExceptionHandler(
                beans.getBeanProvider(CpfErrorCatalogResolver.class));
        Map<HttpStatus, String> expectedCodes = Map.of(
                HttpStatus.BAD_REQUEST, "ECPF010001",
                HttpStatus.UNAUTHORIZED, "ECPF010005",
                HttpStatus.FORBIDDEN, "ECPF010006",
                HttpStatus.NOT_FOUND, "ECPF010002",
                HttpStatus.CONFLICT, "ECPF010007",
                HttpStatus.TOO_MANY_REQUESTS, "ECPF010008",
                HttpStatus.SERVICE_UNAVAILABLE, "ECPF990002");

        expectedCodes.forEach((status, code) -> {
            var response = handler.handleResponseStatus(
                    new ResponseStatusException(status, "RAW_INTERNAL_DETAIL"));
            assertThat(response.getStatusCode()).isEqualTo(status);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo(code);
            assertThat(response.getBody().message()).doesNotContain("RAW_INTERNAL_DETAIL");
        });
    }
}
