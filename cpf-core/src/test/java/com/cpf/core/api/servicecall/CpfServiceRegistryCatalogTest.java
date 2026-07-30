package com.cpf.core.api.servicecall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class CpfServiceRegistryCatalogTest {
    @Test void normalizesAndValidatesCatalogCodes(){
        assertEquals("HTTP",CpfServiceRegistryCatalog.requireEndpointType("http"));
        assertEquals("STG",CpfServiceRegistryCatalog.requireEnvironment("stg"));
        assertThrows(IllegalArgumentException.class,()->CpfServiceRegistryCatalog.requireServiceType("UNKNOWN_TYPE"));
    }
}
