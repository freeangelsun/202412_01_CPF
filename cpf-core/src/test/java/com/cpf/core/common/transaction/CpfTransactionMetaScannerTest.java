package com.cpf.core.common.transaction;

import com.cpf.core.api.execution.CpfOnlineTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CpfTransactionMetaScannerTest {

    @Test
    void detectsPublicAndLegacyAnnotationsAndPrefersPublicMetadata() throws Exception {
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlers = new LinkedHashMap<>();
        handlers.put(mapping("/public"), handlerMethod("publicTransaction"));
        handlers.put(mapping("/legacy"), handlerMethod("legacyTransaction"));
        handlers.put(mapping("/dual"), handlerMethod("dualTransaction"));
        when(handlerMapping.getHandlerMethods()).thenReturn(handlers);

        CpfTransactionMetaScanner scanner = new CpfTransactionMetaScanner(
                handlerMapping,
                mock(CpfTransactionMetaRepository.class));

        List<CpfTransactionMeta> detected = scanner.detect();

        assertThat(detected)
                .extracting(
                        CpfTransactionMeta::transactionId,
                        CpfTransactionMeta::transactionName,
                        CpfTransactionMeta::httpMethod,
                        CpfTransactionMeta::apiPath)
                .containsExactlyInAnyOrder(
                        tuple("OPUB-TST-0101", "Public 거래", "GET", "/public"),
                        tuple("OLEG-TST-0101", "Legacy 거래", "GET", "/legacy"),
                        tuple("OPUB-TST-0102", "Public 우선 거래", "GET", "/dual"));
    }

    private RequestMappingInfo mapping(String path) {
        return RequestMappingInfo.paths(path).methods(RequestMethod.GET).build();
    }

    private HandlerMethod handlerMethod(String methodName) throws Exception {
        Method method = SampleController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new SampleController(), method);
    }

    private static class SampleController {
        @CpfOnlineTransaction(id = "OPUB-TST-0101", name = "Public 거래")
        public void publicTransaction() {
        }

        @com.cpf.core.common.execution.CpfOnlineTransaction(
                id = "OLEG-TST-0101",
                name = "Legacy 거래")
        public void legacyTransaction() {
        }

        @CpfOnlineTransaction(id = "OPUB-TST-0102", name = "Public 우선 거래")
        @com.cpf.core.common.execution.CpfOnlineTransaction(
                id = "OLEG-TST-0102",
                name = "Legacy 후순위 거래")
        public void dualTransaction() {
        }
    }
}
