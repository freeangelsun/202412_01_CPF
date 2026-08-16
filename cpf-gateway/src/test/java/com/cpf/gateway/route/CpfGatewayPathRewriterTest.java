package com.cpf.gateway.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CpfGatewayPathRewriterTest {
    @Test
    void preservesWildcardSuffixInSeparateTargetTemplate() {
        assertEquals("/internal/orders/123/items",
                CpfGatewayPathRewriter.rewrite(
                        "/orders/**", "/internal/orders/**", "/orders/123/items"));
    }

    @Test
    void rewritesNamedVariables() {
        assertEquals("/internal/customer/42/order/7",
                CpfGatewayPathRewriter.rewrite(
                        "/customers/{customerId}/orders/{orderId}",
                        "/internal/customer/{customerId}/order/{orderId}",
                        "/customers/42/orders/7"));
    }

    @Test
    void rejectsTraversalAndEncodedSeparatorsBeforeDispatch() {
        assertThrows(IllegalArgumentException.class,
                () -> CpfGatewayPathRewriter.rewrite("/orders/**", "/internal/**", "/orders/%2e%2e/admin"));
        assertThrows(IllegalArgumentException.class,
                () -> CpfGatewayPathRewriter.rewrite("/orders/**", "/internal/**", "/orders/a%2Fb"));
        assertTrue(CpfGatewayPathRewriter.matches("/orders/**", "/orders/123"));
    }
}
