package com.cpf.core.api.filetransfer;

/** Secret 원문이 아닌 credential 식별자만 전달하는 공개 참조값입니다. */
public record CpfCredentialReference(String scope,String credentialId,String version,String displayName){
    public CpfCredentialReference { if(credentialId==null||credentialId.isBlank()) throw new IllegalArgumentException("credentialId는 필수입니다."); scope=scope==null||scope.isBlank()?"default":scope; version=version==null||version.isBlank()?"latest":version; displayName=displayName==null||displayName.isBlank()?credentialId:displayName; }
}
