package com.cpf.testkit;
import com.cpf.foundation.id.CpfIdGenerator; import java.util.concurrent.atomic.AtomicLong;
/**
 * 반복 가능한 CPF Test Evidence를 위해 순번 기반 ID를 생성하는 Testkit 구현입니다.
 * <p>운영 ID 생성기로 사용하지 않으며 테스트 간 독립적인 prefix를 부여해 충돌을 방지합니다.
 */
public final class CpfDeterministicIdGenerator implements CpfIdGenerator { private final String prefix;private final AtomicLong seq=new AtomicLong();public CpfDeterministicIdGenerator(String prefix){this.prefix=prefix;}@Override public String nextId(){return prefix+String.format("%08d",seq.incrementAndGet());} }
