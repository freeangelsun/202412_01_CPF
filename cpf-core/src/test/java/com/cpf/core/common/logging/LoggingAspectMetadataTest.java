package com.cpf.core.common.logging;

import com.cpf.core.common.exception.DefaultCpfMessageResolver;
import com.cpf.core.common.exception.CpfBusinessException;
import com.cpf.core.common.exception.CpfMessageResolver;
import com.cpf.core.common.exception.CpfResolvedResponse;
import com.cpf.core.common.exception.CpfResponseCodeResolver;
import com.cpf.core.common.logging.policy.LogPolicyResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggingAspectMetadataTest {

    @Test
    void resolveResponseMetadata_ShouldUseResolverForSuccessResponse() throws Exception {
        CpfResponseCodeResolver resolver = mock(CpfResponseCodeResolver.class);
        when(resolver.resolve(eq("SREF000000"), eq(Locale.KOREAN), eq(Map.of()), isNull())).thenReturn(
                new CpfResolvedResponse(200, "SREF000000", "MREF000000", "success", "REF success", null, null));
        LoggingAspect aspect = aspect(resolver);

        Object metadata = invoke(
                aspect,
                "resolveResponseMetadata",
                new Class<?>[] {Object.class, String.class},
                ResponseEntity.ok(new SampleBody("SREF000000", null, null)),
                "REF");

        assertThat(value(metadata, "responseCode")).isEqualTo("SREF000000");
        assertThat(value(metadata, "messageCode")).isEqualTo("MREF000000");
        assertThat(value(metadata, "messageContent")).isEqualTo("success");
        assertThat(value(metadata, "internalMessage")).isEqualTo("REF success");
    }

    @Test
    void resolveErrorMetadata_ShouldUseResolverForResponseCodeException() throws Exception {
        CpfResponseCodeResolver resolver = mock(CpfResponseCodeResolver.class);
        Map<String, Object> args = Map.of("0", "accountId");
        when(resolver.resolve(eq("EREF010001"), eq(Locale.KOREAN), eq(args), eq("invalid accountId"))).thenReturn(
                new CpfResolvedResponse(400, "EREF010001", "MREF010001", "accountId is invalid.", "REF invalid accountId", "EREF010001", "invalid accountId"));
        LoggingAspect aspect = aspect(resolver);

        Object metadata = invoke(
                aspect,
                "resolveErrorMetadata",
                new Class<?>[] {Throwable.class, Locale.class},
                new CpfBusinessException("EREF010001", "invalid accountId", args),
                Locale.KOREAN);

        assertThat(value(metadata, "httpStatus")).isEqualTo(400);
        assertThat(value(metadata, "responseCode")).isEqualTo("EREF010001");
        assertThat(value(metadata, "messageCode")).isEqualTo("MREF010001");
        assertThat(value(metadata, "errorCode")).isEqualTo("EREF010001");
        assertThat(value(metadata, "externalMessage")).isEqualTo("accountId is invalid.");
        assertThat(value(metadata, "internalMessage")).isEqualTo("REF invalid accountId");
    }

    @SuppressWarnings("unchecked")
    private LoggingAspect aspect(CpfResponseCodeResolver responseCodeResolver) {
        ObjectProvider<CpfMessageResolver> messageProvider = mock(ObjectProvider.class);
        ObjectProvider<CpfResponseCodeResolver> responseProvider = mock(ObjectProvider.class);
        ObjectProvider<LogPolicyResolver> logPolicyProvider = mock(ObjectProvider.class);
        when(messageProvider.getIfAvailable(any())).thenReturn(new DefaultCpfMessageResolver());
        when(responseProvider.getIfAvailable(any())).thenReturn(responseCodeResolver);
        when(logPolicyProvider.getIfAvailable()).thenReturn(null);
        return new LoggingAspect(
                mock(ApplicationEventPublisher.class),
                mock(Environment.class),
                new DynamicTransactionLogLevelService(),
                messageProvider,
                responseProvider,
                logPolicyProvider);
    }

    private Object invoke(LoggingAspect aspect, String methodName, Class<?>[] parameterTypes, Object first, Object second) throws Exception {
        Method method = LoggingAspect.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(aspect, first, second);
    }

    private Object value(Object record, String accessorName) throws Exception {
        Method method = record.getClass().getDeclaredMethod(accessorName);
        method.setAccessible(true);
        return method.invoke(record);
    }

    public static class SampleBody {
        private final String statusCode;
        private final String messageCode;
        private final String messageContent;

        SampleBody(String statusCode, String messageCode, String messageContent) {
            this.statusCode = statusCode;
            this.messageCode = messageCode;
            this.messageContent = messageContent;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public String getMessageCode() {
            return messageCode;
        }

        public String getMessageContent() {
            return messageContent;
        }
    }
}

