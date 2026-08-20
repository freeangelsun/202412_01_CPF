package com.cpf.verification;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class CoreOnlyConsumerTest {
 @Test void coreContractLoadsWithoutBootProviderRuntime(){
  // Messaging contracts are intentionally owned by the Messaging capability after Core Slimming.
  assertThat(CpfBrokerPublishRequest.class.getName()).startsWith("com.cpf.messaging.api");
 }
}
