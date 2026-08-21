package com.cpf.foundation.id;
import java.util.UUID;
/** UUID 기반 CPF 식별자를 생성하는 기본 CpfIdGenerator 구현입니다. */
public final class CpfUuidIdGenerator implements CpfIdGenerator {
    @Override public String nextId() { return UUID.randomUUID().toString(); }
}
