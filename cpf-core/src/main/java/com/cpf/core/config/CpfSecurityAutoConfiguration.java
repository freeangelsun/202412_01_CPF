package com.cpf.core.config;

import com.cpf.core.channel.adapter.JdbcCpfChannelRegistryAdapter;
import com.cpf.core.channel.api.CpfChannelRegistryPort;
import com.cpf.core.channel.application.CpfChannelPolicyService;
import com.cpf.core.common.execution.CpfExecutionCatalogPort;
import com.cpf.core.common.execution.CpfExecutionCatalogRepository;
import com.cpf.core.common.execution.CpfExecutionCatalogScanner;
import com.cpf.core.common.security.password.CpfPasswordHashingPort;
import com.cpf.core.common.security.password.CpfPbkdf2PasswordHasher;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.cpf.core.common.remotelog.CpfRemoteLogArtifactPort;
import com.cpf.core.common.remotelog.CpfRemoteLogBundleJobPort;
import com.cpf.core.common.remotelog.CpfRemoteLogNode;
import com.cpf.core.common.remotelog.CpfRemoteLogNodeClientPort;
import com.cpf.core.common.remotelog.CpfRemoteLogNodeRegistryPort;
import com.cpf.core.common.remotelog.CpfRemoteLogServiceCredentialPort;
import com.cpf.core.common.remotelog.LocalCpfRemoteLogArtifactAdapter;
import com.cpf.core.common.remotelog.LocalCpfRemoteLogNodeClient;
import com.cpf.core.common.remotelog.LocalCpfRemoteLogNodeRegistry;
import com.cpf.core.common.remotelog.InMemoryCpfRemoteLogBundleJobAdapter;
import com.cpf.core.common.remotelog.RoutingCpfRemoteLogArtifactAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

/** CPF 보안과 표준 실행 catalog 공통 capability 자동 구성입니다. */
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
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
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
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
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
