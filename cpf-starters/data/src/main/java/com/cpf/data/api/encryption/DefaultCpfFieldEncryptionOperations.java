package com.cpf.data.api.encryption;
import com.cpf.core.api.data.encryption.*;
import com.cpf.core.api.security.crypto.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** Field encryption, searchable token, rekey and immutable access audit implementation. */
/** DefaultCpfFieldEncryptionOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class DefaultCpfFieldEncryptionOperations implements CpfFieldEncryptionOperations {
    public record AuditEvent(Instant occurredAt,String actorId,String action,String fieldName,String reason,String keyVersion,boolean success){}
    private final CpfCryptoOperations crypto; private final List<AuditEvent> audit=new CopyOnWriteArrayList<>();
    public DefaultCpfFieldEncryptionOperations(CpfCryptoOperations crypto){this.crypto=Objects.requireNonNull(crypto,"crypto");}
    @Override public CpfEncryptedField encrypt(String fieldName,String value,CpfFieldClassification classification,boolean searchable){
        require(fieldName,"fieldName"); require(value,"value"); Objects.requireNonNull(classification,"classification"); byte[] aad=aad(fieldName,classification);
        String token=searchable?crypto.searchableToken(normalize(value).getBytes(StandardCharsets.UTF_8),crypto.activeKeyVersion()):"";
        return new CpfEncryptedField(classification,token,crypto.activeKeyVersion(),crypto.encrypt(value.getBytes(StandardCharsets.UTF_8),aad),mask(value,classification));
    }
    @Override public String decrypt(String fieldName,CpfEncryptedField field,String actorId,String reason){
        require(actorId,"actorId"); require(reason,"reason"); try { String v=new String(crypto.decrypt(field.envelope(),aad(fieldName,field.classification())),StandardCharsets.UTF_8); audit.add(new AuditEvent(Instant.now(),actorId,"REVEAL",fieldName,reason,field.keyVersion(),true)); return v; }
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        catch(RuntimeException ex){ audit.add(new AuditEvent(Instant.now(),actorId,"REVEAL",fieldName,reason,field.keyVersion(),false)); throw ex; }
    }
    @Override public CpfEncryptedField rekey(String fieldName,CpfEncryptedField field,String targetKeyVersion,String actorId,String reason){
        require(actorId,"actorId"); require(reason,"reason"); CpfRekeyResult result=crypto.rekey(field.envelope(),aad(fieldName,field.classification()),targetKeyVersion);
        audit.add(new AuditEvent(Instant.now(),actorId,"REKEY",fieldName,reason,targetKeyVersion,true));
        return new CpfEncryptedField(field.classification(),field.searchableToken(),targetKeyVersion,result.ciphertext(),field.maskedPreview());
    }
    @Override public String mask(String value,CpfFieldClassification classification){
        if(value==null||value.isEmpty()) return ""; if(classification==CpfFieldClassification.PUBLIC||classification==CpfFieldClassification.INTERNAL) return value;
        int cp=value.codePointCount(0,value.length()); if(cp<=2) return "*".repeat(cp); int reveal=classification==CpfFieldClassification.SECRET?0:Math.min(2,cp/4);
        int start=value.offsetByCodePoints(0,reveal), end=value.offsetByCodePoints(0,cp-reveal); return value.substring(0,start)+"*".repeat(cp-(2*reveal))+value.substring(end);
    }
    /** auditEvents 작업을 CPF 표준 계약에 따라 수행한다. */
    public List<AuditEvent> auditEvents(){ return List.copyOf(audit); }
    private static String normalize(String v){ return Normalizer.normalize(v.trim(),Normalizer.Form.NFKC).toLowerCase(Locale.ROOT); }
    private static byte[] aad(String name,CpfFieldClassification c){ return (name+":"+c.name()).getBytes(StandardCharsets.UTF_8); }
    private static void require(String v,String n){ if(v==null||v.isBlank()) throw new IllegalArgumentException(n+" must not be blank"); }
}
