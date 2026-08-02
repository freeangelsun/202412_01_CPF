package com.cpf.reference.archive;

import com.cpf.core.api.archive.CpfArchiveEntry;
import com.cpf.core.api.archive.CpfArchivePolicy;
import com.cpf.core.api.archive.CpfArchiveRequest;
import com.cpf.core.api.archive.CpfArchiveResult;
import com.cpf.core.api.archive.CpfArchiveService;
import com.cpf.core.api.archive.CpfArchives;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * REF 업무 파일을 CPF archive port로 압축하는 교육 샘플입니다.
 */
public class ReferenceArchiveEducationSample {
    private final CpfArchiveService archiveService;

    public ReferenceArchiveEducationSample() {
        this(CpfArchives.local());
    }

    public ReferenceArchiveEducationSample(CpfArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    public CpfArchiveResult createZip(Path baseDirectory) {
        CpfArchivePolicy policy = CpfArchivePolicy.local(baseDirectory);
        List<CpfArchiveEntry> entries = List.of(
                streamingEntry("result/success.csv", "id,status\n1,SUCCESS\n"),
                streamingEntry("result/failure.csv", "id,status\n2,FAILED\n"));
        return archiveService.create(CpfArchiveRequest.zip(baseDirectory.resolve("result.zip"), entries, policy));
    }

    private CpfArchiveEntry streamingEntry(String name, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return CpfArchiveEntry.streaming(name, bytes.length, () -> new ByteArrayInputStream(bytes));
    }
}
