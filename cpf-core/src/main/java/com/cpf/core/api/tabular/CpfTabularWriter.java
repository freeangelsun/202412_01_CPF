package com.cpf.core.api.tabular;
/** CSV/XLSX Streaming Writer SPI입니다. */
public interface CpfTabularWriter {
    boolean supports(CpfTabularFormat format);
    String write(CpfTabularWriteRequest request);
}
