package com.cpf.integration.fixedlength;

import com.cpf.integration.fixedlength.api.*;
import com.cpf.integration.fixedlength.internal.DefaultCpfFixedLengthCodec;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** 고정길이 전문 Parser/Writer/Registry/Operations를 Starter 하나로 제공하는 AutoConfiguration입니다. */
@AutoConfiguration
public class CpfFixedLengthAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    ConcurrentHashMap<String,CpfFixedLengthCodec> cpfFixedLengthCodecRegistry() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    @ConditionalOnMissingBean
    CpfFixedLengthConverterRegistry cpfFixedLengthConverterRegistry() {
        return new CpfFixedLengthConverterRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    CpfFixedLengthLayoutRegistry cpfFixedLengthLayoutRegistry() {
        return new CpfFixedLengthLayoutRegistry();
    }

    /** Parser만 고객 확장해도 Writer 기본 구현까지 사라지지 않도록 독립 조건으로 제공합니다. */
    @Bean
    @ConditionalOnMissingBean(CpfFixedLengthParser.class)
    CpfFixedLengthParser cpfFixedLengthParser(CpfFixedLengthConverterRegistry converters) {
        return new DefaultCpfFixedLengthCodec(converters);
    }

    /** Writer만 고객 확장해도 Parser 기본 구현을 계속 사용할 수 있습니다. */
    @Bean
    @ConditionalOnMissingBean(CpfFixedLengthWriter.class)
    CpfFixedLengthWriter cpfFixedLengthWriter(CpfFixedLengthConverterRegistry converters) {
        return new DefaultCpfFixedLengthCodec(converters);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfFixedLengthOperations cpfFixedLengthOperations(
            CpfFixedLengthParser parser,
            CpfFixedLengthWriter writer,
            CpfFixedLengthLayoutRegistry layouts) {
        return new CpfFixedLengthOperations(parser, writer, layouts);
    }
}
