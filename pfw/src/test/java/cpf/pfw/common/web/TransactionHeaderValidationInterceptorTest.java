package cpf.pfw.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.execution.CpfSharedApi;
import cpf.pfw.common.exception.CpfResponseCodeResolver;
import cpf.pfw.common.exception.DefaultCpfResponseCodeResolver;
import cpf.pfw.common.header.CpfHeaderNames;
import cpf.pfw.common.logging.CpfTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionHeaderValidationInterceptorTest {

    @Test
    void allowsOperationApiWithoutBusinessTransactionHeaders() throws Exception {
        TransactionHeaderValidationInterceptor interceptor = interceptor();

        boolean allowed = interceptor.preHandle(
                new MockHttpServletRequest("GET", "/adm/api/operations"),
                new MockHttpServletResponse(),
                handler("operationQuery"));

        assertThat(allowed).isTrue();
    }

    @Test
    void rejectsSensitiveExtensionHeaderEvenForOperationApi() throws Exception {
        TransactionHeaderValidationInterceptor interceptor = interceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/adm/api/operations");
        request.addHeader("X-Cpf-Ext-Api-Token", "should-not-be-propagated");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                request,
                response,
                handler("operationQuery"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString())
                .contains("\"statusCode\":\"EPFW900001\"")
                .doesNotContain("should-not-be-propagated");
    }

    @Test
    void rejectsDeclaredBusinessTransactionWhenRequiredHeadersAreMissing() throws Exception {
        TransactionHeaderValidationInterceptor interceptor = interceptor();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                new MockHttpServletRequest("POST", "/api/v1/sample"),
                response,
                handler("businessTransaction"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString())
                .contains("\"statusCode\":\"EPFW900001\"")
                .doesNotContain("errorDetailMessage");
    }

    @Test
    void allowsTrustedSharedApiCallFromDeclaredCaller() throws Exception {
        TransactionHeaderValidationInterceptor interceptor = interceptor();
        MockHttpServletRequest request = standardRequest("/internal/api/v1/sample");
        request.addHeader(CpfHeaderNames.STANDARD_EXECUTION_ID, "SPFWTS0001");
        request.addHeader(CpfHeaderNames.CALLER_SERVICE, "MBR");
        request.addHeader(CpfHeaderNames.CALLER_INSTANCE_ID, "MBR01");

        boolean allowed = interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                handler("sharedApi"));

        assertThat(allowed).isTrue();
    }

    @Test
    void rejectsSharedApiWithoutCompleteCallerIdentity() throws Exception {
        TransactionHeaderValidationInterceptor interceptor = interceptor();
        MockHttpServletRequest request = standardRequest("/internal/api/v1/sample");
        request.addHeader(CpfHeaderNames.STANDARD_EXECUTION_ID, "SPFWTS0001");
        request.addHeader(CpfHeaderNames.CALLER_SERVICE, "MBR");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, handler("sharedApi"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"statusCode\":\"EPFW900005\"");
    }

    @Test
    void rejectsSharedApiFromPublicGatewayIngress() throws Exception {
        TransactionHeaderValidationInterceptor interceptor = interceptor();
        MockHttpServletRequest request = standardRequest("/internal/api/v1/sample");
        request.addHeader(CpfHeaderNames.STANDARD_EXECUTION_ID, "SPFWTS0001");
        request.addHeader(CpfHeaderNames.CALLER_SERVICE, "MBR");
        request.addHeader(CpfHeaderNames.CALLER_INSTANCE_ID, "MBR01");
        request.addHeader(CpfHeaderNames.INGRESS_TYPE, "PFW_GATEWAY");
        request.addHeader(CpfHeaderNames.GATEWAY_INSTANCE_ID, "GW01");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, handler("sharedApi"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void rejectsSharedApiFromUndeclaredCpfService() throws Exception {
        TransactionHeaderValidationInterceptor interceptor = interceptor();
        MockHttpServletRequest request = standardRequest("/internal/api/v1/sample");
        request.addHeader(CpfHeaderNames.STANDARD_EXECUTION_ID, "SPFWTS0001");
        request.addHeader(CpfHeaderNames.CALLER_SERVICE, "XYZ");
        request.addHeader(CpfHeaderNames.CALLER_INSTANCE_ID, "XYZ01");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, handler("sharedApi"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @SuppressWarnings("unchecked")
    private TransactionHeaderValidationInterceptor interceptor() {
        ObjectProvider<CpfResponseCodeResolver> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable(any())).thenReturn(new DefaultCpfResponseCodeResolver());
        return new TransactionHeaderValidationInterceptor(
                new ObjectMapper().findAndRegisterModules(),
                provider,
                new MockEnvironment());
    }

    private HandlerMethod handler(String methodName) throws NoSuchMethodException {
        return new HandlerMethod(new SampleController(), SampleController.class.getDeclaredMethod(methodName));
    }

    private MockHttpServletRequest standardRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRemoteAddr("127.0.0.1");
        request.addHeader(CpfHeaderNames.TRANSACTION_ID, "20260615120000000MBRlocal010000001");
        request.addHeader(CpfHeaderNames.REQUEST_TYPE, "ONLINE");
        request.addHeader(CpfHeaderNames.ORIGINAL_CHANNEL_CODE, "MBR");
        request.addHeader(CpfHeaderNames.CHANNEL_CODE, "MBR");
        return request;
    }

    private static class SampleController {
        @SuppressWarnings("unused")
        public void operationQuery() {
        }

        @CpfTransaction(id = "PFW01API0002", name = "표준 헤더 검증 테스트")
        @SuppressWarnings("unused")
        public void businessTransaction() {
        }

        @CpfSharedApi(
                id = "SPFWTS0001",
                name = "내부 공유 API 검증 테스트",
                allowedCallers = "MBR")
        @SuppressWarnings("unused")
        public void sharedApi() {
        }
    }
}
