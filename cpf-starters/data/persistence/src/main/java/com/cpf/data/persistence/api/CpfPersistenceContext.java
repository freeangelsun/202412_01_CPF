package com.cpf.data.persistence.api;


/** transaction/tenant/actor lineage를 Repository 경계에 전달하는 불변 context입니다. */
public record CpfPersistenceContext(String transactionId, String tenantId, String actorId) {
    public CpfPersistenceContext {
        transactionId = require(transactionId, "transactionId");
        tenantId = normalize(tenantId);
        actorId = normalize(actorId);
    }
    private static String require(String value, String name) { String v=normalize(value); if(v==null) throw new IllegalArgumentException(name+"는 필수입니다."); return v; }
    private static String normalize(String value) { if(value==null)return null; String v=value.trim(); return v.isEmpty()?null:v; }
}
