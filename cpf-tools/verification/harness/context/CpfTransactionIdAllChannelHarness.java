package com.cpf.verification.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

/** 모든 표준 execution type에서 transactionId lineage가 바뀌지 않는지 검증합니다. */
public final class CpfTransactionIdAllChannelHarness {
    public static void main(String[] args) throws Exception {
        AtomicInteger ids = new AtomicInteger();
        CpfTransactionIdGenerator tx = () -> "TX-ALL-CHANNEL";
        CpfExecutionIdGenerator exec = new CpfExecutionIdGenerator() {
            public String newExecutionId() { return "EX-" + ids.incrementAndGet(); }
            public String newSegmentId() { return "SG-" + ids.incrementAndGet(); }
        };
        CpfBusinessDateProvider date = () -> LocalDate.of(2026, 8, 10);
        Clock clock = Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneOffset.UTC);
        CpfContextExecutionFactory factory = new CpfContextExecutionFactory(tx, exec, date, clock);
        CpfContext root = factory.newRoot(new CpfContextExecutionFactory.RootSpec(
                "CORR-1", "root", CpfContext.CpfExecutionType.API,
                CpfContext.CpfTransactionOriginKind.HTTP, "WEB", "INBOUND-1", null, null, null,
                clock.instant().plusSeconds(60)));
        CpfContextSnapshot rootSnapshot = CpfContextSnapshot.capture(root, clock.instant());
        try (AutoCloseable ignored = CpfContexts.bind(rootSnapshot)) {
            for (CpfContext.CpfExecutionType type : EnumSet.allOf(CpfContext.CpfExecutionType.class)) {
                CpfContextSnapshot child = factory.childSnapshot(rootSnapshot,
                        new CpfContextExecutionFactory.ChildSpec("child-" + type.name(), type, 1,
                                clock.instant().plusSeconds(30), null));
                if (!"TX-ALL-CHANNEL".equals(child.context().transactionId())) throw new AssertionError("tx changed: " + type);
                if (!root.transaction().rootTransactionId().equals(child.context().transaction().rootTransactionId())) throw new AssertionError("root tx changed: " + type);
                if (root.executionId().equals(child.context().executionId())) throw new AssertionError("execution not renewed: " + type);
                CpfContexts.run(child, () -> {
                    if (!"TX-ALL-CHANNEL".equals(CpfContexts.transactionId())) throw new AssertionError("bound tx changed");
                });
                if (!"TX-ALL-CHANNEL".equals(CpfContexts.transactionId())) throw new AssertionError("parent not restored");
            }
            CpfContext propagated = factory.fromTrustedPropagation(
                    "TX-EXTERNAL", "TX-ROOT-EXTERNAL", "CORR-X", LocalDate.of(2026, 8, 10),
                    clock.instant(), CpfContext.CpfTransactionOriginKind.MESSAGE, "REMOTE", "REMOTE-TX",
                    "trusted", root.executionId(), root.execution().rootExecutionId(), root.segmentId(),
                    CpfContext.CpfExecutionType.MESSAGE, 1, 1, null, null, null,
                    clock.instant().plusSeconds(20));
            if (!"TX-EXTERNAL".equals(propagated.transactionId())) throw new AssertionError("trusted tx lost");
            if (!"TX-ROOT-EXTERNAL".equals(propagated.transaction().rootTransactionId())) throw new AssertionError("trusted root tx lost");
        }
        if (CpfContexts.current() != null) throw new AssertionError("context leak");
        System.out.println("CPF_TXID_ALL_CHANNEL_RUNTIME=PASS executionTypes=7 trustedPropagation=true restore=true leak=0");
    }
}
