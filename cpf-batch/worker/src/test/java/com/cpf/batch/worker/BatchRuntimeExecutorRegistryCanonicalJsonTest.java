package com.cpf.batch.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

class BatchRuntimeExecutorRegistryCanonicalJsonTest {
    @Test
    void mapIsCanonicalJsonNotJavaMapString() {
        BatchRuntimeExecutorRegistry registry = registry();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("z", 1);
        body.put("a", Map.of("b", true));
        assertEquals("{\"a\":{\"b\":true},\"z\":1}", registry.jsonBody(body));
    }

    @Test
    void invalidJsonStringFailsClosed() {
        assertThrows(IllegalArgumentException.class, () -> registry().jsonBody("{a=1}"));
    }

    private static BatchRuntimeExecutorRegistry registry() {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        return new BatchRuntimeExecutorRegistry(
                beans.getBeanProvider(CpfServiceCaller.class),
                beans.getBeanProvider(CpfBrokerClient.class),
                new ObjectMapper());
    }
}
