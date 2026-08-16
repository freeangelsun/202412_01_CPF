package com.cpf.starter.platform.operations.observability;

import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogArtifactPort;
import com.cpf.platform.operations.observability.CpfRemoteLogAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;

/** Opt-in local filesystem provider for the common remote-log port. */
@AutoConfiguration
@AutoConfigureBefore(CpfRemoteLogAutoConfiguration.class)
@ConditionalOnProperty(prefix = "cpf.remote-log.local", name = "enabled", havingValue = "true")
public class CpfRemoteLogLocalAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CpfRemoteLogArtifactPort.class)
    public LocalCpfRemoteLogArtifactAdapter cpfLocalRemoteLogArtifactPort(
            Environment environment, ObjectProvider<Clock> clockProvider) {
        String root = text(environment, "cpf.remote-log.local.root",
                environment.getProperty("cpf.logging.file.base-path"));
        if (root == null) throw new IllegalStateException("cpf.remote-log.local.root is required");
        String module = text(environment, "cpf.remote-log.local.module", "CPF");
        String service = text(environment, "cpf.remote-log.local.service", module);
        String instance = text(environment, "cpf.remote-log.local.instance", "local-1");
        LocalCpfRemoteLogArtifactAdapter.Settings defaults =
                LocalCpfRemoteLogArtifactAdapter.Settings.defaults(
                        text(environment, "cpf.environment", "local"), module, service, instance);
        LocalCpfRemoteLogArtifactAdapter.Settings settings = new LocalCpfRemoteLogArtifactAdapter.Settings(
                defaults.environment(), defaults.module(), defaults.service(), defaults.instance(),
                environment.getProperty("cpf.remote-log.local.retention", Duration.class, defaults.retention()),
                environment.getProperty("cpf.remote-log.local.active-window", Duration.class, defaults.activeWindow()),
                environment.getProperty("cpf.remote-log.local.bundle-ttl", Duration.class, defaults.bundleTimeToLive()),
                environment.getProperty("cpf.remote-log.local.maximum-depth", Integer.class, defaults.maximumDepth()),
                environment.getProperty("cpf.remote-log.local.maximum-scanned-files", Integer.class, defaults.maximumScannedFiles()),
                environment.getProperty("cpf.remote-log.local.maximum-preview-lines", Integer.class, defaults.maximumPreviewLines()),
                environment.getProperty("cpf.remote-log.local.maximum-search-bytes", Long.class, defaults.maximumSearchBytes()),
                environment.getProperty("cpf.remote-log.local.maximum-preview-bytes", Long.class, defaults.maximumPreviewBytes()),
                environment.getProperty("cpf.remote-log.local.maximum-preview-decoded-characters", Long.class, defaults.maximumPreviewDecodedCharacters()),
                environment.getProperty("cpf.remote-log.local.maximum-raw-line-characters", Integer.class, defaults.maximumRawLineCharacters()),
                environment.getProperty("cpf.remote-log.local.maximum-download-bytes", Long.class, defaults.maximumDownloadBytes()),
                environment.getProperty("cpf.remote-log.local.maximum-bundle-bytes", Long.class, defaults.maximumBundleBytes()),
                environment.getProperty("cpf.remote-log.local.maximum-bundle-artifacts", Integer.class, defaults.maximumBundleArtifacts()),
                environment.getProperty("cpf.remote-log.local.source-already-masked", Boolean.class, defaults.sourceAlreadyMasked()));
        return new LocalCpfRemoteLogArtifactAdapter(
                Path.of(root), clockProvider.getIfUnique(Clock::systemUTC), settings);
    }

    private static String text(Environment environment, String key, String defaultValue) {
        String value = environment.getProperty(key, defaultValue);
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(key + " contains a control character");
        }
        return normalized;
    }
}
