package com.cpf.education.online.cache.service;
import com.cpf.data.cache.api.*;
import com.cpf.education.online.cache.dto.MemberProfileResponse;
import com.cpf.education.online.cache.repository.MemberProfileRepository;
import com.cpf.foundation.annotation.CpfService;
import java.nio.charset.StandardCharsets; import java.time.*;
@CpfService
/** MemberProfileService는 CpfCache의 getOrLoad와 명시적 무효화를 사용하는 Cache Golden Path입니다. */
public class MemberProfileService {
 private final CpfCache cache; private final MemberProfileRepository repository;
 /** MemberProfileService 동작은 CpfCache의 getOrLoad와 명시적 무효화를 사용하는 Cache Golden Path에서 필요한 공개 동작을 수행합니다. */
 public MemberProfileService(CpfCache cache, MemberProfileRepository repository){this.cache=cache;this.repository=repository;}
 /** find 동작은 CpfCache의 getOrLoad와 명시적 무효화를 사용하는 Cache Golden Path에서 필요한 공개 동작을 수행합니다. */
 public MemberProfileResponse find(String memberId){
   CpfCacheKey key=new CpfCacheKey("member-profile",memberId,null); Duration ttl=Duration.ofMinutes(5);
   CpfCacheValue value=cache.getOrLoad(key,new CpfCacheOptions(ttl,Duration.ofSeconds(30),Duration.ofMillis(200),Duration.ofSeconds(3),true,false), ignored -> encode(repository.findRequired(memberId),ttl));
   String[] parts=new String(value.value(),StandardCharsets.UTF_8).split("\\|",2); return new MemberProfileResponse(parts[0],parts[1]);
 }
 /** invalidate 동작은 CpfCache의 getOrLoad와 명시적 무효화를 사용하는 Cache Golden Path에서 필요한 공개 동작을 수행합니다. */
 public void invalidate(String memberId){cache.evict(new CpfCacheKey("member-profile",memberId,null));}
 private static CpfCacheValue encode(MemberProfileResponse p,Duration ttl){byte[] b=(p.memberId()+"|"+p.displayName()).getBytes(StandardCharsets.UTF_8);return new CpfCacheValue(true,false,b,"text/plain",1,Instant.now().plus(ttl));}
}
