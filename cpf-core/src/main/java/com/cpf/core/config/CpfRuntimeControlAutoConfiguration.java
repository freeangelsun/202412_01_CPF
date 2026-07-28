package com.cpf.core.config;

import com.cpf.core.api.runtimecontrol.CpfRuntimeAgentPort;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayoutRegistry;
import com.cpf.core.common.runtimecontrol.applier.CpfFixedLayoutRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfSchemaRegistryRuntimeApplier;
import com.cpf.core.common.attachment.CpfAttachmentRuntimePolicy;
import com.cpf.core.common.attachment.CpfAttachmentStoragePort;
import com.cpf.core.common.runtimecontrol.applier.CpfAttachmentPolicyRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfDownloadPolicyRuntimeApplier;
import com.cpf.core.common.filetransfer.CpfFileTransferRuntimeState;
import com.cpf.core.common.filetransfer.CpfFileTransferEngine;
import com.cpf.core.common.filetransfer.CpfFileTransferPort;
import com.cpf.core.common.filetransfer.CpfFileTransferHistoryPort;
import com.cpf.core.common.filetransfer.CpfDuplicatePreventionPort;
import com.cpf.core.common.filetransfer.CpfFileInspectionPort;
import com.cpf.core.common.reconciliation.CpfReconciliationPort;
import com.cpf.core.common.runtimecontrol.applier.CpfFilePolicyRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfSftpTransferRuntimeApplier;
import com.cpf.core.api.security.runtime.CpfCertificateReloadPort;
import com.cpf.core.api.security.runtime.CpfSecretReferenceReloadPort;
import com.cpf.core.api.security.runtime.CpfJwtKeyReloadPort;
import com.cpf.core.api.security.runtime.CpfEncryptionKeyReloadPort;
import com.cpf.core.common.security.password.CpfPasswordRuntimePolicy;
import com.cpf.core.common.runtimecontrol.applier.CpfPasswordPolicyRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfSecurityMaterialRuntimeApplier;
import com.cpf.core.api.broker.CpfBrokerConsumerControlPort;
import com.cpf.core.common.broker.CpfBrokerConsumerRuntimePolicy;
import com.cpf.core.common.broker.CpfBrokerConsumerWorker;
import com.cpf.core.common.broker.CpfBrokerDlqPort;
import com.cpf.core.common.broker.CpfBrokerInboxPort;
import com.cpf.core.common.runtimecontrol.applier.CpfBrokerConsumerRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfBrokerRetryDlqRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfDbReadRoutingRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfConnectionPoolRuntimeApplier;
import com.cpf.core.common.database.CpfConnectionPoolRuntimeController;
import com.cpf.core.common.database.CpfReadRoutingRuntimePolicy;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeControlPlane;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceRegistration;
import com.cpf.core.common.runtimecontrol.CpfRuntimeControlAgent;
import com.cpf.core.common.runtimecontrol.CpfRuntimeControlPlaneRepository;
import com.cpf.core.common.runtimecontrol.CpfRuntimeControlPlaneService;
import com.cpf.core.common.runtimecontrol.CpfRuntimeControlReconciler;
import com.cpf.core.common.runtimecontrol.CpfRuntimeHttpControlPlaneClient;
import com.cpf.core.common.runtimecontrol.CpfRuntimeInstanceInboxStore;
import com.cpf.core.common.runtimecontrol.applier.CpfChannelPolicyRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfDynamicLogLevelRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfMaskingPolicyRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfTraceSamplingRuntimeApplier;
import com.cpf.core.common.reconciliation.CpfReconciliationPort;
import com.cpf.core.common.reconciliation.CpfReconciliationWorkPort;
import com.cpf.core.common.reconciliation.CpfReconciliationProbePort;
import com.cpf.core.common.reconciliation.CpfReconciliationRuntimePolicy;
import com.cpf.core.common.reconciliation.CpfReconciliationWorker;
import com.cpf.core.common.runtimecontrol.applier.CpfReconciliationRuntimeApplier;
import com.cpf.core.api.http.CpfWebhookSignaturePort;
import com.cpf.core.common.http.CpfWebhookRuntimePolicy;
import com.cpf.core.common.http.CpfWebhookCallbackClient;
import com.cpf.core.common.runtimecontrol.applier.CpfWebhookCallbackRuntimeApplier;
import com.cpf.core.common.http.CpfServiceEndpointRegistry;
import com.cpf.core.common.http.CpfApiClientRuntimePolicy;
import com.cpf.core.common.runtimecontrol.applier.CpfExternalInstitutionRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfApiClientRuntimeApplier;
import com.cpf.core.common.runtimecontrol.applier.CpfServiceRegistryRuntimeVerifierApplier;
import com.cpf.core.common.servicecall.CpfServiceRegistryRepository;
import com.cpf.core.common.logging.DynamicTransactionLogLevelService;
import com.cpf.core.common.logging.CpfTraceSamplingPolicy;
import com.cpf.core.channel.application.CpfChannelPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import java.util.ArrayList;

/** Runtime Control Plane 자동 구성입니다. DB Owner는 cpfDB로 고정합니다. */
@AutoConfiguration
public class CpfRuntimeControlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "cpfJdbcTemplate")
    public CpfRuntimeControlPlaneRepository cpfRuntimeControlPlaneRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcProvider,
            ObjectMapper objectMapper) {
        return new CpfRuntimeControlPlaneRepository(jdbcProvider, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CpfRuntimeControlPlaneRepository.class)
    public CpfRuntimeControlPlane cpfRuntimeControlPlane(CpfRuntimeControlPlaneRepository repository) {
        return new CpfRuntimeControlPlaneService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CpfRuntimeControlPlaneRepository.class)
    public CpfRuntimeControlReconciler cpfRuntimeControlReconciler(
            CpfRuntimeControlPlaneRepository repository,
            CpfRuntimeControlPlane controlPlane,
            @Value("${cpf.runtime.control.controller-id:${cpf.runtime.instance-id:${cpf.framework.was-id:CPF-CONTROLLER}}}") String controllerId,
            @Value("${cpf.runtime.control.controller.lease-seconds:30}") int leaseSeconds,
            @Value("${cpf.runtime.control.controller.ack-timeout-seconds:60}") int ackTimeoutSeconds) {
        return new CpfRuntimeControlReconciler(repository, controlPlane, controllerId, leaseSeconds, ackTimeoutSeconds);
    }

    @Bean
    @ConditionalOnMissingBean(CpfRuntimeAgentPort.class)
    @ConditionalOnProperty(prefix = "cpf.runtime.control", name = "base-url")
    public CpfRuntimeAgentPort cpfRuntimeRemoteControlPlane(
            RestClient.Builder builder,
            @Value("${cpf.runtime.control.base-url}") String baseUrl,
            @Value("${cpf.runtime.control.agent-token:}") String agentToken) {
        if (agentToken == null || agentToken.isBlank()) {
            throw new IllegalStateException("분리 WAS Runtime Agent에는 cpf.runtime.control.agent-token이 필요합니다.");
        }
        return new CpfRuntimeHttpControlPlaneClient(builder.baseUrl(baseUrl).build(), agentToken);
    }



    @Bean
    @ConditionalOnMissingBean
    public CpfFixedLengthLayoutRegistry cpfFixedLengthLayoutRegistry() {
        return new CpfFixedLengthLayoutRegistry();
    }

    @Bean(name = "cpfFixedLayoutRuntimeApplier")
    @ConditionalOnBean(CpfFixedLengthLayoutRegistry.class)
    @ConditionalOnMissingBean(name = "cpfFixedLayoutRuntimeApplier")
    public CpfRuntimeChangeApplier cpfFixedLayoutRuntimeApplier(CpfFixedLengthLayoutRegistry registry) {
        return new CpfFixedLayoutRuntimeApplier(registry);
    }

    @Bean(name = "cpfSchemaRegistryRuntimeApplier")
    @ConditionalOnBean(CpfFixedLengthLayoutRegistry.class)
    @ConditionalOnMissingBean(name = "cpfSchemaRegistryRuntimeApplier")
    public CpfRuntimeChangeApplier cpfSchemaRegistryRuntimeApplier(CpfFixedLengthLayoutRegistry registry) {
        return new CpfSchemaRegistryRuntimeApplier(registry);
    }





    @Bean
    @ConditionalOnMissingBean
    public CpfReconciliationRuntimePolicy cpfReconciliationRuntimePolicy() { return new CpfReconciliationRuntimePolicy(); }

    @Bean(name = "cpfReconciliationRuntimeApplier")
    @ConditionalOnBean(CpfReconciliationRuntimePolicy.class)
    @ConditionalOnMissingBean(name = "cpfReconciliationRuntimeApplier")
    public CpfRuntimeChangeApplier cpfReconciliationRuntimeApplier(CpfReconciliationRuntimePolicy policy) {
        return new CpfReconciliationRuntimeApplier(policy);
    }

    @Bean
    @ConditionalOnBean({CpfReconciliationPort.class, CpfReconciliationWorkPort.class})
    @ConditionalOnMissingBean
    public CpfReconciliationWorker cpfReconciliationWorker(
            CpfReconciliationPort port,
            CpfReconciliationWorkPort workPort,
            CpfReconciliationRuntimePolicy policy,
            ObjectProvider<CpfReconciliationProbePort> probes,
            @Value("${cpf.runtime.instance-id:${cpf.framework.was-id:CPF-RECONCILIATION}}") String workerId) {
        return new CpfReconciliationWorker(port, workPort, policy, probes.orderedStream().toList(), workerId);
    }

    @Bean(name = "cpfWebhookCallbackRuntimeApplier")
    @ConditionalOnBean({CpfWebhookRuntimePolicy.class, CpfWebhookCallbackClient.class})
    @ConditionalOnMissingBean(name = "cpfWebhookCallbackRuntimeApplier")
    public CpfRuntimeChangeApplier cpfWebhookCallbackRuntimeApplier(
            CpfWebhookRuntimePolicy policy,
            ObjectProvider<CpfWebhookSignaturePort> signatureProvider) {
        return new CpfWebhookCallbackRuntimeApplier(policy, signatureProvider.getIfAvailable());
    }

    @Bean(name = "cpfExternalInstitutionRuntimeApplier")
    @ConditionalOnBean(CpfServiceEndpointRegistry.class)
    @ConditionalOnMissingBean(name = "cpfExternalInstitutionRuntimeApplier")
    public CpfRuntimeChangeApplier cpfExternalInstitutionRuntimeApplier(
            CpfServiceEndpointRegistry registry,
            ObjectProvider<CpfFixedLengthLayoutRegistry> layoutRegistryProvider) {
        return new CpfExternalInstitutionRuntimeApplier(registry, layoutRegistryProvider.getIfAvailable());
    }

    @Bean(name = "cpfApiClientRuntimeApplier")
    @ConditionalOnBean(CpfApiClientRuntimePolicy.class)
    @ConditionalOnMissingBean(name = "cpfApiClientRuntimeApplier")
    public CpfRuntimeChangeApplier cpfApiClientRuntimeApplier(CpfApiClientRuntimePolicy policy) {
        return new CpfApiClientRuntimeApplier(policy);
    }

    @Bean(name = "cpfServiceRouteRuntimeApplier")
    @ConditionalOnBean(CpfServiceRegistryRepository.class)
    @ConditionalOnMissingBean(name = "cpfServiceRouteRuntimeApplier")
    public CpfRuntimeChangeApplier cpfServiceRouteRuntimeApplier(CpfServiceRegistryRepository repository) {
        return new CpfServiceRegistryRuntimeVerifierApplier("SERVICE_ROUTE", repository);
    }

    @Bean(name = "cpfCircuitRuntimeApplier")
    @ConditionalOnBean(CpfServiceRegistryRepository.class)
    @ConditionalOnMissingBean(name = "cpfCircuitRuntimeApplier")
    public CpfRuntimeChangeApplier cpfCircuitRuntimeApplier(CpfServiceRegistryRepository repository) {
        return new CpfServiceRegistryRuntimeVerifierApplier("CIRCUIT", repository);
    }

    @Bean(name = "cpfMaintenanceRuntimeApplier")
    @ConditionalOnBean(CpfServiceRegistryRepository.class)
    @ConditionalOnMissingBean(name = "cpfMaintenanceRuntimeApplier")
    public CpfRuntimeChangeApplier cpfMaintenanceRuntimeApplier(CpfServiceRegistryRepository repository) {
        return new CpfServiceRegistryRuntimeVerifierApplier("MAINTENANCE", repository);
    }

    @Bean(name = "cpfChannelPolicyRuntimeApplier")
    @ConditionalOnBean(CpfChannelPolicyService.class)
    @ConditionalOnMissingBean(name = "cpfChannelPolicyRuntimeApplier")
    public CpfRuntimeChangeApplier cpfChannelPolicyRuntimeApplier(CpfChannelPolicyService policyService) {
        return new CpfChannelPolicyRuntimeApplier(policyService);
    }


    @Bean(name="cpfAttachmentPolicyRuntimeApplier")
    @ConditionalOnBean({CpfAttachmentRuntimePolicy.class, CpfAttachmentStoragePort.class})
    @ConditionalOnMissingBean(name="cpfAttachmentPolicyRuntimeApplier")
    public CpfRuntimeChangeApplier cpfAttachmentPolicyRuntimeApplier(CpfAttachmentRuntimePolicy policy){return new CpfAttachmentPolicyRuntimeApplier(policy);}

    @Bean(name="cpfDownloadPolicyRuntimeApplier")
    @ConditionalOnBean({CpfAttachmentRuntimePolicy.class, CpfAttachmentStoragePort.class})
    @ConditionalOnMissingBean(name="cpfDownloadPolicyRuntimeApplier")
    public CpfRuntimeChangeApplier cpfDownloadPolicyRuntimeApplier(CpfAttachmentRuntimePolicy policy){return new CpfDownloadPolicyRuntimeApplier(policy);}

    @Bean
    @ConditionalOnMissingBean
    public CpfFileTransferRuntimeState cpfFileTransferRuntimeState() { return new CpfFileTransferRuntimeState(); }

    @Bean
    @ConditionalOnBean({CpfFileTransferPort.class, CpfFileTransferHistoryPort.class, CpfDuplicatePreventionPort.class})
    @ConditionalOnMissingBean
    public CpfFileTransferEngine cpfFileTransferEngine(
            CpfFileTransferPort transferPort,
            CpfFileTransferHistoryPort historyPort,
            CpfDuplicatePreventionPort duplicatePort,
            ObjectProvider<CpfReconciliationPort> reconciliationProvider,
            CpfFileTransferRuntimeState state,
            ObjectProvider<CpfFileInspectionPort> inspectionProvider) {
        return new CpfFileTransferEngine(transferPort, historyPort, duplicatePort,
                reconciliationProvider.getIfAvailable(), state, inspectionProvider.getIfAvailable());
    }

    @Bean(name="cpfFilePolicyRuntimeApplier")
    @ConditionalOnBean(CpfFileTransferEngine.class)
    @ConditionalOnMissingBean(name="cpfFilePolicyRuntimeApplier")
    public CpfRuntimeChangeApplier cpfFilePolicyRuntimeApplier(CpfFileTransferRuntimeState state){return new CpfFilePolicyRuntimeApplier(state);}

    @Bean(name="cpfSftpTransferRuntimeApplier")
    @ConditionalOnBean(CpfFileTransferEngine.class)
    @ConditionalOnMissingBean(name="cpfSftpTransferRuntimeApplier")
    public CpfRuntimeChangeApplier cpfSftpTransferRuntimeApplier(CpfFileTransferRuntimeState state){return new CpfSftpTransferRuntimeApplier(state);}

    @Bean(name = "cpfPasswordPolicyRuntimeApplier")
    @ConditionalOnBean(CpfPasswordRuntimePolicy.class)
    @ConditionalOnMissingBean(name = "cpfPasswordPolicyRuntimeApplier")
    public CpfRuntimeChangeApplier cpfPasswordPolicyRuntimeApplier(CpfPasswordRuntimePolicy policy) {
        return new CpfPasswordPolicyRuntimeApplier(policy);
    }

    @Bean(name = "cpfCertificateRuntimeApplier")
    @ConditionalOnBean(CpfCertificateReloadPort.class)
    @ConditionalOnMissingBean(name = "cpfCertificateRuntimeApplier")
    public CpfRuntimeChangeApplier cpfCertificateRuntimeApplier(CpfCertificateReloadPort port) {
        return new CpfSecurityMaterialRuntimeApplier("CERTIFICATE", port::reloadCertificates);
    }

    @Bean(name = "cpfSecretReferenceRuntimeApplier")
    @ConditionalOnBean(CpfSecretReferenceReloadPort.class)
    @ConditionalOnMissingBean(name = "cpfSecretReferenceRuntimeApplier")
    public CpfRuntimeChangeApplier cpfSecretReferenceRuntimeApplier(CpfSecretReferenceReloadPort port) {
        return new CpfSecurityMaterialRuntimeApplier("SECRET_REFERENCE", port::reloadSecretReferences);
    }

    @Bean(name = "cpfJwtKeyRuntimeApplier")
    @ConditionalOnBean(CpfJwtKeyReloadPort.class)
    @ConditionalOnMissingBean(name = "cpfJwtKeyRuntimeApplier")
    public CpfRuntimeChangeApplier cpfJwtKeyRuntimeApplier(CpfJwtKeyReloadPort port) {
        return new CpfSecurityMaterialRuntimeApplier("JWT_KEY", port::reloadJwtKeys);
    }

    @Bean(name = "cpfEncryptionKeyRuntimeApplier")
    @ConditionalOnBean(CpfEncryptionKeyReloadPort.class)
    @ConditionalOnMissingBean(name = "cpfEncryptionKeyRuntimeApplier")
    public CpfRuntimeChangeApplier cpfEncryptionKeyRuntimeApplier(CpfEncryptionKeyReloadPort port) {
        return new CpfSecurityMaterialRuntimeApplier("ENCRYPTION_KEY", port::reloadEncryptionKeys);
    }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.db.connection-pool.runtime-control", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public CpfConnectionPoolRuntimeController cpfConnectionPoolRuntimeController(
            @Qualifier("cpfPrimaryDataSource") DataSource primary,
            @Qualifier("cpfReplicaDataSource") ObjectProvider<DataSource> replicaProvider) {
        ArrayList<DataSource> managed = new ArrayList<>();
        managed.add(primary);
        DataSource replica = replicaProvider.getIfAvailable();
        if (replica != null) managed.add(replica);
        return new CpfConnectionPoolRuntimeController(managed);
    }

    @Bean(name = "cpfConnectionPoolRuntimeApplier")
    @ConditionalOnBean(CpfConnectionPoolRuntimeController.class)
    @ConditionalOnMissingBean(name = "cpfConnectionPoolRuntimeApplier")
    public CpfRuntimeChangeApplier cpfConnectionPoolRuntimeApplier(CpfConnectionPoolRuntimeController controller) {
        return new CpfConnectionPoolRuntimeApplier(controller);
    }

    @Bean(name = "cpfDbReadRoutingRuntimeApplier")
    @ConditionalOnProperty(prefix = "cpf.db.read-routing", name = "enabled", havingValue = "true")
    @ConditionalOnBean(CpfReadRoutingRuntimePolicy.class)
    @ConditionalOnMissingBean(name = "cpfDbReadRoutingRuntimeApplier")
    public CpfRuntimeChangeApplier cpfDbReadRoutingRuntimeApplier(CpfReadRoutingRuntimePolicy policy) {
        return new CpfDbReadRoutingRuntimeApplier(policy);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfBrokerConsumerRuntimePolicy cpfBrokerConsumerRuntimePolicy() {
        return new CpfBrokerConsumerRuntimePolicy();
    }

    @Bean
    @ConditionalOnBean({CpfBrokerInboxPort.class, CpfBrokerDlqPort.class})
    @ConditionalOnMissingBean
    public CpfBrokerConsumerWorker cpfBrokerConsumerWorker(
            CpfBrokerInboxPort inboxPort,
            CpfBrokerDlqPort dlqPort,
            CpfBrokerConsumerRuntimePolicy policy) {
        return new CpfBrokerConsumerWorker(inboxPort, dlqPort, policy);
    }

    @Bean(name = "cpfBrokerConsumerRuntimeApplier")
    @ConditionalOnBean(CpfBrokerConsumerWorker.class)
    @ConditionalOnMissingBean(name = "cpfBrokerConsumerRuntimeApplier")
    public CpfRuntimeChangeApplier cpfBrokerConsumerRuntimeApplier(
            CpfBrokerConsumerRuntimePolicy policy,
            ObjectProvider<CpfBrokerConsumerControlPort> transportControlProvider) {
        return new CpfBrokerConsumerRuntimeApplier(policy, transportControlProvider.getIfAvailable());
    }

    @Bean(name = "cpfBrokerRetryDlqRuntimeApplier")
    @ConditionalOnBean(CpfBrokerConsumerWorker.class)
    @ConditionalOnMissingBean(name = "cpfBrokerRetryDlqRuntimeApplier")
    public CpfRuntimeChangeApplier cpfBrokerRetryDlqRuntimeApplier(CpfBrokerConsumerRuntimePolicy policy) {
        return new CpfBrokerRetryDlqRuntimeApplier(policy);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfTraceSamplingPolicy cpfTraceSamplingPolicy() {
        return new CpfTraceSamplingPolicy();
    }

    @Bean(name = "cpfTraceSamplingRuntimeApplier")
    @ConditionalOnBean(CpfTraceSamplingPolicy.class)
    @ConditionalOnMissingBean(name = "cpfTraceSamplingRuntimeApplier")
    public CpfRuntimeChangeApplier cpfTraceSamplingRuntimeApplier(CpfTraceSamplingPolicy policy) {
        return new CpfTraceSamplingRuntimeApplier(policy);
    }

    @Bean(name = "cpfDynamicLogLevelRuntimeApplier")
    @ConditionalOnBean(DynamicTransactionLogLevelService.class)
    @ConditionalOnMissingBean(name = "cpfDynamicLogLevelRuntimeApplier")
    public CpfRuntimeChangeApplier cpfDynamicLogLevelRuntimeApplier(DynamicTransactionLogLevelService service) {
        return new CpfDynamicLogLevelRuntimeApplier(service);
    }

    @Bean(name = "cpfMaskingPolicyRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfMaskingPolicyRuntimeApplier")
    public CpfRuntimeChangeApplier cpfMaskingPolicyRuntimeApplier() {
        return new CpfMaskingPolicyRuntimeApplier();
    }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.runtime.control.agent", name = "enabled", havingValue = "true")
    public CpfRuntimeInstanceInboxStore cpfRuntimeInstanceInboxStore(
            @Value("${cpf.runtime.control.agent.inbox-path:${java.io.tmpdir}/cpf-runtime-inbox}") String inboxPath,
            @Value("${cpf.runtime.instance-id:${cpf.framework.was-id:}}") String instanceId) {
        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalStateException("Runtime Agent inbox에는 instance-id가 필요합니다.");
        }
        return new CpfRuntimeInstanceInboxStore(Path.of(inboxPath, instanceId));
    }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.runtime.control.agent", name = "enabled", havingValue = "true")
    public CpfRuntimeControlAgent cpfRuntimeControlAgent(
            CpfRuntimeAgentPort controlPlane,
            ObjectProvider<CpfRuntimeChangeApplier> appliers,
            CpfRuntimeInstanceInboxStore inbox,
            @Value("${cpf.runtime.instance-id:${cpf.framework.was-id:}}") String instanceId,
            @Value("${cpf.runtime.service-id:${cpf.framework.module-id:}}") String serviceId,
            @Value("${cpf.runtime.endpoint-code:${cpf.framework.module-id:}}") String endpointCode,
            @Value("${cpf.runtime.base-url:}") String baseUrl,
            @Value("${cpf.runtime.environment:${spring.profiles.active:default}}") String environment,
            @Value("${cpf.runtime.zone:}") String zone,
            @Value("${cpf.runtime.cell:}") String cell,
            @Value("${cpf.runtime.artifact-version:${CPF_ARTIFACT_VERSION:unknown}}") String artifactVersion,
            @Value("${cpf.runtime.artifact-commit:${CPF_ARTIFACT_COMMIT:unknown}}") String artifactCommit,
            @Value("${cpf.runtime.role:APPLICATION}") String runtimeRole,
            @Value("${cpf.runtime.registration-source:${CPF_RUNTIME_REGISTRATION_SOURCE:SELF}}") String registrationSource,
            @Value("${cpf.runtime.schema-version:unknown}") String schemaVersion,
            @Value("${cpf.runtime.config-hash:unknown}") String configHash,
            @Value("${cpf.runtime.control.agent.lease-seconds:60}") int leaseSeconds) {
        if (instanceId == null || instanceId.isBlank() || serviceId == null || serviceId.isBlank()
                || endpointCode == null || endpointCode.isBlank() || baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Runtime Agent enabled=true이면 instance-id/service-id/endpoint-code/base-url이 모두 필요합니다.");
        }
        List<CpfRuntimeChangeApplier> ordered = appliers.orderedStream().toList();
        LinkedHashMap<String, String> capabilities = new LinkedHashMap<>();
        for (CpfRuntimeChangeApplier applier : ordered) {
            String type = applier.changeType() == null ? "" : applier.changeType().trim().toUpperCase();
            if (type.isBlank()) throw new IllegalStateException("Runtime ChangeApplier changeType이 비어 있습니다.");
            String value = applier.payloadSchemaVersion() + "|" + applier.restartImpact().name()
                    + "|replay=" + applier.supportsIdempotentReplay()
                    + "|snapshot=" + applier.snapshotCapable();
            if (capabilities.putIfAbsent(type, value) != null) {
                throw new IllegalStateException("Runtime capability 중복: " + type);
            }
        }
        CpfRuntimeInstanceRegistration registration = new CpfRuntimeInstanceRegistration(
                instanceId, serviceId, endpointCode, environment, zone, cell, baseUrl,
                artifactVersion, artifactCommit, runtimeRole, registrationSource, schemaVersion, configHash,
                Map.copyOf(capabilities), Map.of(), Instant.now(), leaseSeconds);
        return new CpfRuntimeControlAgent(controlPlane, registration, ordered, inbox);
    }

    /** DB Owner Control Plane이 존재할 때 controller reconcile scheduling을 활성화합니다. */
    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnBean(CpfRuntimeControlPlaneRepository.class)
    static class RuntimeControllerSchedulingConfiguration {
    }


    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnBean(CpfReconciliationWorker.class)
    static class ReconciliationSchedulingConfiguration { }

    /** Agent가 활성화된 애플리케이션에서만 scheduling infrastructure를 켭니다. */
    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnProperty(prefix = "cpf.runtime.control.agent", name = "enabled", havingValue = "true")
    static class RuntimeAgentSchedulingConfiguration {
    }
}
