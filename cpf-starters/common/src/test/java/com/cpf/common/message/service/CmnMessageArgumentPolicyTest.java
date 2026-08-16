package com.cpf.common.message.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CmnMessageArgumentPolicyTest {
    private final CmnMessageArgumentPolicy policy=new CmnMessageArgumentPolicy(new ObjectMapper());

    @Test void requiredSchemaEscapingAndSensitiveMaskingAreApplied() {
        String schema="""
                {"required":["name","token"],"properties":{"name":{"maxLength":8},"token":{"sensitive":true}}}
                """;
        var prepared=policy.prepare(Map.of("name","<민하>","token","secret-raw"),schema,true,true);
        assertThat(prepared.valid()).isTrue();
        assertThat(prepared.arguments().get("name")).isEqualTo("&lt;민하&gt;");
        assertThat(prepared.arguments().get("token")).isEqualTo("***");
    }

    @Test void missingRequiredAndInvalidSchemaFailClosed() {
        assertThat(policy.prepare(Map.of(),"{\"required\":[\"id\"]}",true,true).valid()).isFalse();
        assertThat(policy.prepare(Map.of("id","1"),"{bad",true,true).reason()).isEqualTo("INVALID_PARAMETER_SCHEMA");
    }
}
