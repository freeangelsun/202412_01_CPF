package com.cpf.reference.edu.counterparty.application;

import com.cpf.reference.edu.counterparty.model.ReferenceCounterpartyExchange;
import com.cpf.reference.edu.counterparty.persistence.ReferenceCounterpartyStore;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ReferenceCounterpartyServiceTest {
    @Test void samePayloadReplaysAndDifferentPayloadConflicts(){
        Store store=new Store();var service=new ReferenceCounterpartyService(store,Clock.fixed(Instant.parse("2026-08-01T00:00:00Z"),ZoneOffset.UTC));
        var request=Map.<String,Object>of("businessKey","B1","payload",Map.of("value",1));
        var first=service.exchange("external","09","EDU-DEV-09","idem-1","trace-1",request);
        var replay=service.exchange("external","09","EDU-DEV-09","idem-1","trace-1",request);
        var conflict=service.exchange("external","09","EDU-DEV-09","idem-1","trace-1",Map.of("businessKey","B1","payload",Map.of("value",2)));
        assertEquals(200,first.httpStatus());assertTrue(replay.replayed());assertEquals(409,conflict.httpStatus());
    }
    @Test void responseLossPersistsUnknownResultForReconciliation(){
        Store store=new Store();var service=new ReferenceCounterpartyService(store,Clock.systemUTC());
        var result=service.exchange("external","09","EDU-DEV-09","idem-loss","trace-loss",Map.of("businessKey","B2","payload",Map.of("simulateResponseLoss",true)));
        assertEquals(202,result.httpStatus());assertEquals("UNKNOWN_RESULT",result.body().get("state"));
        assertEquals("UNKNOWN_RESULT",store.values.values().iterator().next().state());
    }
    static final class Store implements ReferenceCounterpartyStore {
        final Map<String,ReferenceCounterpartyExchange> values=new HashMap<>();
        public Optional<ReferenceCounterpartyExchange> find(String r,String i){return Optional.ofNullable(values.get(r+"|"+i));}
        public boolean insert(ReferenceCounterpartyExchange e){return values.putIfAbsent(e.requirementId()+"|"+e.idempotencyKey(),e)==null;}
        public void update(ReferenceCounterpartyExchange e){values.put(e.requirementId()+"|"+e.idempotencyKey(),e);}
    }
}
