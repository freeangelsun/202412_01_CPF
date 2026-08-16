package com.cpf.file.tabular;

import com.cpf.file.tabular.api.CpfTabularReader;
import com.cpf.file.tabular.api.CpfTabularWriter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/** CSV/XLSX 표준 Streaming Adapter를 등록합니다. */
@AutoConfiguration
public class CpfTabularAutoConfiguration {
    @Bean CpfCsvTabularAdapter cpfCsvTabularAdapter(){return new CpfCsvTabularAdapter();}
    @Bean CpfXlsxTabularAdapter cpfXlsxTabularAdapter(){return new CpfXlsxTabularAdapter();}
}
