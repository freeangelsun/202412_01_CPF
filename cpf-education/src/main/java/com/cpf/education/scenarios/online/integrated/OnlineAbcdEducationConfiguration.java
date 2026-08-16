package com.cpf.education.scenarios.online.integrated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** education profile에서만 A/B/C/D consumer graph를 실제 Bean으로 연결한다. */
@Configuration
@Profile("cpf-education-online-abcd")
public class OnlineAbcdEducationConfiguration {
    @Bean OnlineAbcdEducationFlow.Repository onlineAbcdRepository(){ return new OnlineAbcdEducationFlow.InMemoryRepository(); }
    @Bean OnlineAbcdEducationFlow.ScenarioRemote onlineAbcdRemote(){ return new OnlineAbcdEducationFlow.ScenarioRemote(); }
    @Bean OnlineAbcdEducationFlow.DomainC onlineAbcdDomainC(OnlineAbcdEducationFlow.ScenarioRemote remote){ return new OnlineAbcdEducationFlow.DomainC(remote); }
    @Bean OnlineAbcdEducationFlow.DomainB onlineAbcdDomainB(OnlineAbcdEducationFlow.DomainC c, OnlineAbcdEducationFlow.Repository repo){ return new OnlineAbcdEducationFlow.DomainB(c,repo); }
    @Bean OnlineAbcdEducationFlow.DomainA onlineAbcdDomainA(OnlineAbcdEducationFlow.DomainB b){ return new OnlineAbcdEducationFlow.DomainA(b); }
    @Bean OnlineAbcdEducationFlow.Controller onlineAbcdController(OnlineAbcdEducationFlow.DomainA a){ return new OnlineAbcdEducationFlow.Controller(a); }
    @Bean OnlineAbcdEducationFlow.DomainD onlineAbcdDomainD(OnlineAbcdEducationFlow.Repository repo, OnlineAbcdEducationFlow.ScenarioRemote remote){ return new OnlineAbcdEducationFlow.DomainD(repo,remote); }
}
