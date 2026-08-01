package com.cpf.reference.edu.counterparty;

import com.cpf.reference.edu.counterparty.application.ReferenceCounterpartyService;
import com.cpf.reference.edu.counterparty.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Clock;

@Configuration(proxyBeanMethods=false)
public class ReferenceCounterpartyConfiguration {
    @Bean ReferenceCounterpartyStore referenceCounterpartyStore(JdbcTemplate jdbc,ObjectMapper json){return new JdbcReferenceCounterpartyStore(jdbc,json);}
    @Bean ReferenceCounterpartyService referenceCounterpartyService(ReferenceCounterpartyStore store){return new ReferenceCounterpartyService(store,Clock.systemUTC());}
}
