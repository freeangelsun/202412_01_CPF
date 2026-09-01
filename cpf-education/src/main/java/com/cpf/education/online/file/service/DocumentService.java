package com.cpf.education.online.file.service;
import com.cpf.education.online.file.dto.*; import com.cpf.file.objectstorage.api.*; import com.cpf.foundation.annotation.CpfService; import java.io.*; import java.nio.charset.StandardCharsets; import java.util.*;
// Object Storage 는 opt-in Platform 기능이다. Provider AutoConfiguration 과 같은 속성 조건을
// 붙여, 기능이 꺼진 Runtime 에서는 이 Consumer 도 함께 존재하지 않게 한다. 조건 없이 필수
// 주입하면 기능을 쓰지 않는 Runtime 이 기동조차 못 한다.
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "cpf.file.object-storage.s3", name = "enabled", havingValue = "true")
@CpfService
/** DocumentService는 CPF File/Object Storage Public API로 업로드·다운로드 책임을 분리하는 File Golden Path입니다. */
public class DocumentService { private final CpfObjectStorageOperations storage; public DocumentService(CpfObjectStorageOperations storage){this.storage=storage;}
 /** upload 동작은 CPF File/Object Storage Public API로 업로드·다운로드 책임을 분리하는 File Golden Path에서 필요한 공개 동작을 수행합니다. */
 public DocumentUploadResponse upload(DocumentUploadRequest r) throws IOException { if(r.content().length()>1_000_000) throw new IllegalArgumentException("EDU file size policy exceeded"); byte[] bytes=r.content().getBytes(StandardCharsets.UTF_8); CpfMultipartUpload upload=storage.beginMultipart(r.tenantId(),r.bucket(),r.objectKey(),"text/plain",Map.of("source","EDU")); Map<Integer,String> etags=new LinkedHashMap<>(); etags.put(1,storage.uploadPart(upload,1,bytes)); CpfObjectStorageMetadata metadata=storage.completeMultipart(upload,etags); try(InputStream in=storage.get(r.tenantId(),r.bucket(),r.objectKey(),0,bytes.length)){byte[] downloaded=in.readAllBytes();return new DocumentUploadResponse(metadata,downloaded.length,new String(downloaded,StandardCharsets.UTF_8));} }
}
