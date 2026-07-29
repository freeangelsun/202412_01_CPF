package com.cpf.local.runtime;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import java.util.*;

/** Generator로 생성된 업무 Domain package를 설정 기반으로 같은 JVM에 조립합니다. */
public final class CpfLocalDomainModuleRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
 private Environment environment;
 @Override
 public void setEnvironment(Environment environment){this.environment=environment;}
 @Override
 public void registerBeanDefinitions(AnnotationMetadata metadata,BeanDefinitionRegistry registry){
  if(!environment.getProperty("cpf.local.modules.domains.enabled",Boolean.class,false))return;
  String configured=environment.getProperty("cpf.local.modules.domains.base-packages","");
  List<String> packages=Arrays.stream(configured.split(",")).map(String::trim).filter(s->!s.isBlank()).distinct().toList();
  if(packages.isEmpty())throw new IllegalStateException("Domain module을 활성화했지만 base-packages가 없습니다.");
  ClassPathBeanDefinitionScanner scanner=new ClassPathBeanDefinitionScanner(registry);scanner.scan(packages.toArray(String[]::new));
 }
}
