package com.cpf.platform.operations.channelregistry.application;

import com.cpf.platform.operations.channelregistry.api.CpfChannelRegistryPort;
import com.cpf.platform.operations.channelregistry.model.CpfChannelDefinition;
import com.cpf.platform.operations.channelregistry.model.CpfChannelExecutionPolicy;
import com.cpf.platform.operations.channelregistry.model.CpfChannelPolicyDecision;
import com.cpf.platform.operations.channelregistry.model.CpfChannelPolicyPackage;
import com.cpf.platform.operations.channelregistry.model.CpfChannelPolicySnapshot;

import java.time.Instant;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.transaction.annotation.Transactional;

/** 채널 정책을 원자적으로 교체하고 요청 경로에서는 잠금 없이 판단합니다. */
public class CpfChannelPolicyService {
    private final CpfChannelRegistryPort registryPort;
    private final AtomicReference<CpfChannelPolicySnapshot> snapshotReference;
    private final Duration maxStale;

    public CpfChannelPolicyService(CpfChannelRegistryPort registryPort) {
        this(registryPort, true, Duration.ofMinutes(5));
    }

    /**
     * 채널 정책 서비스의 초기 스냅샷을 구성합니다.
     *
     * <p>운영 기본값은 {@code loadOnStartup=true}이며 저장소 로드 실패를 숨기지 않습니다.
     * DB 없는 단위/컨텍스트 테스트만 명시적으로 {@code false}를 사용해 외부 DB 연결 없이
     * 전체 거부 스냅샷으로 컨텍스트를 구성할 수 있습니다. 이후 {@link #refresh()}와 변경 작업은
     * 여전히 저장소 오류를 그대로 전파합니다.</p>
     */
    public CpfChannelPolicyService(CpfChannelRegistryPort registryPort, boolean loadOnStartup) {
        this(registryPort, loadOnStartup, Duration.ofMinutes(5));
    }

    public CpfChannelPolicyService(CpfChannelRegistryPort registryPort, boolean loadOnStartup, Duration maxStale) {
        this.registryPort = java.util.Objects.requireNonNull(registryPort, "registryPort");
        if (maxStale == null || maxStale.isZero() || maxStale.isNegative()) throw new IllegalArgumentException("maxStale must be positive");
        this.maxStale = maxStale;
        this.snapshotReference = new AtomicReference<>(loadOnStartup ? loadFresh() : CpfChannelPolicySnapshot.denyAll(maxStale));
    }

    public CpfChannelPolicySnapshot snapshot() {
        return snapshotReference.get();
    }

    public synchronized CpfChannelPolicySnapshot refresh() {
        try {
            CpfChannelPolicySnapshot loaded = loadFresh(); snapshotReference.set(loaded); return loaded;
        } catch (RuntimeException ex) {
            CpfChannelPolicySnapshot current=snapshotReference.get(); Instant now=Instant.now();
            if (current != null && !current.expiredAt(now)) {
                CpfChannelPolicySnapshot lkg=current.withStatus(CpfChannelPolicySnapshot.Status.REFRESH_FAILED);
                snapshotReference.set(lkg); return lkg;
            }
            if(current!=null)snapshotReference.set(current.withStatus(CpfChannelPolicySnapshot.Status.EXPIRED));
            throw ex;
        }
    }

    /**
     * Canonical Online Context의 callerChannel을 Channel Policy 정본으로 평가합니다.
     * Policy key는 operationId + callerChannel이며 currentChannel/originalChannel을 허용 키로 사용하지 않습니다.
     */
    public CpfChannelPolicyDecision evaluateCallerChannel(
            String operationId, String callerChannel, boolean authenticated, boolean signed) {
        CpfChannelPolicySnapshot snapshot = snapshotReference.get();
        Instant now = Instant.now();
        if (snapshot == null || snapshot.expiredAt(now) || snapshot.status() == CpfChannelPolicySnapshot.Status.EXPIRED) {
            return new CpfChannelPolicyDecision(false, "채널 정책 LKG가 없거나 maxStale을 초과했습니다.",
                    snapshot == null ? -1 : snapshot.version(), "", false, false, 0);
        }
        String execution = operationId == null || operationId.isBlank() ? "*" : operationId.trim();
        String caller = normalize(callerChannel);
        if (caller.isEmpty()) {
            return denied(snapshot, "Caller Channel이 필요합니다.");
        }
        CpfChannelDefinition callerDefinition = snapshot.channels().get(caller);
        if (callerDefinition == null || !callerDefinition.active()) {
            return denied(snapshot, "등록되지 않았거나 중지된 Caller Channel입니다.");
        }
        return snapshot.resolve(execution, caller, now)
                .map(policy -> decide(snapshot, policy, callerDefinition, authenticated, signed))
                .orElseGet(() -> denied(snapshot, "일치하는 operationId + callerChannel 정책이 없습니다."));
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
            if (!"*".equals(policy.callerChannel()) && !channelCodes.contains(policy.callerChannel())) {
                throw new IllegalArgumentException(
                        "채널 정책 패키지의 Caller Channel 참조가 없습니다. policyKey=" + policy.policyKey());
            }
        }
    }

    private CpfChannelPolicyDecision decide(
            CpfChannelPolicySnapshot snapshot,
            CpfChannelExecutionPolicy policy,
            CpfChannelDefinition callerDefinition,
            boolean authenticated,
            boolean signed) {
        if (!policy.allowed()) {
            return new CpfChannelPolicyDecision(false, "정책에서 거래를 거부했습니다.", snapshot.version(),
                    policy.policyKey(), policy.authenticationRequired(), policy.signatureRequired(), policy.maxTps());
        }
        boolean authenticationRequired = policy.authenticationRequired()
                || callerDefinition.authenticationRequired();
        boolean signatureRequired = policy.signatureRequired()
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


    private CpfChannelPolicySnapshot loadFresh() {
        CpfChannelPolicySnapshot raw=registryPort.loadSnapshot();
        Instant now=Instant.now();
        return new CpfChannelPolicySnapshot(raw.version(), now, now.plus(maxStale),
                CpfChannelPolicySnapshot.Status.CURRENT, raw.channels(), raw.policies());
    }
    private CpfChannelPolicyDecision denied(CpfChannelPolicySnapshot snapshot, String reason) {
        return new CpfChannelPolicyDecision(false, reason, snapshot.version(), "", false, false, 0);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return "";
        String normalized = value.trim();
        return normalized.matches("[A-Z0-9][A-Z0-9_-]{0,15}") ? normalized : "";
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
