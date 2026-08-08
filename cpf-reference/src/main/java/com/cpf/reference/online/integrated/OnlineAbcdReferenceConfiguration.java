package com.cpf.reference.online.integrated;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** reference profile에서만 A/B/C/D consumer graph를 실제 Bean으로 연결한다. */
@Configuration
@Profile("cpf-reference-online-abcd")
public class OnlineAbcdReferenceConfiguration {
    @Bean OnlineAbcdReferenceFlow.Repository onlineAbcdRepository(){ return new OnlineAbcdReferenceFlow.InMemoryRepository(); }
    @Bean OnlineAbcdReferenceFlow.ScenarioRemote onlineAbcdRemote(){ return new OnlineAbcdReferenceFlow.ScenarioRemote(); }
    @Bean OnlineAbcdReferenceFlow.DomainC onlineAbcdDomainC(OnlineAbcdReferenceFlow.ScenarioRemote remote){ return new OnlineAbcdReferenceFlow.DomainC(remote); }
    @Bean OnlineAbcdReferenceFlow.DomainB onlineAbcdDomainB(OnlineAbcdReferenceFlow.DomainC c, OnlineAbcdReferenceFlow.Repository repo){ return new OnlineAbcdReferenceFlow.DomainB(c,repo); }
    @Bean OnlineAbcdReferenceFlow.DomainA onlineAbcdDomainA(OnlineAbcdReferenceFlow.DomainB b){ return new OnlineAbcdReferenceFlow.DomainA(b); }
    @Bean OnlineAbcdReferenceFlow.Controller onlineAbcdController(OnlineAbcdReferenceFlow.DomainA a){ return new OnlineAbcdReferenceFlow.Controller(a); }
    @Bean OnlineAbcdReferenceFlow.DomainD onlineAbcdDomainD(OnlineAbcdReferenceFlow.Repository repo, OnlineAbcdReferenceFlow.ScenarioRemote remote){ return new OnlineAbcdReferenceFlow.DomainD(repo,remote); }
}
