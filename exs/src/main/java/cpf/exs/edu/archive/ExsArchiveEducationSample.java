package cpf.exs.edu.archive;

import cpf.pfw.common.archive.CpfArchiveResult;
import cpf.pfw.common.archive.PfwArchiveEducationSample;

import java.nio.file.Path;

/**
 * 대외 송신 파일 압축 후보를 PFW archive capability로 표현하는 샘플입니다.
 */
public class ExsArchiveEducationSample {

    public CpfArchiveResult outboundZip(Path baseDirectory) {
        return new PfwArchiveEducationSample().createBusinessZip(baseDirectory);
    }
}
