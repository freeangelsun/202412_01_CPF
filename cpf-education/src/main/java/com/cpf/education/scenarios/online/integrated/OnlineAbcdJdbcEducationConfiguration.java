package com.cpf.education.scenarios.online.integrated;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/** 실제 DB가 준비된 검증 환경에서 education consumer graph를 Spring JDBC transaction으로 연결합니다. */
@Configuration
@Profile("cpf-education-online-abcd-jdbc")
public class OnlineAbcdJdbcEducationConfiguration {
    @Bean OnlineAbcdEducationFlow.Repository onlineAbcdJdbcRepository(
            JdbcTemplate jdbc,
            @Value("${cpf.education.online-abcd.table:cpf_ref_online_abcd}") String table) {
        return new OnlineAbcdSpringJdbcRepository(jdbc, table);
    }
    @Bean OnlineAbcdEducationFlow.ScenarioRemote onlineAbcdJdbcRemote(){ return new OnlineAbcdEducationFlow.ScenarioRemote(); }
    @Bean OnlineAbcdEducationFlow.DomainC onlineAbcdJdbcDomainC(OnlineAbcdEducationFlow.ScenarioRemote remote){ return new OnlineAbcdEducationFlow.DomainC(remote); }
    @Bean OnlineAbcdEducationFlow.DomainB onlineAbcdJdbcDomainB(OnlineAbcdEducationFlow.DomainC c, OnlineAbcdEducationFlow.Repository repo){ return new OnlineAbcdEducationFlow.DomainB(c,repo); }
    @Bean OnlineAbcdEducationFlow.DomainA onlineAbcdJdbcDomainA(OnlineAbcdEducationFlow.DomainB b){ return new OnlineAbcdEducationFlow.DomainA(b); }
    @Bean OnlineAbcdEducationFlow.Controller onlineAbcdJdbcController(OnlineAbcdEducationFlow.DomainA a){ return new OnlineAbcdEducationFlow.Controller(a); }
    @Bean OnlineAbcdEducationFlow.DomainD onlineAbcdJdbcDomainD(OnlineAbcdEducationFlow.Repository repo, OnlineAbcdEducationFlow.ScenarioRemote remote){ return new OnlineAbcdEducationFlow.DomainD(repo,remote); }
    @Bean OnlineAbcdSpringTransactionService onlineAbcdSpringTransactionService(
            OnlineAbcdEducationFlow.Controller controller,
            PlatformTransactionManager transactionManager) {
        return new OnlineAbcdSpringTransactionService(controller, transactionManager);
    }
}
