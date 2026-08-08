package com.cpf.reference.online.integrated;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/** 실제 DB가 준비된 검증 환경에서 reference consumer graph를 Spring JDBC transaction으로 연결합니다. */
@Configuration
@Profile("cpf-reference-online-abcd-jdbc")
public class OnlineAbcdJdbcReferenceConfiguration {
    @Bean OnlineAbcdReferenceFlow.Repository onlineAbcdJdbcRepository(
            JdbcTemplate jdbc,
            @Value("${cpf.reference.online-abcd.table:cpf_ref_online_abcd}") String table) {
        return new OnlineAbcdSpringJdbcRepository(jdbc, table);
    }
    @Bean OnlineAbcdReferenceFlow.ScenarioRemote onlineAbcdJdbcRemote(){ return new OnlineAbcdReferenceFlow.ScenarioRemote(); }
    @Bean OnlineAbcdReferenceFlow.DomainC onlineAbcdJdbcDomainC(OnlineAbcdReferenceFlow.ScenarioRemote remote){ return new OnlineAbcdReferenceFlow.DomainC(remote); }
    @Bean OnlineAbcdReferenceFlow.DomainB onlineAbcdJdbcDomainB(OnlineAbcdReferenceFlow.DomainC c, OnlineAbcdReferenceFlow.Repository repo){ return new OnlineAbcdReferenceFlow.DomainB(c,repo); }
    @Bean OnlineAbcdReferenceFlow.DomainA onlineAbcdJdbcDomainA(OnlineAbcdReferenceFlow.DomainB b){ return new OnlineAbcdReferenceFlow.DomainA(b); }
    @Bean OnlineAbcdReferenceFlow.Controller onlineAbcdJdbcController(OnlineAbcdReferenceFlow.DomainA a){ return new OnlineAbcdReferenceFlow.Controller(a); }
    @Bean OnlineAbcdReferenceFlow.DomainD onlineAbcdJdbcDomainD(OnlineAbcdReferenceFlow.Repository repo, OnlineAbcdReferenceFlow.ScenarioRemote remote){ return new OnlineAbcdReferenceFlow.DomainD(repo,remote); }
    @Bean OnlineAbcdSpringTransactionService onlineAbcdSpringTransactionService(
            OnlineAbcdReferenceFlow.Controller controller,
            PlatformTransactionManager transactionManager) {
        return new OnlineAbcdSpringTransactionService(controller, transactionManager);
    }
}
