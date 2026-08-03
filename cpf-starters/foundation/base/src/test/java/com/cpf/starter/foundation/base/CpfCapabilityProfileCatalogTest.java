package com.cpf.starter.foundation.base;import com.fasterxml.jackson.databind.*;import java.io.*;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
class CpfCapabilityProfileCatalogTest {
 @Test void everyProfileResolvesAtLeastOneLeafStarter()throws Exception{try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream("cpf-capability-profiles.json")){assertThat(in).isNotNull();JsonNode root=new ObjectMapper().readTree(in);assertThat(root.path("profiles").size()).isGreaterThanOrEqualTo(10);root.path("profiles").forEach(p->assertThat(p.path("resolvedStarters").size()).isGreaterThan(0));}}
}
