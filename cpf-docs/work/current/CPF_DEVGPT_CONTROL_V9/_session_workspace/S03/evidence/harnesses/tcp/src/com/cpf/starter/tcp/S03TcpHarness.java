package com.cpf.starter.tcp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class S03TcpHarness {
    public static void main(String[] args) {
        int cases = 0;
        CpfTcpUnknownResultStore store = new CpfTcpUnknownResultStore(10);
        byte[] payload = "PAYLOAD".getBytes(StandardCharsets.UTF_8);
        RuntimeException partial = CpfTcpClient.classifyTransportFailure(
                true, "corr-partial", payload, new IOException("partial write"), store);
        require(partial instanceof CpfTcpClient.UnknownResultException,
                "partial write must be UNKNOWN"); cases++;
        require(store.find("corr-partial").isPresent(), "UNKNOWN must be recorded"); cases++;

        CpfTcpUnknownResultStore beforeStore = new CpfTcpUnknownResultStore(10);
        RuntimeException before = CpfTcpClient.classifyTransportFailure(
                false, "corr-before", payload, new IOException("connect failed"), beforeStore);
        require(before instanceof IllegalStateException,
                "pre-write must be definite failure"); cases++;
        require(beforeStore.find("corr-before").isEmpty(), "pre-write must not create UNKNOWN"); cases++;

        CpfTcpProperties properties = new CpfTcpProperties();
        properties.setHost("127.0.0.1");
        properties.setPort(1);
        properties.setFrame(CpfTcpProperties.Frame.FIXED);
        properties.setFixedLength(8);
        CpfTcpUnknownResultStore invalidStore = new CpfTcpUnknownResultStore(10);
        CpfTcpClient client = new CpfTcpClient(properties, invalidStore, null);
        try {
            client.request("corr-invalid", new byte[] {1,2,3});
            throw new AssertionError("invalid fixed frame accepted");
        } catch (IllegalArgumentException expected) {
            require(expected.getMessage().contains("before write"), "deterministic message"); cases++;
        }
        require(invalidStore.find("corr-invalid").isEmpty(), "invalid frame no UNKNOWN"); cases++;
        System.out.println("S03_TCP_OUTCOME_HARNESS PASS cases=" + cases);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
