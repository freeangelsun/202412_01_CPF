package com.cpf.gateway.config;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
class CpfGatewaySafetyPropertiesTest {
 @Test void defaultsAreSafe(){var p=new CpfGatewaySafetyProperties();assertDoesNotThrow(p::validate);assertFalse(p.isRawBodyCaptureAllowed());assertEquals("FAIL_CLOSED",p.getBootstrapMode());}
 @Test void rejectsRetryAndTimeoutExpansion(){var p=new CpfGatewaySafetyProperties();p.setRetryCountCap(11);assertThrows(IllegalStateException.class,p::validate);p.setRetryCountCap(1);p.setOverallTimeoutCap(Duration.ofSeconds(1));assertThrows(IllegalStateException.class,p::validate);}
}
