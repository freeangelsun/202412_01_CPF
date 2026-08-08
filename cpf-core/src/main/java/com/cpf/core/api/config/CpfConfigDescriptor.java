package com.cpf.core.api.config;
/** CPF Typed Configuration의 Prefix와 변경 정책을 나타냅니다. */
public record CpfConfigDescriptor(String prefix,CpfConfigMutability mutability,boolean secretSeparated,String ownerType){
 public CpfConfigDescriptor{if(prefix==null||prefix.isBlank())throw new IllegalArgumentException("prefix required");if(mutability==null)throw new IllegalArgumentException("mutability required");ownerType=ownerType==null?"":ownerType;}
}
