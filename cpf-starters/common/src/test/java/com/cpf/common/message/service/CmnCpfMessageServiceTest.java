package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfMessageRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CmnCpfMessageSourceTest {
    @Test
    void resolvesLocaleAndArguments() {
        CmnErrorCatalogStore store = new CmnErrorCatalogStore() {
            public com.cpf.common.message.api.CpfResponseCodeRecord response(String code) { return null; }
            public CpfMessageRecord message(String code, Locale locale) {
                if (!"ko".equals(locale.getLanguage())) return null;
                return row("안녕하세요 {name}", "Y");
            }
        };
        var service = new CmnCpfMessageSource(store, new CmnMessageArgumentPolicy(new ObjectMapper()));
        assertThat(service.getMessage("HELLO", Locale.ENGLISH, Map.of("name", "CPF"))).isEqualTo("안녕하세요 CPF");
    }

    @Test
    void disabledMessageFailsClosed() {
        CmnErrorCatalogStore store = new CmnErrorCatalogStore() {
            public com.cpf.common.message.api.CpfResponseCodeRecord response(String code) { return null; }
            public CpfMessageRecord message(String code, Locale locale) { return row("x", "N"); }
        };
        var service = new CmnCpfMessageSource(store, new CmnMessageArgumentPolicy(new ObjectMapper()));
        assertThatThrownBy(() -> service.getMessage("X", Locale.KOREAN, Map.of())).isInstanceOf(IllegalArgumentException.class);
    }

    private static CpfMessageRecord row(String text, String useYn) {
        return new CpfMessageRecord(1L,"HELLO","ko","TEMPLATE",text,text,1,null,null,"Y","N",
                Instant.now().minusSeconds(5),Instant.now().plusSeconds(60),1L,null,useYn,Instant.now());
    }
}
