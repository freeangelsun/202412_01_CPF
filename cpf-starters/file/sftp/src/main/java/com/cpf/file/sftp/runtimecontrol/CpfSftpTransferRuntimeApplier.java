package com.cpf.file.sftp.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.file.spi.filetransfer.CpfFileTransferEndpoint;
import com.cpf.file.common.filetransfer.CpfFileTransferRuntimeState;
import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;
import com.cpf.security.api.CpfCredentialRef;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 실제 FileTransferEngine이 사용하는 SFTP/FTP endpoint 전체 snapshot을 교체합니다. */
public final class CpfSftpTransferRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfFileTransferRuntimeState state;

    public CpfSftpTransferRuntimeApplier(CpfFileTransferRuntimeState state) {
        this.state = state;
    }

    @Override
    public String changeType() {
        return "SFTP_TRANSFER";
    }

    @Override
    public boolean supportsIdempotentReplay() {
        return true;
    }

    @Override
    public boolean snapshotCapable() {
        return true;
    }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            Object raw = CpfRuntimePayloadReader.value(delivery.payload(), "endpoints");
            if (!(raw instanceof List<?> entries)) {
                return CpfRuntimeApplyResult.failure(
                        "SFTP_ENDPOINTS_REQUIRED",
                        "endpoints snapshot이 필요합니다.");
            }
            LinkedHashMap<String, CpfFileTransferEndpoint> endpoints = new LinkedHashMap<>();
            for (Object entry : entries) {
                if (!(entry instanceof Map<?, ?> source)) {
                    throw new IllegalArgumentException("endpoint object가 필요합니다.");
                }
                String endpointCode = required(source, "endpointCode");
                String protocol = required(source, "protocol");
                String host = required(source, "host");
                int port = integer(source.get("port"), 22);
                String remoteBasePath = required(source, "remoteBasePath");
                String credentialId = required(source, "credentialId");
                CpfCredentialRef credential = new CpfCredentialRef(
                        optional(source, "credentialScope", "default"),
                        credentialId,
                        optional(source, "credentialVersion", "latest"),
                        endpointCode);
                long timeoutMillis = number(source.get("timeoutMillis"), 30_000L);
                endpoints.put(
                        endpointCode,
                        new CpfFileTransferEndpoint(
                                endpointCode,
                                protocol,
                                host,
                                port,
                                remoteBasePath,
                                credential,
                                Duration.ofMillis(timeoutMillis),
                                attributes(source.get("attributes"))));
            }
            state.replaceEndpoints(delivery.desiredVersion(), endpoints);
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "SFTP_ENDPOINT_INVALID",
                    "SFTP endpoint snapshot이 유효하지 않습니다.");
        }
    }

    private String required(Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException(key + " 필수");
        }
        return String.valueOf(value);
    }

    private String optional(Map<?, ?> source, String key, String fallback) {
        Object value = source.get(key);
        return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
    }

    private int integer(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        return value == null ? fallback : Integer.parseInt(String.valueOf(value));
    }

    private long number(Object value, long fallback) {
        if (value instanceof Number number) return number.longValue();
        return value == null ? fallback : Long.parseLong(String.valueOf(value));
    }

    private Map<String, String> attributes(Object value) {
        if (!(value instanceof Map<?, ?> source)) return Map.of();
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        source.forEach((key, entry) -> {
            if (key != null && entry != null) {
                result.put(String.valueOf(key), String.valueOf(entry));
            }
        });
        return Map.copyOf(result);
    }
}
