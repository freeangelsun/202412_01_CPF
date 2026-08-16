package com.cpf.file.archive.api;
import java.nio.file.Path;import java.util.List;
/** CPF 압축 capability의 bounded streaming Port입니다. */
public interface CpfArchiveService{
 CpfArchiveResult create(CpfArchiveRequest request);
 List<CpfExtractedArchiveEntry> extract(Path archivePath,CpfArchiveFormat format,Path targetDirectory,CpfArchivePolicy policy);
}
