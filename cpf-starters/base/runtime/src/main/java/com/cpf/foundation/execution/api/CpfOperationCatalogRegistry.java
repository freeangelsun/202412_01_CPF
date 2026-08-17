package com.cpf.foundation.execution.api;

import java.util.List;

/** Runtime에서 발견한 실제 online operation을 Canonical Catalog에 동기화하는 SPI입니다. */
public interface CpfOperationCatalogRegistry {
    record Operation(
            String operationId, String name, String description, String systemCode, String domainCode,
            String application, String httpMethod, String apiPath, String controllerClass,
            String handlerMethod, String sourceFingerprint) {}

    record SyncRequest(String systemCode, String domainCode, String application, String instanceId, List<Operation> operations) {
        public SyncRequest { operations = operations == null ? List.of() : List.copyOf(operations); }
    }

    record SyncResult(int discovered, int inserted, int metadataUpdated, int policiesSeeded) {}

    SyncResult synchronize(SyncRequest request);
}
