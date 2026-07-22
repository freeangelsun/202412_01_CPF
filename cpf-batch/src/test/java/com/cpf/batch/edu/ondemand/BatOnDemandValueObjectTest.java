package com.cpf.batch.edu.ondemand;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatOnDemandValueObjectTest {

    @Test
    void 결과JSON의null값을보존하면서외부변경은차단한다() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("exitDescription", null);

        BatOnDemandStatus status = new BatOnDemandStatus(
                "REQ-001", "BEDUOD0001", "KEY-001", "GLOBAL-001", "20260720",
                "COMPLETED", 1L, 2L, source, null, null, Instant.now(), Instant.now());

        source.put("exitDescription", "변경됨");
        assertThat(status.result()).containsEntry("exitDescription", null);
        assertThatThrownBy(() -> status.result().put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void 요청파라미터의null값을보존하면서외부변경은차단한다() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("optionalValue", null);

        BatOnDemandRequest request = new BatOnDemandRequest(
                "BEDUOD0001", "20260720", "KEY-002", "null 입력 검증", "tester", source);

        source.put("optionalValue", "변경됨");
        assertThat(request.parameters()).containsEntry("optionalValue", null);
        assertThatThrownBy(() -> request.parameters().put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
