package com.cpf.core.api.health;
public enum CpfHealthStatus { UP, DEGRADED, DOWN, OUT_OF_SERVICE, UNKNOWN;
    public boolean ready(){ return this==UP || this==DEGRADED; }
    public static CpfHealthStatus worst(CpfHealthStatus a,CpfHealthStatus b){ return rank(a)>=rank(b)?a:b; }
    private static int rank(CpfHealthStatus s){ return switch(s){case UP->0;case DEGRADED->1;case UNKNOWN->2;case OUT_OF_SERVICE->3;case DOWN->4;}; }
}
