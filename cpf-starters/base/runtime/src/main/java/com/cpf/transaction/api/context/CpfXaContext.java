package com.cpf.transaction.api.context;

/** XA global/branch/resource/recovery lineage를 표현하는 불변 Transaction 의미 값입니다. */
public record CpfXaContext(
        String globalTransactionId, String branchQualifier, String resourceId, String recoveryId, int attempt) {
    public CpfXaContext {
        globalTransactionId = required(globalTransactionId, "globalTransactionId", 256);
        branchQualifier = required(branchQualifier, "branchQualifier", 256);
        resourceId = required(resourceId, "resourceId", 180);
        recoveryId = optional(recoveryId, 180);
        if (attempt < 1) throw new IllegalArgumentException("attempt는 1 이상이어야 합니다.");
    }
    private static String required(String v,String n,int m){String x=v==null?"":v.trim();if(x.isEmpty()||x.length()>m)throw new IllegalArgumentException(n+"은(는) 1~"+m+"자여야 합니다.");return x;}
    private static String optional(String v,int m){if(v==null)return null;String x=v.trim();if(x.isEmpty())return null;if(x.length()>m)throw new IllegalArgumentException("recoveryId는 "+m+"자 이하여야 합니다.");return x;}
}
