package com.cpf.messaging.reliability.api.jdbc;
import java.util.Map;import java.util.Set;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
class CpfMessageCompatibilityGuardTest {
 @Test void acceptsApprovedSchemaVersion(){new CpfMessageCompatibilityGuard(Map.of("payment",Set.of("1"))).verify(Map.of("cpf-schema-id","payment","cpf-schema-version","1"));}
 @Test void quarantinesMissingOrUnknownSchema(){var guard=new CpfMessageCompatibilityGuard(Map.of("payment",Set.of("1")));assertThatThrownBy(()->guard.verify(Map.of())).isInstanceOf(CpfMessageCompatibilityGuard.QuarantineException.class);assertThatThrownBy(()->guard.verify(Map.of("cpf-schema-id","payment","cpf-schema-version","2"))).isInstanceOf(CpfMessageCompatibilityGuard.QuarantineException.class);}
}
