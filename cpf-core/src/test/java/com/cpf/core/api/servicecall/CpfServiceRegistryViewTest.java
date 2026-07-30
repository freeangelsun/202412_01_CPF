package com.cpf.core.api.servicecall;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CpfServiceRegistryViewTest {
    @Test void convertsVendorColumnCaseWithoutLeakingMapContract() {
        var service = CpfServiceRegistryView.Service.from(Map.of(
                "SERVICE_ID", "MBR", "SERVICE_NAME", "회원", "SERVICE_TYPE", "DOMAIN",
                "OWNER_MODULE_CODE", "MBR", "USE_YN", "Y", "ROW_VERSION", 3));
        assertEquals("MBR", service.serviceId());
        assertTrue(service.enabled());
        assertEquals(3, service.version());
    }
}
