package com.cpf.local.runtime;

import org.springframework.context.*;
import org.springframework.core.env.Environment;
import java.util.*;
/** 개발 전용 통합 Runtime의 Production 오사용, 원격 Bind, 과도한 모듈 조립을 fail-closed로 차단합니다. */
public final class CpfLocalRuntimeSafetyGuard implements ApplicationContextInitializer<ConfigurableApplicationContext> {
 private static final Set<String> FORBIDDEN=Set.of("prod","production","prd","stg","stage");
 @Override
 public void initialize(ConfigurableApplicationContext context){Environment e=context.getEnvironment();
  if(!e.getProperty("cpf.local.runtime.enabled",Boolean.class,false))throw new IllegalStateException("cpf.local.runtime.enabled=true가 필요합니다.");
  List<String> profiles=Arrays.stream(e.getActiveProfiles()).map(s->s.toLowerCase(Locale.ROOT)).toList();String env=e.getProperty("cpf.environment","local").toLowerCase(Locale.ROOT);
  if(profiles.stream().noneMatch(p->p.equals("local")||p.startsWith("local-"))||profiles.stream().anyMatch(FORBIDDEN::contains)||FORBIDDEN.contains(env))throw new IllegalStateException("local 계열 Profile에서만 실행할 수 있습니다.");
  String address=e.getProperty("server.address","127.0.0.1").trim();if(!e.getProperty("cpf.local.runtime.allow-remote-bind",Boolean.class,false)&&!Set.of("127.0.0.1","localhost","::1")
          .contains(address))throw new IllegalStateException("원격 Bind는 기본 차단됩니다: "+address);
  int port=e.getProperty("server.port",Integer.class,8080);if(port<1||port>65535)throw new IllegalStateException("유효하지 않은 단일 Web Port입니다.");
  long enabled=java.util.stream.Stream.of("core","common","gateway","admin","biz-admin","domains").filter(n->e.getProperty("cpf.local.modules."+n+("domains".equals(n)?".enabled":""),Boolean.class,!Set
          .of("biz-admin","domains").contains(n))).count();
  int max=e.getProperty("cpf.local.runtime.max-enabled-modules",Integer.class,6);if(enabled>max)throw new IllegalStateException("활성 모듈 수가 안전 상한을 초과했습니다.");
  long minMb=e.getProperty("cpf.local.runtime.minimum-max-memory-mb",Long.class,512L);long maxMb=Runtime.getRuntime().maxMemory()/1024/1024;if(maxMb<minMb)throw new
          IllegalStateException("Local Runtime max heap이 부족합니다. required="+minMb+"MB, actual="+maxMb+"MB");
 }
}
