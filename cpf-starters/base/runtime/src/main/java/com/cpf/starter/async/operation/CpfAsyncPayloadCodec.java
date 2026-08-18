package com.cpf.starter.async.operation;
import com.cpf.core.api.data.encryption.*; import com.fasterxml.jackson.databind.ObjectMapper; import java.util.Objects;
/** CPF Field Encryption을 재사용해 Async command/context/result payload를 at-rest 보호합니다. */
public final class CpfAsyncPayloadCodec {
 private final CpfFieldEncryptionOperations encryption; private final ObjectMapper json;
 public CpfAsyncPayloadCodec(CpfFieldEncryptionOperations encryption,ObjectMapper json){this.encryption=Objects.requireNonNull(encryption,"encryption");this.json=Objects.requireNonNull(json,"json");}
 public String protect(String fieldName,Object value){try{String raw=json.writeValueAsString(value);CpfEncryptedField field=encryption.encrypt(fieldName,raw,CpfFieldClassification.CONFIDENTIAL,false);return json.writeValueAsString(field);}catch(Exception e){throw new IllegalArgumentException("Async payload encryption failed: "+fieldName,e);}}
 public <T> T reveal(String fieldName,String protectedValue,Class<T> type,String actor,String reason){try{CpfEncryptedField field=json.readValue(protectedValue,CpfEncryptedField.class);String raw=encryption.decrypt(fieldName,field,actor,reason);return json.readValue(raw,type);}catch(Exception e){throw new IllegalStateException("Async payload decryption failed: "+fieldName,e);}}
}
