package com.cpf.batch.edu.archive;

import com.cpf.core.common.archive.CpfArchiveEntry;
import com.cpf.core.common.archive.CpfArchivePolicy;
import com.cpf.core.common.archive.CpfArchiveRequest;
import com.cpf.core.common.archive.CpfArchiveResult;
import com.cpf.core.common.archive.LocalCpfArchiveService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * 배치 결과 파일을 CPF archive capability로 압축하는 샘플입니다.
 */
public class BatArchiveCompressionEducationSample {

    public CpfArchiveResult zipResultFile(Path baseDirectory) {
        return new LocalCpfArchiveService().create(CpfArchiveRequest.zip(
                baseDirectory.resolve("bat-result.zip"),
                List.of(new CpfArchiveEntry("result.csv", "id,result\n1,OK\n".getBytes(StandardCharsets.UTF_8))),
                CpfArchivePolicy.local(baseDirectory)));
    }
}
