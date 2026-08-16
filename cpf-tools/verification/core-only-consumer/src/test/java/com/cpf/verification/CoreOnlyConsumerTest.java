package com.cpf.verification;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class CoreOnlyConsumerTest {
 @Test void coreContractLoadsWithoutBootProviderRuntime(){
  assertThat(CpfBrokerPublishRequest.class.getName()).startsWith("com.cpf.core.api");
 }
}
