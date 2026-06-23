package cpf.pfw.config;

import cpf.pfw.common.transaction.CpfTransactionMetaRepository;
import cpf.pfw.common.transaction.CpfTransactionMetaScanner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;

/**
 * CPF 온라인 거래 메타 자동 등록 구성입니다.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping")
public class CpfTransactionMetaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CpfTransactionMetaRepository cpfTransactionMetaRepository(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("pfwDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        return new CpfTransactionMetaRepository(jdbcTemplateProvider, dataSourceProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RequestMappingHandlerMapping.class)
    public CpfTransactionMetaScanner cpfTransactionMetaScanner(
            RequestMappingHandlerMapping handlerMapping,
            CpfTransactionMetaRepository repository) {
        return new CpfTransactionMetaScanner(handlerMapping, repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfTransactionMetaStartupScanner cpfTransactionMetaStartupScanner(
            ObjectProvider<CpfTransactionMetaScanner> scannerProvider) {
        return new CpfTransactionMetaStartupScanner(scannerProvider);
    }

    /**
     * 애플리케이션 준비 후 거래 메타를 한 번 스캔합니다.
     */
    public static class CpfTransactionMetaStartupScanner {
        private final ObjectProvider<CpfTransactionMetaScanner> scannerProvider;

        public CpfTransactionMetaStartupScanner(ObjectProvider<CpfTransactionMetaScanner> scannerProvider) {
            this.scannerProvider = scannerProvider;
        }

        @EventListener(ApplicationReadyEvent.class)
        public void scanOnReady() {
            CpfTransactionMetaScanner scanner = scannerProvider.getIfAvailable();
            if (scanner != null) {
                scanner.scanAndUpsert("PFW_STARTUP_SCAN");
            }
        }
    }
}
