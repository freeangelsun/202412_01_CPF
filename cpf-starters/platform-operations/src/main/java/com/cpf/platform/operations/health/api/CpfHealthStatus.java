package com.cpf.platform.operations.health.api;
/** CpfHealthStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public enum CpfHealthStatus { UP, DEGRADED, DOWN, OUT_OF_SERVICE, UNKNOWN;
    public boolean ready(){ return this==UP || this==DEGRADED; }
    public static CpfHealthStatus worst(CpfHealthStatus a,CpfHealthStatus b){ return rank(a)>=rank(b)?a:b; }
    private static int rank(CpfHealthStatus s){ return switch(s){case UP->0;case DEGRADED->1;case UNKNOWN->2;case OUT_OF_SERVICE->3;case DOWN->4;}; }
}
