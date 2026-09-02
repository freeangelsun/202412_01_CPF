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
// CPF-FILELOG(platform-operations/observability)와 ADM-LOG(cpf-admin)는 CANONICAL_PRODUCT_REQUIREMENTS
// 상 CURRENT mandatory capability다. ADM의 mandatory Admin Route(remoteLogs)가 CpfRemoteLogArtifactPort
// 를 요구하는데, 이 Port 의 구현이 여기 하나뿐이면서 opt-in 이면 mandatory 계약을 설정으로 취소하는 셈이
// 된다. 실제로 1-WAS 가 그 이유로 기동하지 못했다. 기본 제공으로 두되 두 갈래 재정의는 계속 열어 둔다.
//   - 다른 Provider 가 Port 를 공급하면 아래 @ConditionalOnMissingBean 이 물러난다.
//   - 운영이 이 구현만 끄려면 cpf.remote-log.local.enabled=false 를 명시한다.
@ConditionalOnProperty(prefix = "cpf.remote-log.local", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class CpfRemoteLogLocalAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CpfRemoteLogArtifactPort.class)
    public LocalCpfRemoteLogArtifactAdapter cpfLocalRemoteLogArtifactPort(
            Environment environment, ObjectProvider<Clock> clockProvider) {
        // Log Root 의 Canonical Config Owner 는 cpf.logging 네임스페이스를 소유한
        // CpfApplicationLoggingProperties(cpf.logging.root, 기본 logs)다. 기존 폴백이었던
        // cpf.logging.file.base-path 는 그 properties 클래스의 필드가 아닌, 같은 네임스페이스에 얹힌
        // 중복 철자다. 소비자인 여기서 정본 소유자를 따르게 하고 동일 의미 키를 새로 만들지 않는다.
        // (cpf.remote-log.local.root 는 이 구현 전용 override 로만 남긴다.)
        String root = text(environment, "cpf.remote-log.local.root",
                environment.getProperty("cpf.logging.root",
                        environment.getProperty("cpf.logging.file.base-path")));
        // 이 AutoConfiguration 은 기본 제공이고 observability 는 사실상 모든 Runtime 에 전이된다.
        // 여기서 throw 하면 Log Root 를 선언하지 않은 Runtime 이 통째로 기동 실패한다.
        // Canonical Config Owner(CpfApplicationLoggingProperties.root)의 기본값과 같은 값으로 맞춘다.
        if (root == null) root = "logs";
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
