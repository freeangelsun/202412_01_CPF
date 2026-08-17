package com.cpf.education.online;

import com.cpf.file.objectstorage.api.CpfMultipartUpload;
import com.cpf.file.objectstorage.api.CpfObjectStorageMetadata;
import com.cpf.file.objectstorage.api.CpfObjectStorageOperations;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-16 File/Bulk 거래: Multipart upload→complete→download를 CPF Object Storage 계약으로 실행합니다. */
@CpfRestController
@RequestMapping("/edu/online/16-file")
public class Online16FileBulkExample {
    private final CpfObjectStorageOperations storage;

    public Online16FileBulkExample(CpfObjectStorageOperations storage) {
        this.storage = storage;
    }

    @PostMapping
    @Operation(operationId = "EDU-ONLINE-16", summary = "File Upload·Download·Bulk 거래")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-16",
            name = "File Upload·Download·Bulk 거래",
            description = "CPF Object Storage 계약으로 Multipart 업로드·완료·다운로드를 수행하고 Provider/SFTP 변형은 Starter로 교체한다.")
    /** uploadAndDownload 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public FileResult uploadAndDownload(@RequestBody Command command) throws IOException {
        if (command.content().length() > 1_000_000) throw new IllegalArgumentException("EDU file size policy exceeded");
        byte[] bytes = command.content().getBytes(StandardCharsets.UTF_8);
        CpfMultipartUpload upload = storage.beginMultipart(
                command.tenantId(), command.bucket(), command.objectKey(), "text/plain", Map.of("source", "EDU"));

        Map<Integer, String> etags = new LinkedHashMap<>();
        etags.put(1, storage.uploadPart(upload, 1, bytes));
        CpfObjectStorageMetadata metadata = storage.completeMultipart(upload, etags);

        byte[] downloaded;
        try (InputStream stream = storage.get(command.tenantId(), command.bucket(), command.objectKey(), 0, bytes.length)) {
            downloaded = stream.readAllBytes();
        }
        return new FileResult(metadata, downloaded.length, new String(downloaded, StandardCharsets.UTF_8));
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record Command(String tenantId, String bucket, String objectKey, String content) { }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record FileResult(CpfObjectStorageMetadata metadata, int downloadedBytes, String downloadedContent) { }
}
