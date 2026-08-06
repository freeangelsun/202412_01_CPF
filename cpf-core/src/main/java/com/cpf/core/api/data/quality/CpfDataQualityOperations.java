package com.cpf.core.api.data.quality;
import java.util.*;
public interface CpfDataQualityOperations {
 CpfDataQualityRule register(CpfDataQualityRule rule,String actorId,String reason);
 CpfDataQualityDecision validate(String recordId,Map<String,Object> record);
 Optional<QuarantineItem> quarantine(String quarantineId);
 QuarantineItem correct(String quarantineId,long expectedVersion,Map<String,Object> corrected,String actorId,String reason,boolean approved);
 CpfDataQualityDecision replay(String quarantineId,String actorId,String reason);
 ReconcileResult reconcile(String actorId,String reason);
 record QuarantineItem(String quarantineId,String recordId,Map<String,Object> original,Map<String,Object> corrected,String state,long version,List<CpfDataQualityDecision.Violation> violations){}
 record ReconcileResult(int inspected,int replayed,int remaining){}
}
