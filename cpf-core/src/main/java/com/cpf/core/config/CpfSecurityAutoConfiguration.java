package cpf.pfw.config;

import cpf.pfw.channel.adapter.JdbcCpfChannelRegistryAdapter;
import cpf.pfw.channel.api.CpfChannelRegistryPort;
import cpf.pfw.channel.application.CpfChannelPolicyService;
import cpf.pfw.common.execution.CpfExecutionCatalogPort;
import cpf.pfw.common.execution.CpfExecutionCatalogRepository;
import cpf.pfw.common.execution.CpfExecutionCatalogScanner;
import cpf.pfw.common.security.password.CpfPasswordHashingPort;
import cpf.pfw.common.security.password.CpfPbkdf2PasswordHasher;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.common.remotelog.CpfRemoteLogArtifactPort;
import cpf.pfw.common.remotelog.CpfRemoteLogBundleJobPort;
import cpf.pfw.common.remotelog.CpfRemoteLogNode;
import cpf.pfw.common.remotelog.CpfRemoteLogNodeClientPort;
import cpf.pfw.common.remotelog.CpfRemoteLogNodeRegistryPort;
import cpf.pfw.common.remotelog.CpfRemoteLogServiceCredentialPort;
import cpf.pfw.common.remotelog.LocalCpfRemoteLogArtifactAdapter;
import cpf.pfw.common.remotelog.LocalCpfRemoteLogNodeClient;
import cpf.pfw.common.remotelog.LocalCpfRemoteLogNodeRegistry;
import cpf.pfw.common.remotelog.InMemoryCpfRemoteLogBundleJobAdapter;
import cpf.pfw.common.remotelog.RoutingCpfRemoteLogArtifactAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

/** PFW 보안과 표준 실행 catalog 공통 capability 자동 구성입니다. */
@AutoConfiguration
public class CpfSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CpfPasswordHashingPort cpfPasswordHashingPort(Environment environment) {
        int iterations = environment.getProperty(
                "cpf.security.password.pbkdf2.iterations",
                Integer.class,
                CpfPbkdf2PasswordHasher.DEFAULT_ITERATIONS);
        int keyBits = environment.getProperty(
                "cpf.security.password.pbkdf2.key-bits",
                Integer.class,
                CpfPbkdf2PasswordHasher.DEFAULT_KEY_BITS);
        String pepper = environment.getProperty("cpf.security.password.pepper", "");
        return new CpfPbkdf2PasswordHasher(iterations, keyBits, pepper.toCharArray());
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfExecutionCatalogPort cpfExecutionCatalogPort(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        return new CpfExecutionCatalogRepository(jdbcTemplateProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfExecutionCatalogScanner cpfExecutionCatalogScanner(
            ApplicationContext applicationContext,
            Environment environment,
            CpfExecutionCatalogPort catalogPort) {
        return new CpfExecutionCatalogScanner(applicationContext, environment, catalogPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfChannelRegistryPort cpfChannelRegistryPort(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        return new JdbcCpfChannelRegistryAdapter(jdbcTemplateProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfChannelPolicyService cpfChannelPolicyService(CpfChannelRegistryPort registryPort) {
        return new CpfChannelPolicyService(registryPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRemoteLogNodeRegistryPort cpfRemoteLogNodeRegistryPort(CpfFileLogWriter fileLogWriter) {
        String nodeId = fileLogWriter.environmentCode() + ":"
                + fileLogWriter.runtimeModuleCode() + ":" + fileLogWriter.instanceId();
        CpfRemoteLogNode localNode = new CpfRemoteLogNode(
                nodeId,
                fileLogWriter.environmentCode(),
                fileLogWriter.runtimeModuleCode(),
                fileLogWriter.runtimeModuleCode(),
                fileLogWriter.instanceId(),
                null,
                true,
                true,
                "LOCAL",
                "LOCAL");
        return new LocalCpfRemoteLogNodeRegistry(localNode);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRemoteLogServiceCredentialPort cpfRemoteLogServiceCredentialPort() {
        return node -> {
            if (node.local()) {
                return "";
            }
            throw new IllegalStateException("원격 로그 service token provider가 구성되지 않았습니다.");
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRemoteLogNodeClientPort cpfRemoteLogNodeClientPort(
            CpfFileLogWriter fileLogWriter,
            Environment environment) {
        String nodeId = fileLogWriter.environmentCode() + ":"
                + fileLogWriter.runtimeModuleCode() + ":" + fileLogWriter.instanceId();
        return new LocalCpfRemoteLogNodeClient(
                nodeId,
                new LocalCpfRemoteLogArtifactAdapter(fileLogWriter, environment));
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRemoteLogArtifactPort cpfRemoteLogArtifactPort(
            CpfRemoteLogNodeRegistryPort registryPort,
            CpfRemoteLogNodeClientPort clientPort,
            CpfRemoteLogServiceCredentialPort credentialPort,
            Environment environment) {
        return new RoutingCpfRemoteLogArtifactAdapter(
                registryPort, clientPort, credentialPort, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRemoteLogBundleJobPort cpfRemoteLogBundleJobPort(
            CpfRemoteLogArtifactPort artifactPort,
            Environment environment) {
        return new InMemoryCpfRemoteLogBundleJobAdapter(artifactPort, environment);
    }
}
