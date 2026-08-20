package external.online.external.service;

import external.online.external.service.DomainAuditLogger;
import external.online.base.ExternalBaseService;
import external.online.external.repository.SampleTransactionRepository;
import external.online.external.model.*;
import external.online.external.dto.*;
import external.online.external.service.SampleTransactionPolicy;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.annotation.CpfService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;

/** HTTP -> Service -> DAO/Mapper -> CUSTOMER_BUSINESS_DB 실제 Sample Transaction입니다. */
@CpfService
public class SampleTransactionService extends ExternalBaseService {
    private final SampleTransactionRepository repository;
    private final SampleTransactionPolicy policy;
    private final DomainAuditLogger audit;
    private final Clock clock;
    public SampleTransactionService(SampleTransactionRepository repository, SampleTransactionPolicy policy, DomainAuditLogger audit, Clock clock) {
        this.repository=repository; this.policy=policy; this.audit=audit; this.clock=clock;
    }

    @CpfTransactional
    /** create 작업을 CPF 표준 계약에 따라 수행한다. */
    public SampleItem create(CreateSampleRequest request) {
        String tx=requireTransactionId(); String actor=actorId();
        String idem=policy.requireIdempotencyKey(request.idempotencyKey());
        String sampleKey=policy.requireSampleKey(request.sampleKey());
        String itemName=policy.requireItemName(request.itemName());
        String hash=requestHash("CREATE",sampleKey,itemName);
        SampleItem replay=replay(idem,"CREATE",hash,tx);
        if (replay != null) return replay;
        if (repository.findBySampleKey(sampleKey).isPresent())
            throw new CpfBusinessException(CpfErrorCode.DUPLICATE, "sampleKey가 이미 존재합니다.");
        Instant now=clock.instant();
        SampleItem item=new SampleItem(0L,sampleKey,itemName,"ACTIVE",0L,idem,tx,
                transactionSequence(),now,"N",actor,now,actor,now);
        try {
            if (repository.insert(item) != 1)
                throw new CpfBusinessException(CpfErrorCode.DATABASE_ERROR, "Sample insert 결과가 1건이 아닙니다.");
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (DuplicateKeyException duplicate) {
            throw new CpfBusinessException(CpfErrorCode.DUPLICATE,"sampleKey 또는 idempotencyKey가 이미 존재합니다.");
        }
        recordIdempotency(idem,"CREATE",hash,item,tx,now);
        audit.success("CREATE",tx,Long.toString(item.getSampleItemId()));
        return item;
    }

    @CpfTransactional(readOnly=true)
    /** detail 작업을 CPF 표준 계약에 따라 수행한다. */
    public SampleItem detail(long id) {
        return repository.findById(id).orElseThrow(() -> new CpfBusinessException(CpfErrorCode.NOT_FOUND, "Sample을 찾을 수 없습니다."));
    }

    @CpfTransactional(readOnly=true)
    public SamplePage search(SampleSearchRequest request) {
        int page=request.safePage(), size=request.safeSize();
        return new SamplePage(repository.search(request.keyword(),request.statusCode(),page,size,
                "sample_item_id","ASC"),repository.count(request.keyword(),request.statusCode()),page,size);
    }

    @CpfTransactional(readOnly=true)
    /** slice 작업을 CPF 표준 계약에 따라 수행한다. */
    public SampleSlice slice(SampleSearchRequest request) {
        int size=request.safeSize();
        List<SampleItem> rows=repository.cursorSlice(request.keyword(),request.statusCode(),request.safeCursor(),size+1);
        boolean hasNext=rows.size()>size;
        List<SampleItem> items=hasNext ? List.copyOf(rows.subList(0,size)) : List.copyOf(rows);
        Long nextCursor=hasNext && !items.isEmpty() ? items.get(items.size()-1).getSampleItemId() : null;
        return new SampleSlice(items,hasNext,nextCursor);
    }

    @CpfTransactional
    /** update 작업을 CPF 표준 계약에 따라 수행한다. */
    public SampleItem update(long id, UpdateSampleRequest request) {
        String tx=requireTransactionId(); String actor=actorId();
        policy.requireExpectedVersion(request.expectedVersion());
        String idem=policy.requireIdempotencyKey(request.idempotencyKey());
        String itemName=policy.requireItemName(request.itemName());
        String status=policy.requireStatusCode(request.statusCode());
        String hash=requestHash("UPDATE",Long.toString(id),itemName,status,Long.toString(request.expectedVersion()));
        SampleItem replay=replay(idem,"UPDATE",hash,tx);
        if (replay != null) return replay;
        SampleItem current=repository.findForUpdate(id)
                .orElseThrow(() -> new CpfBusinessException(CpfErrorCode.NOT_FOUND,"Sample을 찾을 수 없습니다."));
        if (current.getVersionNo()!=request.expectedVersion())
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"expectedVersion이 현재 Version과 다릅니다.");
        Instant now=clock.instant();
        SampleItem command=new SampleItem(id,current.getSampleKey(),itemName,status,current.getVersionNo(),idem,tx,
                transactionSequence(),now,"N",current.getCreatedBy(),current.getCreatedAt(),actor,now);
        if (repository.updateWithVersion(command) != 1)
            throw new CpfBusinessException(CpfErrorCode.CONFLICT, "Version 충돌 또는 대상 부재로 Update하지 못했습니다.");
        SampleItem updated=detail(id); recordIdempotency(idem,"UPDATE",hash,updated,tx,now);
        audit.success("UPDATE",tx,Long.toString(id)); return updated;
    }

    @CpfTransactional
    /** delete 작업을 CPF 표준 계약에 따라 수행한다. */
    public SampleItem delete(long id, DeleteSampleRequest request) {
        String tx=requireTransactionId(); String actor=actorId();
        policy.requireExpectedVersion(request.expectedVersion());
        String idem=policy.requireIdempotencyKey(request.idempotencyKey());
        String hash=requestHash("DELETE",Long.toString(id),Long.toString(request.expectedVersion()));
        SampleItem replay=replay(idem,"DELETE",hash,tx);
        if (replay != null) return replay;
        SampleItem current=repository.findForUpdate(id)
                .orElseThrow(() -> new CpfBusinessException(CpfErrorCode.NOT_FOUND,"Sample을 찾을 수 없습니다."));
        if (current.getVersionNo()!=request.expectedVersion())
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"expectedVersion이 현재 Version과 다릅니다.");
        Instant now=clock.instant();
        SampleItem command=new SampleItem(id,current.getSampleKey(),current.getItemName(),current.getStatusCode(),
                current.getVersionNo(),idem,tx,transactionSequence(),now,"Y",current.getCreatedBy(),
                current.getCreatedAt(),actor,now);
        if (repository.logicalDeleteWithVersion(command)!=1)
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"Version 충돌 또는 대상 부재로 Delete하지 못했습니다.");
        SampleItem deleted=detail(id); recordIdempotency(idem,"DELETE",hash,deleted,tx,now);
        audit.success("DELETE",tx,Long.toString(id)); return deleted;
    }

    /** Failure-injection Test가 실제 Transaction rollback을 증명할 수 있는 명시적 Probe입니다. */
    @CpfTransactional
    public void rollbackProbe(CreateSampleRequest request) {
        create(request);
        throw new CpfBusinessException(CpfErrorCode.BUSINESS_RULE_VIOLATION,
                "의도된 Sample Transaction rollback probe입니다.");
    }

    private SampleItem replay(String key, String operation, String requestHash, String transactionId) {
        var existing=repository.findIdempotency(key);
        if (existing.isEmpty()) return null;
        SampleIdempotencyRecord record=existing.get();
        if (!operation.equals(record.operationCode()) || !requestHash.equals(record.requestHash()))
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"같은 idempotencyKey가 다른 요청에 사용되었습니다.");
        SampleItem item=repository.findById(record.sampleItemId())
                .orElseThrow(() -> new CpfBusinessException(CpfErrorCode.CONFLICT,"멱등 결과 Entity가 없습니다."));
        audit.replay(transactionId,Long.toString(item.getSampleItemId()));
        return item;
    }

    private void recordIdempotency(String key, String operation, String requestHash,
            SampleItem item, String transactionId, Instant now) {
        SampleIdempotencyRecord record=new SampleIdempotencyRecord(key,operation,requestHash,
                item.getSampleItemId(),item.getVersionNo(),item.getDeletedYn(),transactionId,now);
        try {
            if (repository.insertIdempotency(record)!=1)
                throw new CpfBusinessException(CpfErrorCode.CONFLICT,"Idempotency ledger 기록에 실패했습니다.");
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (DuplicateKeyException duplicate) {
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"Idempotency ledger가 동시 요청에 의해 먼저 기록되었습니다.");
        }
    }

    private static String requestHash(String... values) {
        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            for (String value: values) {
                byte[] bytes=(value==null ? "" : value).getBytes(StandardCharsets.UTF_8);
                digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.US_ASCII));
                digest.update((byte)':'); digest.update(bytes); digest.update((byte)'|');
            }
            return HexFormat.of().formatHex(digest.digest());
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 provider가 없습니다.", impossible);
        }
    }
}
