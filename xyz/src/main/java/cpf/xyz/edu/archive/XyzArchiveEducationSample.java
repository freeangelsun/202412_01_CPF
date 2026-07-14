package cpf.xyz.edu.archive;

import cpf.pfw.common.archive.CpfArchiveEntry;
import cpf.pfw.common.archive.CpfArchivePolicy;
import cpf.pfw.common.archive.CpfArchiveRequest;
import cpf.pfw.common.archive.CpfArchiveResult;
import cpf.pfw.common.archive.CpfArchiveService;
import cpf.pfw.common.archive.LocalCpfArchiveService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * XYZ 업무 파일을 PFW archive port로 압축하는 교육 샘플입니다.
 */
public class XyzArchiveEducationSample {
    private final CpfArchiveService archiveService;

    public XyzArchiveEducationSample() {
        this(new LocalCpfArchiveService());
    }

    public XyzArchiveEducationSample(CpfArchiveService archiveService) {
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
