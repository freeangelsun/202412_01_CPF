package com.cpf.member.sampleitem.repository;

import com.cpf.member.sampleitem.dto.*;
import com.cpf.core.api.page.CpfSlice;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** 중앙 Vendor Pack statement를 Typed DTO로 반환하는 DB-neutral 저장소입니다. */
@Repository
public class MemberRepository {
    private final SqlSessionTemplate sql; private final TransactionTemplate tx;
    public MemberRepository(@Qualifier("memberSqlSessionTemplate") SqlSessionTemplate sql,@Qualifier("memberTransactionManager") PlatformTransactionManager manager){this.sql=Objects.requireNonNull(sql);this.tx=new TransactionTemplate(Objects.requireNonNull(manager));}
    public MemberSearchResult search(MemberSearchRequest request){List<MemberSampleItem> items=sql.selectList(statement("search"),request);Long total=sql.selectOne(statement("count"),request);return new MemberSearchResult(items,request,total==null?0:total);}
    public Optional<MemberSampleItem> findBySampleKey(String key){return Optional.ofNullable(sql.selectOne(statement("findBySampleKey"),key));}
    public MemberSampleItem create(MemberSampleCommand c,String txId,String idem,long sequence,String actor){
        String hash=requestHash("CREATE",0,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion());
        var replay=idempotency(idem,"CREATE",hash,0); if(replay!=null)return requiredItem(replay.sampleItemId());
        var p=parameters(c,txId,idem,sequence,actor);sql.insert(statement("insert"),p);
        var item=findBySampleKey(c.sampleKey()).orElseThrow();insertIdempotency(idem,"CREATE",hash,item.sampleItemId(),item.versionNo(),false,txId);return item;
    }
    public MemberSampleItem update(long id,MemberSampleCommand c,String txId,String idem,long sequence,String actor){
        String hash=requestHash("UPDATE",id,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion());
        var replay=idempotency(idem,"UPDATE",hash,id); if(replay!=null)return requiredItem(id);
        var p=parameters(c,txId,idem,sequence,actor);p.put("sampleItemId",id);
        if(sql.update(statement("updateWithVersion"),p)!=1)throw new OptimisticLockingFailureException("Sample Item version 충돌");
        var item=requiredItem(id);insertIdempotency(idem,"UPDATE",hash,id,item.versionNo(),false,txId);return item;
    }
    public MemberDeleteResult delete(long id,long version,String txId,String idem,long sequence,String actor){
        String hash=requestHash("DELETE",id,"","","",version);var replay=idempotency(idem,"DELETE",hash,id);
        if(replay!=null)return new MemberDeleteResult(true,id,replay.resultVersion());
        var p=new HashMap<String,Object>();p.put("sampleItemId",id);p.put("versionNo",version);p.put("idempotencyKey",idem);p.put("transactionId",txId);p.put("transactionSequence",sequence);p.put("updatedBy",actor);
        if(sql.update(statement("logicalDeleteWithVersion"),p)!=1)throw new OptimisticLockingFailureException("Sample Item version 충돌");
        long deletedVersion=version+1;insertIdempotency(idem,"DELETE",hash,id,deletedVersion,true,txId);return new MemberDeleteResult(true,id,deletedVersion);
    }
    public CpfSlice<MemberSampleItem> cursor(Long afterId,int size){int safe=Math.max(1,Math.min(size,200));List<MemberSampleItem> rows=sql.selectList(statement("cursorSlice"),Map.of("cursor",afterId==null?0:afterId,"size",safe+1));boolean next=rows.size()>safe;return new CpfSlice<>(next?rows.subList(0,safe):rows,0,safe,next);}
    public boolean verifyRollback(MemberSampleCommand c,String txId,String idem,long sequence,String actor){boolean before=findBySampleKey(c.sampleKey()).isPresent();var prior=sql.selectOne(statement("findIdempotency"),idem);tx.executeWithoutResult(status->{var p=parameters(c,txId,idem,sequence,actor);sql.insert(statement("insert"),p);var item=findBySampleKey(c.sampleKey()).orElseThrow();insertIdempotency(idem,"CREATE",requestHash("CREATE",0,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion()),item.sampleItemId(),item.versionNo(),false,txId);status.setRollbackOnly();});return before==findBySampleKey(c.sampleKey()).isPresent()&&Objects.equals(prior,sql.selectOne(statement("findIdempotency"),idem));}
    private MemberIdempotencyEntry idempotency(String key,String operation,String hash,long expectedItemId){MemberIdempotencyEntry value=sql.selectOne(statement("findIdempotency"),key);if(value==null)return null;if(!value.operationCode().equals(operation)||!value.requestHash().equals(hash)||(expectedItemId>0&&value.sampleItemId()!=expectedItemId))throw new IllegalStateException("동일 idempotencyKey에 다른 요청을 사용할 수 없습니다.");return value;}
    private void insertIdempotency(String key,String operation,String hash,long itemId,long resultVersion,boolean deleted,String txId){var p=new HashMap<String,Object>();p.put("idempotencyKey",key);p.put("operationCode",operation);p.put("requestHash",hash);p.put("sampleItemId",itemId);p.put("resultVersion",resultVersion);p.put("deletedYn",deleted?"Y":"N");p.put("transactionId",txId);sql.insert(statement("insertIdempotency"),p);}
    private MemberSampleItem requiredItem(long id){MemberSampleItem value=sql.selectOne(statement("findById"),id);if(value==null)throw new IllegalArgumentException("Sample Item을 찾을 수 없습니다: "+id);return value;}
    private String requestHash(String operation,long id,String sampleKey,String itemName,String statusCode,long version){String canonical=operation+'|'+id+'|'+sampleKey+'|'+itemName+'|'+statusCode+'|'+version;try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));}catch(Exception ex){throw new IllegalStateException("멱등 요청 Hash 생성 실패",ex);}}
    private Map<String,Object> parameters(MemberSampleCommand c,String txId,String idem,long sequence,String actor){var p=new HashMap<String,Object>();p.put("sampleKey",c.sampleKey());p.put("itemName",c.itemName());p.put("statusCode",c.statusCode());p.put("versionNo",c.expectedVersion());p.put("idempotencyKey",idem);p.put("transactionId",txId);p.put("transactionSequence",sequence);p.put("createdBy",actor);p.put("updatedBy",actor);return p;}
    private String statement(String id){return "com.cpf.member.sampleitem.mapper.MemberMapper."+id;}
}