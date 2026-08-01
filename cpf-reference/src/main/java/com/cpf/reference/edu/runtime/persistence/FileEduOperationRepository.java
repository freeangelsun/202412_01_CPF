package com.cpf.reference.edu.runtime.persistence;
import com.cpf.reference.edu.runtime.application.EduConflictException;
import com.cpf.reference.edu.runtime.model.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import static java.nio.file.StandardOpenOption.*;
public final class FileEduOperationRepository implements EduOperationRepository {
    private static final Map<Path,ReentrantLock> JVM_LOCKS=new ConcurrentHashMap<>();
    private final Path dataFile; private final Path lockFile; private final ReentrantLock jvmLock;
    public FileEduOperationRepository(Path directory){
        try{Files.createDirectories(directory);}catch(IOException e){throw new UncheckedIOException(e);}
        this.dataFile=directory.resolve("edu-operation-store.bin");
        this.lockFile=directory.resolve("edu-operation-store.lock");
        this.jvmLock=JVM_LOCKS.computeIfAbsent(lockFile.toAbsolutePath().normalize(),ignored->new ReentrantLock(true));
    }
    private interface Work<T>{T apply(Snapshot s);}
    private <T> T readWrite(boolean write, Work<T> work){
        jvmLock.lock();
        try(FileChannel ch=FileChannel.open(lockFile,CREATE,WRITE); var ignored=ch.lock()){
            Snapshot s=readSnapshot();T result=work.apply(s);if(write)writeSnapshot(s);return result;
        }catch(IOException e){throw new UncheckedIOException(e);}finally{jvmLock.unlock();}
    }
    private Snapshot readSnapshot(){
        if(!Files.exists(dataFile)) return new Snapshot();
        try(ObjectInputStream in=new ObjectInputStream(Files.newInputStream(dataFile))){return (Snapshot)in.readObject();}
        catch(EOFException e){return new Snapshot();}
        catch(IOException e){throw new UncheckedIOException(e);}catch(ClassNotFoundException e){throw new IllegalStateException(e);}
    }
    private void writeSnapshot(Snapshot s){
        Path tmp=dataFile.resolveSibling(dataFile.getFileName()+".tmp");
        try(ObjectOutputStream out=new ObjectOutputStream(Files.newOutputStream(tmp,CREATE,TRUNCATE_EXISTING,WRITE))){out.writeObject(s);out.flush();}
        catch(IOException e){throw new UncheckedIOException(e);}
        try{Files.move(tmp,dataFile,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
        catch(AtomicMoveNotSupportedException e){try{Files.move(tmp,dataFile,StandardCopyOption.REPLACE_EXISTING);}catch(IOException x){throw new UncheckedIOException(x);}}
        catch(IOException e){throw new UncheckedIOException(e);}
    }
    public EduCreateResult create(EduOperationRecord r){return readWrite(true,s->{
        String key=r.requirementId()+"|"+r.idempotencyKey(); String existing=s.idempotency.get(key);
        if(existing!=null){EduOperationRecord old=s.operations.get(existing); if(!old.payloadHash().equals(r.payloadHash())) throw new EduConflictException("Idempotency key payload mismatch"); return new EduCreateResult(old,true);}
        s.operations.put(r.operationId(),r);s.idempotency.put(key,r.operationId());return new EduCreateResult(r,false);
    });}
    public Optional<EduOperationRecord> find(String id){return readWrite(false,s->Optional.ofNullable(s.operations.get(id)));}
    public Optional<EduOperationRecord> findByIdempotency(String req,String key){return readWrite(false,s->Optional.ofNullable(s.idempotency.get(req+"|"+key)).map(s.operations::get));}
    public List<EduOperationRecord> findByRequirement(String req,int limit){return readWrite(false,s->s.operations.values().stream().filter(x->x.requirementId().equals(req)).sorted(Comparator.comparing(EduOperationRecord::createdAt).reversed()).limit(limit).toList());}
    public EduOperationRecord save(EduOperationRecord r,long expected){return readWrite(true,s->{EduOperationRecord old=s.operations.get(r.operationId());if(old==null)throw new NoSuchElementException(r.operationId());if(old.recordVersion()!=expected)throw new EduConflictException("record version conflict expected="+expected+" actual="+old.recordVersion());s.operations.put(r.operationId(),r);return r;});}
    public void appendAudit(EduAuditRecord a){readWrite(true,s->{s.audits.computeIfAbsent(a.operationId(),k->new ArrayList<>()).add(a);return null;});}
    public List<EduAuditRecord> audits(String id){return readWrite(false,s->List.copyOf(s.audits.getOrDefault(id,List.of())));}
    public void saveTarget(EduTargetRecord t){readWrite(true,s->{s.targets.computeIfAbsent(t.operationId(),k->new LinkedHashMap<>()).put(t.targetId(),t);return null;});}
    public List<EduTargetRecord> targets(String id){return readWrite(false,s->List.copyOf(s.targets.getOrDefault(id,new LinkedHashMap<>()).values()));}
    public void enqueue(EduOutboxRecord e){readWrite(true,s->{s.outbox.computeIfAbsent(e.operationId(),k->new LinkedHashMap<>()).put(e.eventId(),e);return null;});}
    public void saveOutbox(EduOutboxRecord e){enqueue(e);}
    public List<EduOutboxRecord> outbox(String id){return readWrite(false,s->List.copyOf(s.outbox.getOrDefault(id,new LinkedHashMap<>()).values()));}
    public long claimLease(String key,String owner,Instant expires){return readWrite(true,s->{Lease l=s.leases.get(key);Instant now=Instant.now();if(l!=null&&l.expiresAt.isAfter(now)&&!l.owner.equals(owner))throw new EduConflictException("lease held by "+l.owner);long token=l==null?1:l.fencingToken+1;s.leases.put(key,new Lease(owner,token,expires));return token;});}
    private static final class Snapshot implements Serializable {Map<String,EduOperationRecord> operations=new LinkedHashMap<>();Map<String,String> idempotency=new HashMap<>();Map<String,List<EduAuditRecord>> audits=new HashMap<>();Map<String,LinkedHashMap<String,EduTargetRecord>> targets=new HashMap<>();Map<String,LinkedHashMap<String,EduOutboxRecord>> outbox=new HashMap<>();Map<String,Lease> leases=new HashMap<>();}
    private record Lease(String owner,long fencingToken,Instant expiresAt) implements Serializable{}
}
