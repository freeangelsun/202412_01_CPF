package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CpfRuntimeRateLimitExceptionTest {
    @Test void remoteLimitCanRemainUnknownWithoutFabricatingAQuota(){
        CpfRuntimeRateLimitException ex=new CpfRuntimeRateLimitException(" remote rate limit ");
        assertEquals(-1,ex.limit());
        assertEquals("remote rate limit",ex.getMessage());
    }
}
