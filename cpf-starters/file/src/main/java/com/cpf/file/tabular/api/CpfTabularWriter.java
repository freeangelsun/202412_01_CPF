package com.cpf.file.tabular.api;
/** CSV/XLSX Streaming Writer SPI입니다. */
public interface CpfTabularWriter {
    boolean supports(CpfTabularFormat format);
    String write(CpfTabularWriteRequest request);
}
