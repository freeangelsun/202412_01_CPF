package com.cpf.file.tabular.api;
/** CSV/XLSX Streaming Reader SPI입니다. */
public interface CpfTabularReader {
    boolean supports(CpfTabularFormat format);
    CpfTabularReadResult read(CpfTabularReadRequest request, CpfTabularRowConsumer consumer);
}
