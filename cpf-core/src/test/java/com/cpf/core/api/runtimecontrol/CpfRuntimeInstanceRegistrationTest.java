package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfRuntimeInstanceRegistrationTest {
    @Test void defaultsNonPositiveLeaseButRejectsExplicitOutOfRange(){
        CpfRuntimeInstanceRegistration defaulted=registration(0);
        assertEquals(60,defaulted.leaseSeconds());
        assertThrows(IllegalArgumentException.class,()->registration(9));
        assertThrows(IllegalArgumentException.class,()->registration(3601));
    }

    private CpfRuntimeInstanceRegistration registration(int leaseSeconds){
        return new CpfRuntimeInstanceRegistration("instance","service","endpoint","dev","zone","cell",
                "http://localhost","1.0","commit","APPLICATION","SELF","1","hash",
                Map.of(),Map.of(),Instant.now(),leaseSeconds);
    }

    @Test void normalizesIdentityAndCapabilityManifest(){
        CpfRuntimeInstanceRegistration value=new CpfRuntimeInstanceRegistration(
                " instance "," service "," endpoint "," dev "," zone "," cell ",
                " http://localhost "," 1.0 "," commit "," APPLICATION "," SELF "," 1 "," hash ",
                Map.of(" reconciliation "," 1|HOT_APPLY "),Map.of(" zone "," east "),Instant.now(),60);
        assertEquals("instance",value.instanceId());
        assertEquals("service",value.serviceId());
        assertEquals("RECONCILIATION",value.capabilities().keySet().iterator().next());
        assertEquals(Map.of("zone","east"),value.labels());
    }

    @Test void rejectsBlankRequiredIdentityAndMapEntries(){
        assertThrows(IllegalArgumentException.class,()->new CpfRuntimeInstanceRegistration(
                " ","service","endpoint","dev","zone","cell","http://localhost",
                "1.0","commit","APPLICATION","SELF","1","hash",Map.of(),Map.of(),Instant.now(),60));
        assertThrows(IllegalArgumentException.class,()->new CpfRuntimeInstanceRegistration(
                "instance","service","endpoint","dev","zone","cell","http://localhost",
                "1.0","commit","APPLICATION","SELF","1","hash",Map.of("TYPE"," "),Map.of(),Instant.now(),60));
    }
}
