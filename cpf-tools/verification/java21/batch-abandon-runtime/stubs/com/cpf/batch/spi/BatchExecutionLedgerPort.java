package com.cpf.batch.spi;
import com.cpf.batch.api.BatchControlState;
import java.time.Instant;
import java.util.Set;
public interface BatchExecutionLedgerPort {
 void transition(String id,Set<BatchControlState> expected,BatchControlState target,String reason,String detail,Instant reconcileAfter);
 default void recordUnknown(String id,String reason,String detail){transition(id,Set.of(BatchControlState.ABANDONING,BatchControlState.UNKNOWN_RESULT),BatchControlState.UNKNOWN_RESULT,reason,detail,Instant.now());}
}
