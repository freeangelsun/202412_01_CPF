package com.cpf.common.message.service;

import com.cpf.common.message.api.*;
import com.cpf.core.api.error.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CmnCpfErrorCatalogResolverTest {
    @Test void arbitraryBusinessCodeAndLocaleFallbackNeedNoCoreSourceChange() {
        FakeStore store=new FakeStore(); FakeSignals signals=new FakeSignals();
        store.responses.put("EBANK990777", response("EBANK990777","MBANK990777","BUSINESS","NEVER","SAFE_MESSAGE_ONLY","Y"));
        store.messages.put("MBANK990777|ko", message(1,"MBANK990777","ko","회원 {member} 요청을 처리할 수 없습니다.","Y",null,null));
        var resolver=new CmnCpfErrorCatalogResolver(store,signals,new CmnMessageArgumentPolicy(new ObjectMapper()));
        var result=resolver.resolve("EBANK990777", CpfErrorCode.BUSINESS_RULE_VIOLATION,Locale.ENGLISH,Map.of("member","A1"));
        assertThat(result.catalogHit()).isTrue();
        assertThat(result.responseCode()).isEqualTo("EBANK990777");
        assertThat(result.locale().getLanguage()).isEqualTo("ko");
        assertThat(result.externalMessage()).isEqualTo("회원 A1 요청을 처리할 수 없습니다.");
        assertThat(signals.events).isEmpty();
    }

    @Test void disabledExpiredAndInvalidParameterSchemaFailClosed() {
        Instant now=Instant.now(); FakeStore store=new FakeStore(); FakeSignals signals=new FakeSignals();
        store.responses.put("EDISABLED", response("EDISABLED","MDISABLED","BUSINESS","NEVER","SAFE_MESSAGE_ONLY","N"));
        store.responses.put("EEXPIRED", response("EEXPIRED","MEXPIRED","BUSINESS","NEVER","SAFE_MESSAGE_ONLY","Y"));
        store.messages.put("MEXPIRED|ko", message(2,"MEXPIRED","ko","expired","Y",now.minusSeconds(100),now.minusSeconds(10)));
        store.responses.put("ESCHEMA", response("ESCHEMA","MSCHEMA","BUSINESS","NEVER","SAFE_MESSAGE_ONLY","Y"));
        store.messages.put("MSCHEMA|ko", new CpfMessageRecord(3,"MSCHEMA","ko","FIXED","{member}","{member}",1,"","{bad-json","Y","Y",null,null,1,"","Y",now));
        var resolver=new CmnCpfErrorCatalogResolver(store,signals,new CmnMessageArgumentPolicy(new ObjectMapper()));
        assertThat(resolver.resolve("EDISABLED",CpfErrorCode.BUSINESS_RULE_VIOLATION,Locale.KOREAN,Map.of()).catalogHit()).isFalse();
        assertThat(resolver.resolve("EEXPIRED",CpfErrorCode.BUSINESS_RULE_VIOLATION,Locale.KOREAN,Map.of()).catalogHit()).isFalse();
        assertThat(resolver.resolve("ESCHEMA",CpfErrorCode.BUSINESS_RULE_VIOLATION,Locale.KOREAN,Map.of("member","1")).catalogHit()).isFalse();
        assertThat(signals.events).hasSize(3);
    }

    @Test void frameworkReservedCodeCanOverrideMessageButCannotWeakenSemantics() {
        FakeStore store=new FakeStore(); FakeSignals signals=new FakeSignals();
        store.responses.put("ECPF990000", response("ECPF990000","MCPF990000","BUSINESS","SAFE","SAFE_MESSAGE_ONLY","Y"));
        store.messages.put("MCPF990000|ko", message(4,"MCPF990000","ko","관리자 정의 안전 문구","Y",null,null));
        var resolver=new CmnCpfErrorCatalogResolver(store,signals,new CmnMessageArgumentPolicy(new ObjectMapper()));
        var result=resolver.resolve("ECPF990000",CpfErrorCode.INTERNAL_SERVER_ERROR,Locale.KOREAN,Map.of());
        assertThat(result.externalMessage()).isEqualTo("관리자 정의 안전 문구");
        assertThat(result.category()).isEqualTo(CpfErrorDefinition.Category.INTERNAL);
        assertThat(result.retryDisposition()).isEqualTo(CpfErrorDefinition.RetryDisposition.NEVER);
        assertThat(result.exposure()).isEqualTo(CpfErrorDefinition.Exposure.GENERIC_MESSAGE_ONLY);
    }

    private static CpfResponseCodeRecord response(String code,String message,String category,String retry,String exposure,String use) {
        return new CpfResponseCodeRecord(code,message,"FAIL","MBR","BUSINESS","1",category,retry,exposure,null,null,1,"",use,Instant.now());
    }
    private static CpfMessageRecord message(long id,String code,String locale,String external,String use,Instant from,Instant to) {
        return new CpfMessageRecord(id,code,locale,"FIXED",external,"internal",0,"",null,"N","N",from,to,1,"",use,Instant.now());
    }
    static final class FakeStore implements CmnErrorCatalogStore {
        final Map<String,CpfResponseCodeRecord> responses=new HashMap<>(); final Map<String,CpfMessageRecord> messages=new HashMap<>();
        public CpfResponseCodeRecord response(String code){return responses.get(code);} public CpfMessageRecord message(String code,Locale locale){return messages.get(code+"|"+locale.getLanguage());}
    }
    static final class FakeSignals implements CpfErrorCatalogSignalSink {
        final List<String> events=new ArrayList<>(); public void catalogFallback(String reason,String ref){events.add(reason+":"+ref);}
    }
}
