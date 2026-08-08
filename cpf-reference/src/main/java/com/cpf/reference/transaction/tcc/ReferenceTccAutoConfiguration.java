package com.cpf.reference.transaction.tcc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.jdbc.core.JdbcTemplate;

@Configuration(proxyBeanMethods=false)
@ConditionalOnBean(JdbcTemplate.class)
public class ReferenceTccAutoConfiguration {
 @Bean ReferenceTccReservationStore referenceTccReservationStore(JdbcTemplate jdbc){return new JdbcReferenceTccReservationStore(jdbc);}
 @Bean ReferenceDurableTccReservationParticipant referenceDurableTccReservationParticipant(ReferenceTccReservationStore store){return new ReferenceDurableTccReservationParticipant(store);}
 @Bean ReferenceTccRecoveryService referenceTccRecoveryService(ReferenceTccReservationStore store){return new ReferenceTccRecoveryService(store);}
}
