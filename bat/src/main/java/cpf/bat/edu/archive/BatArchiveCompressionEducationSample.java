package cpf.bat.edu.archive;

import cpf.pfw.common.archive.CpfArchiveEntry;
import cpf.pfw.common.archive.CpfArchivePolicy;
import cpf.pfw.common.archive.CpfArchiveRequest;
import cpf.pfw.common.archive.CpfArchiveResult;
import cpf.pfw.common.archive.LocalCpfArchiveService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * 배치 결과 파일을 PFW archive capability로 압축하는 샘플입니다.
 */
public class BatArchiveCompressionEducationSample {

    public CpfArchiveResult zipResultFile(Path baseDirectory) {
        return new LocalCpfArchiveService().create(CpfArchiveRequest.zip(
                baseDirectory.resolve("bat-result.zip"),
                List.of(new CpfArchiveEntry("result.csv", "id,result\n1,OK\n".getBytes(StandardCharsets.UTF_8))),
                CpfArchivePolicy.local(baseDirectory)));
    }
}
