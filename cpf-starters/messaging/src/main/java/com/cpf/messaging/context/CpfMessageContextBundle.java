package com.cpf.messaging.context;

import com.cpf.core.api.context.CpfContextSnapshot;
import java.util.Objects;

/** Core correlation snapshot과 Messaging 전용 metadata를 분리해 함께 전달합니다. */
public record CpfMessageContextBundle(CpfContextSnapshot snapshot, CpfMessageContext message) {
    public CpfMessageContextBundle {
        Objects.requireNonNull(snapshot, "snapshot");
    }
}
