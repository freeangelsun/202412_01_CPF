package com.cpf.messaging.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.cpf.messaging.schema.api.*;
import com.cpf.messaging.schema.runtime.CpfInMemoryEventSchemaRegistry;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfEventSchemaCompatibilityCorpusTest {
 @Test void jsonBackwardCompatibilityAndBreakingChangeGate() throws Exception {
   var registry=new CpfInMemoryEventSchemaRegistry();
   String v1=Files.readString(Path.of("src/test/resources/schema-corpus/json-v1.json"));
   String v2=Files.readString(Path.of("src/test/resources/schema-corpus/json-v2-compatible.json"));
   String breaking=Files.readString(Path.of("src/test/resources/schema-corpus/json-v2-breaking.json"));
   var one=new CpfEventSchemaDescriptor("orders",1,"orders-v1",CpfEventSchemaFormat.JSON_SCHEMA,"application/schema+json",v1,CpfEventSchemaCompatibility.BACKWARD,Map.of());
   var two=new CpfEventSchemaDescriptor("orders",2,"orders-v2",CpfEventSchemaFormat.JSON_SCHEMA,"application/schema+json",v2,CpfEventSchemaCompatibility.BACKWARD,Map.of());
   var bad=new CpfEventSchemaDescriptor("orders",2,"orders-bad",CpfEventSchemaFormat.JSON_SCHEMA,"application/schema+json",breaking,CpfEventSchemaCompatibility.BACKWARD,Map.of());
   registry.register(one); assertThat(registry.compatibility(one,two).compatible()).isTrue(); registry.register(two);
   assertThatThrownBy(()->registry.register(bad)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("breaking schema");
   registry.validate(one,"{\"id\":\"O-1\"}".getBytes(StandardCharsets.UTF_8));
   assertThatThrownBy(()->registry.validate(one,"{}".getBytes(StandardCharsets.UTF_8))).isInstanceOf(IllegalArgumentException.class);
 }

 @Test void officialFormatsAreExplicit() {
   assertThat(CpfEventSchemaFormat.values()).containsExactly(CpfEventSchemaFormat.JSON_SCHEMA,CpfEventSchemaFormat.AVRO,CpfEventSchemaFormat.PROTOBUF);
 }
}
