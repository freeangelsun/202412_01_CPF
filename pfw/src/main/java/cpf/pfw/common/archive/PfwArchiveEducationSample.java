package cpf.pfw.common.archive;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * PFW 압축 capability를 업무 모듈에서 호출하는 방식의 교육 샘플입니다.
 */
public class PfwArchiveEducationSample {
    private final CpfArchiveService archiveService;

    public PfwArchiveEducationSample() {
        this(new LocalCpfArchiveService());
    }

    public PfwArchiveEducationSample(CpfArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    /**
     * 업무 결과 파일 여러 개를 ZIP으로 묶는 계획을 생성합니다.
     */
    public CpfArchiveResult createBusinessZip(Path baseDirectory) {
        CpfArchivePolicy policy = CpfArchivePolicy.local(baseDirectory);
        List<CpfArchiveEntry> entries = List.of(
                new CpfArchiveEntry("result/success.csv", "id,status\n1,SUCCESS\n".getBytes(StandardCharsets.UTF_8)),
                new CpfArchiveEntry("result/failure.csv", "id,status\n2,FAILED\n".getBytes(StandardCharsets.UTF_8)));
        return archiveService.create(CpfArchiveRequest.zip(baseDirectory.resolve("result.zip"), entries, policy));
    }
}
