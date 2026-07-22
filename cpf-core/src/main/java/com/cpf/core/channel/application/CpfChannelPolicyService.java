package com.cpf.core.channel.application;

import com.cpf.core.channel.api.CpfChannelRegistryPort;
import com.cpf.core.channel.model.CpfChannelDefinition;
import com.cpf.core.channel.model.CpfChannelExecutionPolicy;
import com.cpf.core.channel.model.CpfChannelPolicyDecision;
import com.cpf.core.channel.model.CpfChannelPolicyPackage;
import com.cpf.core.channel.model.CpfChannelPolicySnapshot;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.transaction.annotation.Transactional;

/** 채널 정책을 원자적으로 교체하고 요청 경로에서는 잠금 없이 판단합니다. */
public class CpfChannelPolicyService {
    private final CpfChannelRegistryPort registryPort;
    private final AtomicReference<CpfChannelPolicySnapshot> snapshotReference;

    public CpfChannelPolicyService(CpfChannelRegistryPort registryPort) {
        this.registryPort = registryPort;
        this.snapshotReference = new AtomicReference<>(registryPort.loadSnapshot());
    }

    public CpfChannelPolicySnapshot snapshot() {
        return snapshotReference.get();
    }

    public synchronized CpfChannelPolicySnapshot refresh() {
        CpfChannelPolicySnapshot loaded = registryPort.loadSnapshot();
        snapshotReference.set(loaded);
        return loaded;
    }

    public CpfChannelPolicyDecision evaluate(
            String standardExecutionId,
            String originalChannelCode,
            String callerChannelCode,
            String requestType,
            boolean authenticated,
            boolean signed) {
        CpfChannelPolicySnapshot snapshot = snapshotReference.get();
        String original = normalize(originalChannelCode);
        String caller = normalize(callerChannelCode);
        String type = normalize(requestType);
        CpfChannelDefinition originalDefinition = snapshot.channels().get(original);
        CpfChannelDefinition callerDefinition = snapshot.channels().get(caller);
        if (originalDefinition == null || !originalDefinition.active()) {
            return denied(snapshot, "등록되지 않았거나 중지된 최초 채널입니다.");
        }
        if (callerDefinition == null || !callerDefinition.active()) {
            return denied(snapshot, "등록되지 않았거나 중지된 호출 채널입니다.");
        }
        return snapshot.resolve(standardExecutionId, original, caller, type, Instant.now())
                .map(policy -> decide(
                        snapshot, policy, originalDefinition, callerDefinition, authenticated, signed))
                .orElseGet(() -> denied(snapshot, "일치하는 채널 실행 정책이 없습니다."));
    }

    @Transactional(transactionManager = "cpfTransactionManager")
    public synchronized CpfChannelPolicySnapshot saveChannel(
            CpfChannelDefinition channel,
            String actor,
            String reason) {
        requireOperation(actor, reason);
        registryPort.saveChannel(channel, actor.trim(), reason.trim());
        return refresh();
    }

    @Transactional(transactionManager = "cpfTransactionManager")
    public synchronized CpfChannelPolicySnapshot savePolicy(
            CpfChannelExecutionPolicy policy,
            String actor,
            String reason) {
        requireOperation(actor, reason);
        registryPort.savePolicy(policy, actor.trim(), reason.trim());
        return refresh();
    }

    public CpfChannelPolicyPackage exportPackage() {
        return CpfChannelPolicyPackage.from(snapshotReference.get());
    }

    @Transactional(transactionManager = "cpfTransactionManager")
    public synchronized CpfChannelPolicySnapshot importPackage(
            CpfChannelPolicyPackage policyPackage,
            boolean dryRun,
            String actor,
            String reason) {
        requireOperation(actor, reason);
        if (policyPackage == null || !"1".equals(policyPackage.schemaVersion())) {
            throw new IllegalArgumentException("지원하지 않는 채널 정책 패키지 버전입니다.");
        }
        if (!policyPackage.hasValidChecksum()) {
            throw new IllegalArgumentException("채널 정책 패키지 checksum이 일치하지 않습니다.");
        }
        validateReferences(policyPackage);
        if (dryRun) {
            return snapshotReference.get();
        }
        policyPackage.channels().forEach(channel -> registryPort.saveChannel(channel, actor.trim(), reason.trim()));
        policyPackage.policies().forEach(policy -> registryPort.savePolicy(policy, actor.trim(), reason.trim()));
        return refresh();
    }

    private void validateReferences(CpfChannelPolicyPackage policyPackage) {
        Set<String> channelCodes = java.util.stream.Stream.concat(
                        snapshotReference.get().channels().keySet().stream(),
                        policyPackage.channels().stream().map(CpfChannelDefinition::channelCode))
                .collect(Collectors.toUnmodifiableSet());
        for (CpfChannelExecutionPolicy policy : policyPackage.policies()) {
            if (!channelCodes.contains(policy.originalChannelCode())) {
                throw new IllegalArgumentException(
                        "채널 정책 패키지의 최초 채널 참조가 없습니다. policyKey=" + policy.policyKey());
            }
            if (!channelCodes.contains(policy.callerChannelCode())) {
                throw new IllegalArgumentException(
                        "채널 정책 패키지의 호출 채널 참조가 없습니다. policyKey=" + policy.policyKey());
            }
        }
    }

    private CpfChannelPolicyDecision decide(
            CpfChannelPolicySnapshot snapshot,
            CpfChannelExecutionPolicy policy,
            CpfChannelDefinition originalDefinition,
            CpfChannelDefinition callerDefinition,
            boolean authenticated,
            boolean signed) {
        if (!policy.allowed()) {
            return new CpfChannelPolicyDecision(false, "정책에서 거래를 거부했습니다.", snapshot.version(),
                    policy.policyKey(), policy.authenticationRequired(), policy.signatureRequired(), policy.maxTps());
        }
        boolean authenticationRequired = policy.authenticationRequired()
                || originalDefinition.authenticationRequired()
                || callerDefinition.authenticationRequired();
        boolean signatureRequired = policy.signatureRequired()
                || originalDefinition.signatureRequired()
                || callerDefinition.signatureRequired();
        if (authenticationRequired && !authenticated) {
            return new CpfChannelPolicyDecision(false, "채널 정책에서 인증을 요구합니다.", snapshot.version(),
                    policy.policyKey(), true, signatureRequired, policy.maxTps());
        }
        if (signatureRequired && !signed) {
            return new CpfChannelPolicyDecision(false, "채널 정책에서 요청 서명을 요구합니다.", snapshot.version(),
                    policy.policyKey(), authenticationRequired, true, policy.maxTps());
        }
        return new CpfChannelPolicyDecision(true, "채널 정책 허용", snapshot.version(), policy.policyKey(),
                authenticationRequired, signatureRequired, policy.maxTps());
    }

    private CpfChannelPolicyDecision denied(CpfChannelPolicySnapshot snapshot, String reason) {
        return new CpfChannelPolicyDecision(false, reason, snapshot.version(), "", false, false, 0);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private void requireOperation(String actor, String reason) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("채널 정책 변경자는 필수입니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("채널 정책 변경 사유는 필수입니다.");
        }
    }
}
