package com.cpf.education.file.archive;
import com.cpf.file.archive.api.CpfArchiveEntry;
import com.cpf.file.archive.api.CpfArchivePolicy;
import com.cpf.file.archive.api.CpfArchiveRequest;
import com.cpf.file.archive.api.CpfArchiveResult;
import com.cpf.file.archive.api.CpfArchiveService;
import com.cpf.file.archive.api.CpfArchives;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * EDU 업무 파일을 CPF archive port로 압축하는 교육 샘플입니다.
 */
public class EducationArchiveEducationSample {
    private final CpfArchiveService archiveService;

    public EducationArchiveEducationSample() {
        this(CpfArchives.local());
    }

    /** EducationArchiveEducationSample 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationArchiveEducationSample(CpfArchiveService archiveService) {
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
