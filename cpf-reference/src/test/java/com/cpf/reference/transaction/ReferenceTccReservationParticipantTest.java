package com.cpf.reference.transaction;
import static org.assertj.core.api.Assertions.*;
import com.cpf.core.api.transaction.*;import java.time.*;import org.junit.jupiter.api.Test;
class ReferenceTccReservationParticipantTest {
 private static CpfTccContext ctx(String key){return new CpfTccContext("TX-TCC-1","BR-1",key,Instant.now().plusSeconds(10));}
 @Test void tryConfirmAndDuplicateConfirmAreIdempotent(){var p=new ReferenceTccReservationParticipant();var c=ctx("k1");assertThat(p.tryAction(c,100L)).isEqualTo(CpfTccResult.APPLIED);assertThat(p.tryAction(c,100L)).isEqualTo(CpfTccResult.ALREADY_APPLIED);assertThat(p.confirm(c,100L)).isEqualTo(CpfTccResult.APPLIED);assertThat(p.confirm(c,100L)).isEqualTo(CpfTccResult.ALREADY_APPLIED);}
 @Test void emptyRollbackAndDuplicateCancelAreIdempotent(){var p=new ReferenceTccReservationParticipant();var c=ctx("k2");assertThat(p.cancel(c,100L)).isEqualTo(CpfTccResult.EMPTY_ROLLBACK);assertThat(p.cancel(c,100L)).isEqualTo(CpfTccResult.ALREADY_APPLIED);assertThat(p.confirm(c,100L)).isEqualTo(CpfTccResult.REJECTED);}
 @Test void cancelAfterTryWinsAndConfirmIsRejected(){var p=new ReferenceTccReservationParticipant();var c=ctx("k3");assertThat(p.tryAction(c,100L)).isEqualTo(CpfTccResult.APPLIED);assertThat(p.cancel(c,100L)).isEqualTo(CpfTccResult.APPLIED);assertThat(p.confirm(c,100L)).isEqualTo(CpfTccResult.REJECTED);}
}
