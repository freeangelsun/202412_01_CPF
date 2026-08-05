package com.cpf.starter.messaging.reliability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfBrokerHeaderPolicyTest {
    @Test
    void rejectsReservedHeaderAcrossProviderNamingVariants() {
        assertThatThrownBy(() -> request(Map.of("CPF.Message-Id", "override")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("reserved broker header");
    }

    @Test
    void rejectsHeadersThatCollapseToSameJmsProperty() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("trace-parent", "first");
        headers.put("trace.parent", "second");

        assertThatThrownBy(() -> request(headers))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("normalize to the same provider name");
    }

    @Test
    void returnsImmutableSnapshotIndependentFromCallerMap() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("traceparent", "v1");

        CpfBrokerPublishRequest validated = request(headers);
        headers.put("traceparent", "v2");
        headers.put("tenant", "T1");

        assertThat(validated.headers()).containsExactlyEntriesOf(Map.of("traceparent", "v1"));
        assertThatThrownBy(() -> validated.headers().put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsNonPortableAndWhitespaceNamesBeforeProviderCall() {
        assertThatThrownBy(() -> request(Map.of(" traceparent", "value")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("surrounding whitespace");
        assertThatThrownBy(() -> request(Map.of("trace parent", "value")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not portable");
    }


    @Test
    void rejectsExcessiveHeaderCountAndValueLength() {
        Map<String, String> excessive = new LinkedHashMap<>();
        for (int index = 0; index < 65; index++) {
            excessive.put("header-" + index, "value");
        }
        assertThatThrownBy(() -> request(excessive))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("header count");

        assertThatThrownBy(() -> request(Map.of("traceparent", "x".repeat(4097))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value exceeds");
    }

    @Test
    void rejectsControlCharactersBeforeProviderCall() {
        assertThatThrownBy(() -> request(Map.of("traceparent", "value\r\ninjected")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("control character");
        assertThatThrownBy(() -> request(Map.of("traceparent", "value\u0000suffix")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("control character");
    }

    private static CpfBrokerPublishRequest request(Map<String, String> headers) {
        return CpfBrokerHeaderPolicy.validatedRequest(new CpfBrokerPublishRequest(
                "msg-1", "topic-1", "key-1",
                "payload".getBytes(StandardCharsets.UTF_8), "text/plain",
                "tx-1", "seg-1", "producer", "consumer", "idem-1",
                headers, Map.of("tenant", "T1")));
    }
}
