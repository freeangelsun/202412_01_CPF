package com.cpf.starter.file.objectstorage;

import com.cpf.core.api.attachment.*;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

public final class CpfS3ObjectStorageOperations implements CpfObjectStorageOperations {
 private final S3Client s3; private final S3Presigner presigner; private final CpfObjectStorageProperties p; private final CpfObjectStorageScanHook scan;
 public CpfS3ObjectStorageOperations(S3Client s3,S3Presigner pr,CpfObjectStorageProperties p,CpfObjectStorageScanHook scan){this.s3=Objects.requireNonNull(s3);this.presigner=Objects.requireNonNull(pr);this.p=Objects.requireNonNull(p);this.scan=Objects.requireNonNull(scan);}
 private String bucket(String requested){return requested==null||requested.isBlank()?p.getBucket():requested;}
 private String key(String tenant,String objectKey){if(tenant==null||tenant.isBlank())throw new IllegalArgumentException("tenantId required");validateKey(objectKey);return tenant.trim()+"/"+objectKey;}
 @Override public CpfObjectStorageMetadata put(CpfObjectStorageRequest r){Objects.requireNonNull(r);String b=bucket(r.bucket());requireBucket(b);String k=key(r.tenantId(),r.objectKey());try{var digest=MessageDigest.getInstance("SHA-256");InputStream inspected=scan.inspect(r.objectKey(),r.content(),r.contentLength(),r.contentType());if(inspected==null)throw new IllegalStateException("scan hook returned null stream");var body=new DigestInputStream(inspected,digest);var req=PutObjectRequest.builder().bucket(b).key(k).contentType(r.contentType()).contentLength(r.contentLength()).metadata(r.metadata()).build();var out=s3.putObject(req,RequestBody.fromInputStream(body,r.contentLength()));String sha=HexFormat.of().formatHex(digest.digest());if(r.checksumSha256()!=null&&!r.checksumSha256().isBlank()&&!sha.equalsIgnoreCase(r.checksumSha256())){delete(r.tenantId(),b,r.objectKey());throw new IllegalArgumentException("sha256 checksum mismatch");}return new CpfObjectStorageMetadata(r.tenantId(),b,r.objectKey(),r.contentLength(),r.contentType(),out.eTag(),sha,out.versionId(),Instant.now(),r.metadata());}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException("SHA-256 unavailable",e);}}
 @Override public Optional<CpfObjectStorageMetadata> head(String t,String b,String k){b=bucket(b);try{var h=s3.headObject(HeadObjectRequest.builder().bucket(b).key(key(t,k)).build());return Optional.of(new CpfObjectStorageMetadata(t,b,k,h.contentLength(),h.contentType(),h.eTag(),h.checksumSHA256(),h.versionId(),h.lastModified(),h.metadata()));}catch(S3Exception e){if(e.statusCode()==404)return Optional.empty();throw e;}}
 @Override public InputStream get(String t,String b,String k,long off,long len){if(off<0||len<0)throw new IllegalArgumentException("offset/length must be >= 0");String range=len>0?"bytes="+off+"-"+(off+len-1):null;var rb=GetObjectRequest.builder().bucket(bucket(b)).key(key(t,k));if(range!=null)rb.range(range);return s3.getObject(rb.build());}
 @Override public boolean delete(String t,String b,String k){s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket(b)).key(key(t,k)).build());return true;}
 @Override public URI presignGet(String t,String b,String k,Duration ttl){ttl=ttl(ttl);var req=GetObjectRequest.builder().bucket(bucket(b)).key(key(t,k)).build();return URI.create(presigner.presignGetObject(GetObjectPresignRequest.builder().signatureDuration(ttl).getObjectRequest(req).build()).url().toString());}
 @Override public URI presignPut(String t,String b,String k,String contentType,Duration ttl){ttl=ttl(ttl);var req=PutObjectRequest.builder().bucket(bucket(b)).key(key(t,k)).contentType(contentType).build();return URI.create(presigner.presignPutObject(PutObjectPresignRequest.builder().signatureDuration(ttl).putObjectRequest(req).build()).url().toString());}
 @Override public CpfMultipartUpload beginMultipart(String t,String b,String k,String contentType,Map<String,String> metadata){b=bucket(b);var out=s3.createMultipartUpload(CreateMultipartUploadRequest.builder().bucket(b).key(key(t,k)).contentType(contentType).metadata(metadata==null?Map.of():metadata).build());return new CpfMultipartUpload(t,b,k,out.uploadId());}
 @Override public String uploadPart(CpfMultipartUpload u,int part,byte[] bytes){if(u==null||part<1||bytes==null)throw new IllegalArgumentException("invalid multipart part");var out=s3.uploadPart(UploadPartRequest.builder().bucket(u.bucket()).key(key(u.tenantId(),u.objectKey())).uploadId(u.uploadId()).partNumber(part).contentLength((long)bytes.length).build(),RequestBody.fromBytes(bytes));return out.eTag();}
 @Override public CpfObjectStorageMetadata completeMultipart(CpfMultipartUpload u,Map<Integer,String> etags){if(u==null||etags==null||etags.isEmpty())throw new IllegalArgumentException("multipart parts required");var parts=etags.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->CompletedPart.builder().partNumber(e.getKey()).eTag(e.getValue()).build()).toList();s3.completeMultipartUpload(CompleteMultipartUploadRequest.builder().bucket(u.bucket()).key(key(u.tenantId(),u.objectKey())).uploadId(u.uploadId()).multipartUpload(CompletedMultipartUpload.builder().parts(parts).build()).build());return head(u.tenantId(),u.bucket(),u.objectKey()).orElseThrow();}
 @Override public void abortMultipart(CpfMultipartUpload u){Objects.requireNonNull(u);s3.abortMultipartUpload(AbortMultipartUploadRequest.builder().bucket(u.bucket()).key(key(u.tenantId(),u.objectKey())).uploadId(u.uploadId()).build());}
 @Override public int abortMultipartOlderThan(String b,Duration age){if(age==null||age.isNegative()||age.isZero())throw new IllegalArgumentException("age must be positive");b=bucket(b);Instant cutoff=Instant.now().minus(age);int count=0;String km=null,um=null;do{var r=s3.listMultipartUploads(ListMultipartUploadsRequest.builder().bucket(b).keyMarker(km).uploadIdMarker(um).build());for(var u:r.uploads()){if(u.initiated()!=null&&u.initiated().isBefore(cutoff)){s3.abortMultipartUpload(AbortMultipartUploadRequest.builder().bucket(b).key(u.key()).uploadId(u.uploadId()).build());count++;}}km=r.nextKeyMarker();um=r.nextUploadIdMarker();if(!r.isTruncated())break;}while(true);return count;}
 private static void validateKey(String key){if(key==null||key.isBlank()||key.startsWith("/")||key.contains("..")||key.contains("\\"))throw new IllegalArgumentException("unsafe object key");}
 private static void requireBucket(String b){if(b==null||b.isBlank())throw new IllegalStateException("object storage bucket is required");}
 private static Duration ttl(Duration d){Duration use=d==null?Duration.ofMinutes(5):d;if(use.isNegative()||use.isZero()||use.compareTo(Duration.ofDays(7))>0)throw new IllegalArgumentException("presign expiry out of policy");return use;}
}
