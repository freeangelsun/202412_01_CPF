package com.cpf.testkit;
import java.util.concurrent.ConcurrentHashMap; import java.util.concurrent.atomic.AtomicInteger;
/**
 * 지정한 failure point에서 정해진 횟수만큼 실패를 주입하는 결정적 Testkit 도구입니다.
 * <p>Retry/Recovery/UNKNOWN 테스트에 사용하며 운영 Runtime에서는 사용하지 않습니다.
 */
public final class CpfFailurePlan { private final ConcurrentHashMap<String,AtomicInteger> remaining=new ConcurrentHashMap<>(); public CpfFailurePlan failNext(String point,int count){remaining.put(point,new AtomicInteger(count));return this;} public void hit(String point){var n=remaining.get(point);if(n!=null&&n.getAndUpdate(v->Math.max(0,v-1))>0)throw new CpfInjectedFailure(point);} public static final class CpfInjectedFailure extends RuntimeException{public CpfInjectedFailure(String p){super("injected failure: "+p);}} }
