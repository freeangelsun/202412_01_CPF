package com.cpf.transaction.api.context;

/** TCC participant/phase/recovery lineage를 표현하는 불변 Transaction 의미 값입니다. */
public record CpfTccContext(
        String tccId, String participantId, String phase, String branchId, int attempt, String recoveryId) {
    public CpfTccContext {
        tccId = required(tccId, "tccId", 180);
        participantId = required(participantId, "participantId", 180);
        phase = required(phase, "phase", 32).toUpperCase(java.util.Locale.ROOT);
        if (!(phase.equals("TRY") || phase.equals("CONFIRM") || phase.equals("CANCEL"))) {
            throw new IllegalArgumentException("phase는 TRY/CONFIRM/CANCEL 중 하나여야 합니다.");
        }
        branchId = required(branchId, "branchId", 180);
        if (attempt < 1) throw new IllegalArgumentException("attempt는 1 이상이어야 합니다.");
        recoveryId = optional(recoveryId, 180);
    }
    private static String required(String v,String n,int m){String x=v==null?"":v.trim();if(x.isEmpty()||x.length()>m)throw new IllegalArgumentException(n+"은(는) 1~"+m+"자여야 합니다.");return x;}
    private static String optional(String v,int m){if(v==null)return null;String x=v.trim();if(x.isEmpty())return null;if(x.length()>m)throw new IllegalArgumentException("recoveryId는 "+m+"자 이하여야 합니다.");return x;}
}
