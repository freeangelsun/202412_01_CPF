package cpf.pfw.common.archive;

import java.nio.file.Path;
import java.util.List;

/**
 * PFW 압축/보관 capability의 표준 port입니다.
 */
public interface CpfArchiveService {
    CpfArchiveResult create(CpfArchiveRequest request);

    List<CpfArchiveEntry> extract(Path archivePath, CpfArchiveFormat format, Path targetDirectory, CpfArchivePolicy policy);
}
