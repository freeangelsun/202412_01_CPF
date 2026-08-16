package com.cpf.education.data.transaction;
import com.cpf.core.api.transaction.*; import java.util.concurrent.ConcurrentHashMap; import java.util.concurrent.ConcurrentMap;
/** TCC hold/reservation 의미를 업무 Consumer가 소유하는 실행형 education participant입니다. */
public final class EducationTccReservationParticipant implements CpfTccParticipant<Long> {
 private final ConcurrentMap<String,CpfTccPhase> states=new ConcurrentHashMap<>();
 @Override public CpfTccResult tryAction(CpfTccContext c,Long amount){if(amount==null||amount<=0)return CpfTccResult.FAILED; CpfTccPhase old=states.putIfAbsent(c.idempotencyKey(),CpfTccPhase.TRY); return old==null?CpfTccResult.APPLIED:CpfTccResult.ALREADY_APPLIED;}
 @Override public CpfTccResult confirm(CpfTccContext c,Long amount){for(;;){CpfTccPhase old=states.get(c.idempotencyKey());if(old==CpfTccPhase.CONFIRM)return CpfTccResult.ALREADY_APPLIED;if(old==null||old==CpfTccPhase.CANCEL)return CpfTccResult.HANGING_REJECTED;if(states.replace(c.idempotencyKey(),old,CpfTccPhase.CONFIRM))return CpfTccResult.APPLIED;}}
 @Override public CpfTccResult cancel(CpfTccContext c,Long amount){for(;;){CpfTccPhase old=states.get(c.idempotencyKey());if(old==null){if(states.putIfAbsent(c.idempotencyKey(),CpfTccPhase.CANCEL)==null)return CpfTccResult.EMPTY_ROLLBACK;continue;}if(old==CpfTccPhase.CANCEL)return CpfTccResult.ALREADY_APPLIED;if(old==CpfTccPhase.CONFIRM)return CpfTccResult.HANGING_REJECTED;if(states.replace(c.idempotencyKey(),old,CpfTccPhase.CANCEL))return CpfTccResult.APPLIED;}}
 public CpfTccPhase state(String key){return states.get(key);}
}
