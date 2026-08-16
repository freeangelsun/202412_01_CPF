package com.cpf.education.data.transaction.tcc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.jdbc.core.JdbcTemplate;

@Configuration(proxyBeanMethods=false)
@ConditionalOnBean(JdbcTemplate.class)
/** EducationTccAutoConfiguration 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationTccAutoConfiguration {
 @Bean EducationTccReservationStore educationTccReservationStore(JdbcTemplate jdbc){return new JdbcEducationTccReservationStore(jdbc);}
 @Bean EducationDurableTccReservationParticipant educationDurableTccReservationParticipant(EducationTccReservationStore store){return new EducationDurableTccReservationParticipant(store);}
 @Bean EducationTccRecoveryService educationTccRecoveryService(EducationTccReservationStore store){return new EducationTccRecoveryService(store);}
}
