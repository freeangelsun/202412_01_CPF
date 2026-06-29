package cpf.pfw.common.header;

import cpf.pfw.common.logging.TransactionHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CpfHeaderStandardCoverageTest {

    @AfterEach
    void tearDown() {
        System.clearProperty(CpfTrustedProxyPolicy.TRUSTED_PROXIES_PROPERTY);
    }

    @Test
    void everyDeclaredHeaderNameHasHeaderSpec() {
        Set<String> specNames = CpfHeaderSpecs.all().stream()
                .map(CpfHeaderSpec::name)
                .collect(Collectors.toSet());

        assertThat(publicHeaderNameConstants())
                .allSatisfy(headerName -> assertThat(specNames).contains(headerName));
    }

    @Test
    void requiredAndSensitiveHeaderPoliciesAreExplicit() {
        assertThat(CpfHeaderSpecs.all().stream()
                .filter(spec -> spec.category() == CpfHeaderCategory.REQUIRED)
                .map(CpfHeaderSpec::name))
                .containsExactlyInAnyOrder(
                        CpfHeaderNames.TRANSACTION_ID,
                        CpfHeaderNames.REQUEST_TYPE,
                        CpfHeaderNames.ORIGINAL_CHANNEL_CODE,
                        CpfHeaderNames.CHANNEL_CODE);

        assertForbiddenRawHeader(CpfHeaderNames.AUTHORIZATION);
        assertForbiddenRawHeader(CpfHeaderNames.API_KEY);
        assertForbiddenRawHeader(CpfHeaderNames.REQUEST_SIGNATURE);
        assertForbiddenRawHeader(CpfHeaderNames.NONCE);
        assertForbiddenRawHeader(CpfHeaderNames.FORWARDED_FOR);
        assertForbiddenRawHeader(CpfHeaderNames.FORWARDED);

        assertThat(CpfHeaderSpecs.find(CpfHeaderNames.CLIENT_COUNTRY_CODE))
                .get()
                .extracting(CpfHeaderSpec::recommendedDbLength)
                .isEqualTo(2);
    }

    @Test
    void extractorResolvesAliasAndNetworkFallbacks() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.88");
        request.addHeader(CpfHeaderNames.IDEMPOTENCY_KEY_ALIAS, "idem-alias-001");
        request.addHeader(CpfHeaderNames.REAL_IP, "10.20.30.40");
        request.addHeader(CpfHeaderNames.CLIENT_COUNTRY_CODE, "KR");
        request.addHeader(CpfHeaderNames.CLIENT_TIMEZONE, "Asia/Seoul");
        request.addHeader(CpfHeaderNames.USER_AGENT, "cpf-test-agent");

        TransactionHeader header = CpfHeaderExtractor.toTransactionHeader(request, "local01");

        assertThat(header.getIdempotencyKey()).isEqualTo("idem-alias-001");
        assertThat(header.getClientIp()).isEqualTo("198.51.100.88");
        assertThat(header.getRealIp()).isEqualTo("10.20.30.40");
        assertThat(header.getClientCountryCode()).isEqualTo("KR");
        assertThat(header.getClientTimezone()).isEqualTo("Asia/Seoul");
        assertThat(header.getUserAgent()).isEqualTo("cpf-test-agent");
        assertThat(header.getWasId()).isEqualTo("local01");
    }

    @Test
    void extractorIgnoresForwardedHeadersFromUntrustedRemote() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.88");
        request.addHeader(CpfHeaderNames.CLIENT_IP, "192.0.2.10");
        request.addHeader(CpfHeaderNames.FORWARDED_FOR, "203.0.113.10, 10.0.0.1");
        request.addHeader(CpfHeaderNames.FORWARDED, "for=203.0.113.20;proto=https");
        request.addHeader(CpfHeaderNames.REAL_IP, "203.0.113.30");

        TransactionHeader header = CpfHeaderExtractor.toTransactionHeader(request, "local01");

        assertThat(header.getClientIp()).isEqualTo("198.51.100.88");
        assertThat(header.getForwardedFor()).isEqualTo("203.0.113.10, 10.0.0.1");
        assertThat(header.getForwarded()).isEqualTo("for=203.0.113.20;proto=https");
        assertThat(header.getRealIp()).isEqualTo("203.0.113.30");
    }

    @Test
    void extractorUsesForwardedForFromTrustedProxy() {
        System.setProperty(CpfTrustedProxyPolicy.TRUSTED_PROXIES_PROPERTY, "10.0.0.1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader(CpfHeaderNames.FORWARDED_FOR, "203.0.113.10, 10.0.0.1");
        request.addHeader(CpfHeaderNames.REAL_IP, "203.0.113.30");

        TransactionHeader header = CpfHeaderExtractor.toTransactionHeader(request, "local01");

        assertThat(header.getClientIp()).isEqualTo("203.0.113.10");
    }

    @Test
    void extractorUsesForwardedHeaderFromTrustedProxyWhenForwardedForIsAbsent() {
        System.setProperty(CpfTrustedProxyPolicy.TRUSTED_PROXIES_PROPERTY, "10.0.0.0/24");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.15");
        request.addHeader(CpfHeaderNames.FORWARDED, "for=\"203.0.113.44\";proto=https");

        TransactionHeader header = CpfHeaderExtractor.toTransactionHeader(request, "local01");

        assertThat(header.getClientIp()).isEqualTo("203.0.113.44");
    }

    @Test
    void extractorIgnoresUnknownBlankAndInvalidForwardedValues() {
        System.setProperty(CpfTrustedProxyPolicy.TRUSTED_PROXIES_PROPERTY, "10.0.0.1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader(CpfHeaderNames.FORWARDED_FOR, "unknown, , not-an-ip");
        request.addHeader(CpfHeaderNames.FORWARDED, "for=unknown;proto=https, for=bad-host-name");
        request.addHeader(CpfHeaderNames.REAL_IP, "invalid-real-ip");

        TransactionHeader header = CpfHeaderExtractor.toTransactionHeader(request, "local01");

        assertThat(header.getClientIp()).isEqualTo("10.0.0.1");
    }

    @Test
    void extractorFallsBackToTrustedRealIpWhenForwardedHeadersAreInvalid() {
        System.setProperty(CpfTrustedProxyPolicy.TRUSTED_PROXIES_PROPERTY, "10.0.0.1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader(CpfHeaderNames.FORWARDED_FOR, "unknown");
        request.addHeader(CpfHeaderNames.FORWARDED, "for=invalid-host");
        request.addHeader(CpfHeaderNames.REAL_IP, "203.0.113.77");

        TransactionHeader header = CpfHeaderExtractor.toTransactionHeader(request, "local01");

        assertThat(header.getClientIp()).isEqualTo("203.0.113.77");
    }

    @Test
    void inboundHeaderExtractionMasksSensitiveHeaderValues() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CpfHeaderNames.AUTHORIZATION, "Bearer very-sensitive-token");
        request.addHeader(CpfHeaderNames.API_KEY, "api-key-value");
        request.addHeader(CpfHeaderNames.CHANNEL_CODE, "MBR");

        Map<String, String> inboundHeaders = CpfHeaderExtractor.extractInboundHeaders(request);

        assertThat(inboundHeaders)
                .containsEntry(CpfHeaderNames.AUTHORIZATION, "****")
                .containsEntry(CpfHeaderNames.API_KEY, "****")
                .containsEntry(CpfHeaderNames.CHANNEL_CODE, "MBR");
    }

    @Test
    void outboundPolicyPropagatesBusinessHeadersButNeverSecuritySecrets() {
        Map<String, Boolean> expected = Map.of(
                CpfHeaderNames.TRANSACTION_ID, true,
                CpfHeaderNames.PARENT_TRANSACTION_ID, true,
                CpfHeaderNames.TRACE_ID, true,
                CpfHeaderNames.SPAN_ID, false,
                CpfHeaderNames.CLIENT_VERSION, true,
                CpfHeaderNames.CALLER_SERVICE, true,
                CpfHeaderNames.TIMEZONE, true,
                CpfHeaderNames.USER_AGENT, false,
                CpfHeaderNames.AUTHORIZATION, false,
                CpfHeaderNames.API_KEY, false);

        expected.forEach((headerName, shouldPropagate) ->
                assertThat(CpfHeaderSpecs.shouldPropagate(headerName))
                        .as(headerName)
                        .isEqualTo(shouldPropagate));
    }

    private static void assertForbiddenRawHeader(String headerName) {
        assertThat(CpfHeaderSpecs.find(headerName))
                .get()
                .satisfies(spec -> {
                    assertThat(spec.category()).isEqualTo(CpfHeaderCategory.FORBIDDEN_TO_LOG_RAW);
                    assertThat(spec.propagation()).isFalse();
                    assertThat(spec.loggable()).isFalse();
                    assertThat(spec.masked()).isTrue();
                });
    }

    private static List<String> publicHeaderNameConstants() {
        return Arrays.stream(CpfHeaderNames.class.getDeclaredFields())
                .filter(field -> field.getType().equals(String.class))
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .map(CpfHeaderStandardCoverageTest::readStringConstant)
                .toList();
    }

    private static String readStringConstant(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("헤더 상수 값을 읽을 수 없습니다. field=" + field.getName(), e);
        }
    }
}
