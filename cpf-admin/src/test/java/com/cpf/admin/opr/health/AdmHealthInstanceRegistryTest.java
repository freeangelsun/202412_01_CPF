package com.cpf.admin.opr.health;
import static org.assertj.core.api.Assertions.assertThat;
import com.cpf.platform.operations.health.api.*;
import java.time.*; import java.util.*;
import org.junit.jupiter.api.Test;
class AdmHealthInstanceRegistryTest {
 @Test void keepsSameInstanceIdSeparatedBySystem(){ Clock c=Clock.fixed(Instant.parse("2026-08-09T00:00:00Z"),ZoneOffset.UTC); var r=new AdmHealthInstanceRegistry(Duration.ofSeconds(90),c); r.report(h("a","i")); r.report(h("b","i")); assertThat(r.count(null,null,true)).isEqualTo(2); assertThat(r.find("a","i")).isPresent(); }
 private static CpfRuntimeHealth h(String s,String i){return new CpfRuntimeHealth(s,i,CpfHealthStatus.UP,CpfHealthStatus.UP,CpfHealthStatus.UP,false,false,"v","sha",Instant.EPOCH,1,List.of(),List.of(),List.of(),Map.of());}
}
