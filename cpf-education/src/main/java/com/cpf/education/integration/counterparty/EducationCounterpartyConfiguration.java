package com.cpf.education.integration.counterparty;
import com.cpf.education.integration.counterparty.application.EducationCounterpartyService;
import com.cpf.education.integration.counterparty.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Clock;

@Configuration(proxyBeanMethods=false)
/** EducationCounterpartyConfiguration 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationCounterpartyConfiguration {
    @Bean EducationCounterpartyStore educationCounterpartyStore(JdbcTemplate jdbc,ObjectMapper json){return new JdbcEducationCounterpartyStore(jdbc,json);}
    @Bean EducationCounterpartyService educationCounterpartyService(EducationCounterpartyStore store){return new EducationCounterpartyService(store,Clock.systemUTC());}
}
