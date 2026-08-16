package com.cpf.file.attachment.internal;
import java.util.*;import java.util.concurrent.atomic.AtomicReference;
/** Attachment store/read 경로가 공유하는 immutable Runtime 정책입니다. */
public final class CpfAttachmentRuntimePolicy{
 private final AtomicReference<Snapshot>snapshot=new AtomicReference<>(Snapshot.defaults());public Snapshot current(){return snapshot.get();}
 public Snapshot replaceAttachment(long version,long maxBytes,Set<String>ext,Set<String>mime,boolean scan,long retentionDays,boolean quarantine,boolean watermark){return snapshot.updateAndGet(s->new Snapshot(version,new Upload(maxBytes,norm(ext),norm(mime),scan,retentionDays,quarantine,watermark),s.download()));}
 public Snapshot replaceDownload(long version,boolean permission,boolean approval,long expirySeconds,boolean watermark){return snapshot.updateAndGet(s->new Snapshot(version,s.upload(),new Download(permission,approval,expirySeconds,watermark)));}
 private static Set<String>norm(Set<String>s){if(s==null)return Set.of();return s.stream().filter(v->v!=null&&!v.isBlank()).map(v->v.trim().toLowerCase(Locale.ROOT).replace(".","")).collect(java.util.stream.Collectors.toUnmodifiableSet());}
 public record Snapshot(long version,Upload upload,Download download){private static Snapshot defaults(){return new Snapshot(0,new Upload(10_485_760L,Set.of("txt","csv","json","xml","pdf","png","jpg","jpeg","gif","zip"),Set.of(),false,3650,true,false),new Download(false,false,3600,false));}}
 public record Upload(long maxBytes,Set<String>allowedExtensions,Set<String>allowedMimeTypes,boolean scanRequired,long retentionDays,boolean quarantineOnFailure,boolean watermarkOnStore){public Upload{if(maxBytes<1||retentionDays<1)throw new IllegalArgumentException("attachment upload policy range");}}
 public record Download(boolean permissionRequired,boolean approvalRequired,long linkExpirySeconds,boolean watermarkRequired){public Download{if(linkExpirySeconds<1||linkExpirySeconds>604800)throw new IllegalArgumentException("download expiry range");}}
}
