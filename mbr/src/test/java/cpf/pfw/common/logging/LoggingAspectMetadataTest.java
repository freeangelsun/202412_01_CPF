package cpf.pfw.common.logging;

import cpf.pfw.common.exception.DefaultCpfMessageResolver;
import cpf.pfw.common.exception.CpfBusinessException;
import cpf.pfw.common.exception.CpfMessageResolver;
import cpf.pfw.common.exception.CpfResolvedResponse;
import cpf.pfw.common.exception.CpfResponseCodeResolver;
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
        when(resolver.resolve(eq("SACC000000"), eq(Locale.KOREAN), eq(Map.of()), isNull())).thenReturn(
                new CpfResolvedResponse(200, "SACC000000", "MACC000000", "success", "ACC success", null, null));
        LoggingAspect aspect = aspect(resolver);

        Object metadata = invoke(
                aspect,
                "resolveResponseMetadata",
                new Class<?>[] {Object.class, String.class},
                ResponseEntity.ok(new SampleBody("SACC000000", null, null)),
                "ACC");

        assertThat(value(metadata, "responseCode")).isEqualTo("SACC000000");
        assertThat(value(metadata, "messageCode")).isEqualTo("MACC000000");
        assertThat(value(metadata, "messageContent")).isEqualTo("success");
        assertThat(value(metadata, "internalMessage")).isEqualTo("ACC success");
    }

    @Test
    void resolveErrorMetadata_ShouldUseResolverForResponseCodeException() throws Exception {
        CpfResponseCodeResolver resolver = mock(CpfResponseCodeResolver.class);
        Map<String, Object> args = Map.of("0", "accountId");
        when(resolver.resolve(eq("EACC010001"), eq(Locale.KOREAN), eq(args), eq("invalid accountId"))).thenReturn(
                new CpfResolvedResponse(400, "EACC010001", "MACC010001", "accountId is invalid.", "ACC invalid accountId", "EACC010001", "invalid accountId"));
        LoggingAspect aspect = aspect(resolver);

        Object metadata = invoke(
                aspect,
                "resolveErrorMetadata",
                new Class<?>[] {Throwable.class, Locale.class},
                new CpfBusinessException("EACC010001", "invalid accountId", args),
                Locale.KOREAN);

        assertThat(value(metadata, "httpStatus")).isEqualTo(400);
        assertThat(value(metadata, "responseCode")).isEqualTo("EACC010001");
        assertThat(value(metadata, "messageCode")).isEqualTo("MACC010001");
        assertThat(value(metadata, "errorCode")).isEqualTo("EACC010001");
        assertThat(value(metadata, "externalMessage")).isEqualTo("accountId is invalid.");
        assertThat(value(metadata, "internalMessage")).isEqualTo("ACC invalid accountId");
    }

    @SuppressWarnings("unchecked")
    private LoggingAspect aspect(CpfResponseCodeResolver responseCodeResolver) {
        ObjectProvider<CpfMessageResolver> messageProvider = mock(ObjectProvider.class);
        ObjectProvider<CpfResponseCodeResolver> responseProvider = mock(ObjectProvider.class);
        when(messageProvider.getIfAvailable(any())).thenReturn(new DefaultCpfMessageResolver());
        when(responseProvider.getIfAvailable(any())).thenReturn(responseCodeResolver);
        return new LoggingAspect(
                mock(ApplicationEventPublisher.class),
                mock(Environment.class),
                new DynamicTransactionLogLevelService(),
                messageProvider,
                responseProvider);
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

