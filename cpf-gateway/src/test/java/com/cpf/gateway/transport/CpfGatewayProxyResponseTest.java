package com.cpf.gateway.transport;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfGatewayProxyResponseTest {
    @Test
    void transfersBodyWithoutMaterializingAndEnforcesOwnershipTransfer() {
        HttpHeaders original = new HttpHeaders();
        original.setContentLength(5);
        CpfGatewayProxyResponse response = new CpfGatewayProxyResponse(
                206, original, new ByteArrayInputStream("hello".getBytes()));
        HttpHeaders replaced = new HttpHeaders();
        replaced.putAll(original);
        replaced.set("X-Cpf-Gateway-Route-Id", "OXYZAA0001");

        CpfGatewayProxyResponse owned = response.replaceHeaders(replaced);
        assertThrows(IllegalStateException.class, response::readAllBytes);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertEquals(5L, owned.transferTo(output, 4096));
        assertEquals("hello", output.toString());
        owned.close();
    }
}
