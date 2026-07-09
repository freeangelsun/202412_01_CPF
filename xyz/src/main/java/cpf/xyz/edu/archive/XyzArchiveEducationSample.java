package cpf.xyz.edu.archive;

import cpf.pfw.common.archive.CpfArchiveResult;
import cpf.pfw.common.archive.PfwArchiveEducationSample;

import java.nio.file.Path;

/**
 * XYZ 업무 파일을 PFW archive capability로 압축하는 샘플입니다.
 */
public class XyzArchiveEducationSample {

    public CpfArchiveResult createZip(Path baseDirectory) {
        return new PfwArchiveEducationSample().createBusinessZip(baseDirectory);
    }
}
