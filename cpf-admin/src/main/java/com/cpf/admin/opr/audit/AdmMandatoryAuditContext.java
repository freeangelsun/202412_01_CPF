package com.cpf.admin.opr.audit;

/** 한 ADM HTTP mutation 안에서 사전 reservation과 상세 Audit을 연결합니다. */
public final class AdmMandatoryAuditContext {
    private static final ThreadLocal<State> STATE = new ThreadLocal<>();
    private AdmMandatoryAuditContext() { }
    public static void begin(long deliveryId) { STATE.set(new State(deliveryId, false)); }
    public static Long deliveryId() { State s=STATE.get(); return s == null ? null : s.deliveryId(); }
    public static boolean completed() { State s=STATE.get(); return s != null && s.completed(); }
    public static void markCompleted() { State s=STATE.get(); if(s!=null) STATE.set(new State(s.deliveryId(), true)); }
    public static void clear() { STATE.remove(); }
    private record State(long deliveryId, boolean completed) { }
}
