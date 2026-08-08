package com.cpf.starter.data.persistence.jpa;

/** JPA Query/CRUD 실행시간과 transaction/tenant/actor lineage를 전달하는 관측 이벤트입니다. */
public record CpfJpaQueryObservation(
        String operation,
        String entityName,
        long elapsedMillis,
        boolean slow,
        String transactionId,
        String tenantId,
        String actorId) { }
