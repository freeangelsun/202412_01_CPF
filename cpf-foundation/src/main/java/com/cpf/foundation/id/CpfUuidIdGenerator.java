package com.cpf.foundation.id;
import java.util.UUID;
public final class CpfUuidIdGenerator implements CpfIdGenerator {
    @Override public String nextId() { return UUID.randomUUID().toString(); }
}
