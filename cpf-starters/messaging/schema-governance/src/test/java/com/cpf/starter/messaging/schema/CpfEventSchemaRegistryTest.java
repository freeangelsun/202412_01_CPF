package com.cpf.starter.messaging.schema;
import static org.assertj.core.api.Assertions.assertThat;
import com.cpf.core.api.reliability.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
class CpfEventSchemaRegistryTest {
 @Test void registersAndFindsLatestUsingCoreContract() {
   CpfEventSchemaRegistry registry = new CpfInMemoryEventSchemaRegistry();
   var schema = new CpfEventSchemaDescriptor("orders",1,"order-v1",CpfEventSchemaFormat.JSON_SCHEMA,
       "application/json","{\"type\":\"object\",\"required\":[\"id\"]}",CpfEventSchemaCompatibility.BACKWARD,Map.of());
   assertThat(registry.register(schema)).isEqualTo(schema);
   assertThat(registry.latest("orders")).contains(schema);
 }
}
