package com.cpf.integration.fixedlength;

import com.cpf.integration.fixedlength.api.*;
import com.cpf.integration.fixedlength.internal.DefaultCpfFixedLengthCodec;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Qualifier;
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

    /**
     * Parser/Writer 주입 대상을 Bean 이름으로 명시합니다.
     *
     * <p>기본 구현 {@code DefaultCpfFixedLengthCodec} 은 {@link CpfFixedLengthParser} 와
     * {@link CpfFixedLengthWriter} 를 **함께** 구현한다. 그래서 위 두 {@code @Bean} 은 각각
     * 두 타입을 모두 만족하는 인스턴스를 만들고, 타입만으로 주입하면 후보가 2개가 된다.
     * 파라미터 이름({@code parser}/{@code writer})과 같은 Bean 이 없어 1-WAS 합성 기동이
     * {@code expected single matching bean but found 2:
     * cpfFixedLengthParser,cpfFixedLengthWriter} 로 실패했다.</p>
     *
     * <p>{@code @ConditionalOnMissingBean(CpfFixedLengthParser.class)} 는 선언 타입만 보므로
     * 이 상황을 막지 못한다. 고객이 Parser 만 확장해도 Writer 기본 구현이 살아 있어야 하는
     * 위 설계는 그대로 두고, 소비 지점에서 해석 근거를 명시한다(Harness §25.4).</p>
     */
    @Bean
    @ConditionalOnMissingBean
    CpfFixedLengthOperations cpfFixedLengthOperations(
            @Qualifier("cpfFixedLengthParser") CpfFixedLengthParser parser,
            @Qualifier("cpfFixedLengthWriter") CpfFixedLengthWriter writer,
            CpfFixedLengthLayoutRegistry layouts) {
        return new CpfFixedLengthOperations(parser, writer, layouts);
    }
}
