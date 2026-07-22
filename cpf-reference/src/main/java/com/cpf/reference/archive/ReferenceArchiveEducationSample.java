package com.cpf.reference.archive;

import com.cpf.core.common.archive.CpfArchiveEntry;
import com.cpf.core.common.archive.CpfArchivePolicy;
import com.cpf.core.common.archive.CpfArchiveRequest;
import com.cpf.core.common.archive.CpfArchiveResult;
import com.cpf.core.common.archive.CpfArchiveService;
import com.cpf.core.common.archive.LocalCpfArchiveService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * REF 업무 파일을 CPF archive port로 압축하는 교육 샘플입니다.
 */
public class ReferenceArchiveEducationSample {
    private final CpfArchiveService archiveService;

    public ReferenceArchiveEducationSample() {
        this(new LocalCpfArchiveService());
    }

    public ReferenceArchiveEducationSample(CpfArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    public CpfArchiveResult createZip(Path baseDirectory) {
        CpfArchivePolicy policy = CpfArchivePolicy.local(baseDirectory);
        List<CpfArchiveEntry> entries = List.of(
                new CpfArchiveEntry("result/success.csv", "id,status\n1,SUCCESS\n".getBytes(StandardCharsets.UTF_8)),
                new CpfArchiveEntry("result/failure.csv", "id,status\n2,FAILED\n".getBytes(StandardCharsets.UTF_8)));
        return archiveService.create(CpfArchiveRequest.zip(baseDirectory.resolve("result.zip"), entries, policy));
    }
}
