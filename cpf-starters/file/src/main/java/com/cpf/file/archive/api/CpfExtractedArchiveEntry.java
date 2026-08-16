package com.cpf.file.archive.api;
import java.nio.file.Path;
/** 추출 결과는 본문 byte[]가 아니라 검증된 파일 Metadata만 반환합니다. */
public record CpfExtractedArchiveEntry(String name,Path path,long size,String checksumSha256){}
