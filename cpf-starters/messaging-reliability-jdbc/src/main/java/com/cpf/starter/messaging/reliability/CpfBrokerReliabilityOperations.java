package com.cpf.starter.messaging.reliability;
import com.cpf.core.common.broker.CpfBrokerReplayPort;
import com.cpf.core.common.broker.CpfBrokerResult;
import java.time.Instant;
import java.util.List;
/** Auditable operations facade. Replay requires an operator and a non-empty reason. */
public final class CpfBrokerReliabilityOperations {
 private final CpfBrokerReplayPort replay;
 public CpfBrokerReliabilityOperations(CpfBrokerReplayPort replay){this.replay=replay;}
 public CpfBrokerResult replay(String messageId,String operatorId,String reason){requireAudit(operatorId,reason);return replay.replay(messageId);}
 public List<CpfBrokerResult> replayRange(String topic,Instant from,Instant to,int limit,String operatorId,String reason){requireAudit(operatorId,reason);return replay.replayRange(topic,from,to,Math.min(Math.max(limit,1),5000));}
 private static void requireAudit(String operatorId,String reason){if(operatorId==null||operatorId.isBlank())throw new SecurityException("operatorId is required");if(reason==null||reason.isBlank())throw new IllegalArgumentException("replay reason is required");}
}
