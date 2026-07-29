package com.cpf.core.api.tabular;
/** CSV/XLSX Streaming Reader SPI입니다. */
public interface CpfTabularReader {
    boolean supports(CpfTabularFormat format);
    CpfTabularReadResult read(CpfTabularReadRequest request, CpfTabularRowConsumer consumer);
}
