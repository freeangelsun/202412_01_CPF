package com.cpf.common.data.encryption;
import com.cpf.core.api.data.encryption.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Actual application/query consumer for encrypted fields. */
public final class CpfProtectedRecordService {
    public record ProtectedRecord(String recordId,CpfEncryptedField value,long version){}
    private final CpfFieldEncryptionOperations encryption; private final Map<String,ProtectedRecord> records=new ConcurrentHashMap<>();
    public CpfProtectedRecordService(CpfFieldEncryptionOperations encryption){this.encryption=Objects.requireNonNull(encryption,"encryption");}
    public ProtectedRecord save(String recordId,String fieldName,String plaintext,CpfFieldClassification classification,boolean searchable){
        Objects.requireNonNull(recordId,"recordId"); return records.compute(recordId,(id,old)->new ProtectedRecord(id,encryption.encrypt(fieldName,plaintext,classification,searchable),old==null?1:old.version()+1));
    }
    public Optional<ProtectedRecord> findBySearchToken(String token){ return records.values().stream().filter(r->MessageDigestSafe.equals(r.value().searchableToken(),token)).findFirst(); }
    public String reveal(String recordId,String fieldName,String actorId,String reason){ ProtectedRecord r=Optional.ofNullable(records.get(recordId)).orElseThrow(); return encryption.decrypt(fieldName,r.value(),actorId,reason); }
    public ProtectedRecord rekey(String recordId,String fieldName,String target,String actorId,String reason){ return records.compute(recordId,(id,old)->{if(old==null)throw new NoSuchElementException(id);return new ProtectedRecord(id,encryption.rekey(fieldName,old.value(),target,actorId,reason),old.version()+1);}); }
    private static final class MessageDigestSafe { static boolean equals(String a,String b){ if(a==null||b==null)return false; return java.security.MessageDigest.isEqual(a.getBytes(java.nio.charset.StandardCharsets.UTF_8),b.getBytes(java.nio.charset.StandardCharsets.UTF_8)); } }
}
