package com.cpf.starter.platform.operations.observability;

import com.cpf.core.api.tracking.CpfSubjectTrackingOperations;
import com.cpf.platform.operations.observability.api.tracking.CpfSubjectTimelineQueryPort;
import com.cpf.platform.operations.observability.internal.tracking.CpfSubjectTrackingProperties;
import com.cpf.platform.operations.observability.internal.tracking.JdbcCpfSubjectTrackingRepository;
import com.cpf.security.api.crypto.CpfCryptoOperations;
import java.time.Clock;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/** 기존 CPF Crypto와 CPF DataSource를 재사용해 Subject protected index를 활성화합니다. */
@AutoConfiguration
@ConditionalOnClass({DataSource.class, JdbcTemplate.class})
@ConditionalOnBean({DataSource.class, CpfCryptoOperations.class})
@EnableConfigurationProperties(CpfSubjectTrackingProperties.class)
public class CpfSubjectTrackingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean({CpfSubjectTrackingOperations.class, CpfSubjectTimelineQueryPort.class})
    JdbcCpfSubjectTrackingRepository cpfSubjectTrackingRepository(DataSource dataSource, CpfCryptoOperations crypto,
            CpfSubjectTrackingProperties properties, ObjectProvider<Clock> clocks) {
        return new JdbcCpfSubjectTrackingRepository(new JdbcTemplate(dataSource), crypto, properties,
                clocks.getIfUnique(Clock::systemUTC));
    }
}
