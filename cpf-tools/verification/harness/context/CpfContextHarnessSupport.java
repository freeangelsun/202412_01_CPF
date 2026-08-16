import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

final class CpfContextHarnessSupport {
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneOffset.UTC);
    private CpfContextHarnessSupport() {}

    static CpfContextExecutionFactory factory(String transactionId) {
        AtomicInteger sequence = new AtomicInteger();
        CpfTransactionIdGenerator tx = () -> transactionId;
        CpfExecutionIdGenerator ex = new CpfExecutionIdGenerator() {
            public String newExecutionId() { return "EX-" + sequence.incrementAndGet(); }
            public String newSegmentId() { return "SG-" + sequence.incrementAndGet(); }
        };
        CpfBusinessDateProvider date = () -> LocalDate.of(2026, 8, 10);
        return new CpfContextExecutionFactory(tx, ex, date, CLOCK);
    }

    static CpfContext root(CpfContextExecutionFactory factory, String standardExecutionId,
                           CpfContext.CpfExecutionType type, CpfContext.CpfTransactionOriginKind origin,
                           CpfContext.CpfIdentityContext identity, CpfContext.CpfTenantContext tenant) {
        return factory.newRoot(new CpfContextExecutionFactory.RootSpec(
                "COR-1", standardExecutionId, type, origin, "harness", null,
                null, identity, tenant, CLOCK.instant().plusSeconds(30)));
    }

    static CpfContextSnapshot child(CpfContextExecutionFactory factory, CpfContext parent,
                                    String standardExecutionId, CpfContext.CpfExecutionType type, int attempt) {
        return factory.childSnapshot(CpfContextSnapshot.capture(parent, CLOCK.instant()),
                new CpfContextExecutionFactory.ChildSpec(
                        standardExecutionId, type, attempt, parent.execution().deadline(), parent.operation()));
    }

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
