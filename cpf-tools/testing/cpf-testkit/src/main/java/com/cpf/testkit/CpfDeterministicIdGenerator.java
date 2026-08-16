package com.cpf.testkit;
import com.cpf.foundation.id.CpfIdGenerator; import java.util.concurrent.atomic.AtomicLong;
public final class CpfDeterministicIdGenerator implements CpfIdGenerator { private final String prefix;private final AtomicLong seq=new AtomicLong();public CpfDeterministicIdGenerator(String prefix){this.prefix=prefix;}@Override public String nextId(){return prefix+String.format("%08d",seq.incrementAndGet());} }
