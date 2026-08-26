package com.cpf.file.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import java.util.Objects;

/** File 처리마다 동일 transaction lineage를 유지하는 child execution을 생성합니다. */
public final class CpfFileContextSupport {
    private final CpfContextExecutionFactory factory;

    public CpfFileContextSupport(CpfContextExecutionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public CpfContextSnapshot child(
            String fileJobId,
            String transferId,
            String logicalFileName,
            String protocol,
            String bucketAlias,
            String objectKeyHash,
            String provider,
            String direction,
            String checkpointId,
            String partId,
            String checksum,
            String unknownOutcomeId,
            int attempt,
            String recoveryId) {
        CpfContextSnapshot parent = CpfContexts.requireSnapshot();
        int normalizedAttempt = Math.max(1, attempt);
        new CpfFileProcessingContext(
                fileJobId, transferId, logicalFileName, protocol, bucketAlias, objectKeyHash,
                provider, direction, parent.context().businessDate(), checkpointId, partId,
                checksum, unknownOutcomeId, normalizedAttempt, recoveryId);
        return factory.childSnapshot(
                parent,
                new CpfContextExecutionFactory.ChildSpec(
                        standardExecutionId(protocol, direction),
                        CpfContext.CpfExecutionType.INTEGRATION,
                        normalizedAttempt,
                        parent.context().execution().deadline(),
                        parent.context().operation()));
    }

    private static String standardExecutionId(String protocol, String direction) {
        String p = protocol == null || protocol.isBlank() ? "file" : protocol.trim().toLowerCase(java.util.Locale.ROOT);
        String d = direction == null || direction.isBlank() ? "operation" : direction.trim().toLowerCase(java.util.Locale.ROOT);
        return "file." + p + "." + d;
    }
}
