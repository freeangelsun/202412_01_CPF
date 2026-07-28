package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeAck;
import com.cpf.core.api.runtimecontrol.CpfRuntimeActualState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeAgentPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceLease;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceRegistration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** 각 Runtime 프로세스에서 동작하는 durable Control Plane Agent입니다. */
public class CpfRuntimeControlAgent {
    private static final Logger log = LoggerFactory.getLogger(CpfRuntimeControlAgent.class);
    private static final Pattern KEY_VALUE_SECRET = Pattern.compile(
            "(?i)(password|passwd|pwd|token|secret|api[-_ ]?key|authorization|resident[-_ ]?number|rrn)\\s*[:=]\\s*([^,;\\s]+)");
    private static final Pattern EMAIL = Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(?:01[016789]|0\\d{1,2})[- ]?\\d{3,4}[- ]?\\d{4}(?!\\d)");
    private static final Pattern RRN = Pattern.compile("(?<!\\d)\\d{6}[- ]?[1-4]\\d{6}(?!\\d)");

    private final CpfRuntimeAgentPort controlPlane;
    private final CpfRuntimeInstanceRegistration registration;
    private final Map<String, CpfRuntimeChangeApplier> appliers;
    private final CpfRuntimeInstanceInboxStore inbox;
    private volatile CpfRuntimeInstanceLease lease;
    private volatile String actualHash;
    private volatile long actualVersion;

    public CpfRuntimeControlAgent(CpfRuntimeAgentPort controlPlane,
                                  CpfRuntimeInstanceRegistration registration,
                                  List<CpfRuntimeChangeApplier> appliers,
                                  CpfRuntimeInstanceInboxStore inbox) {
        this.controlPlane = controlPlane;
        this.registration = registration;
        this.inbox = inbox;
        LinkedHashMap<String, CpfRuntimeChangeApplier> map = new LinkedHashMap<>();
        for (CpfRuntimeChangeApplier applier : appliers) {
            String type = normalize(applier.changeType());
            if (type.isBlank()) throw new IllegalStateException("Runtime ChangeApplier changeType이 비어 있습니다.");
            if (map.putIfAbsent(type, applier) != null) {
                throw new IllegalStateException("Runtime ChangeApplier 중복: " + type);
            }
        }
        this.appliers = Map.copyOf(map);
    }

    /** 기존 생성 코드 호환입니다. */
    public CpfRuntimeControlAgent(CpfRuntimeAgentPort controlPlane,
                                  CpfRuntimeInstanceRegistration registration,
                                  List<CpfRuntimeChangeApplier> appliers) {
        this(controlPlane, registration, appliers,
                new CpfRuntimeInstanceInboxStore(java.nio.file.Path.of("runtime", "cpf-inbox", registration.instanceId())));
    }

    @PostConstruct
    public synchronized void start() {
        lease = controlPlane.register(registration);
        actualVersion = lease.actualVersion();
        actualHash = lease.actualHash();
        List<CpfRuntimeActualState> states = inbox.latestAppliedStates();
        if (!states.isEmpty()) {
            controlPlane.reconcileActualState(registration.instanceId(), lease.fencingToken(), states);
            states.stream().max(java.util.Comparator.comparingLong(CpfRuntimeActualState::actualVersion)).ifPresent(latest -> {
                actualVersion = latest.actualVersion();
                actualHash = latest.actualHash();
            });
        }
    }

    @PreDestroy
    public synchronized void stop() {
        CpfRuntimeInstanceLease current = lease;
        if (current == null) return;
        try {
            controlPlane.deregister(registration.instanceId(), current.fencingToken(), "APPLICATION_SHUTDOWN");
        } catch (RuntimeException ex) {
            log.warn("Runtime Agent graceful deregistration 실패. lease 만료로 복구됩니다. instanceId={}",
                    registration.instanceId(), ex);
        }
    }

    @Scheduled(fixedDelayString = "${cpf.runtime.control.agent.poll-millis:2000}")
    public void poll() {
        CpfRuntimeInstanceLease current = lease;
        if (current == null) {
            start();
            current = lease;
        }
        try {
            current = controlPlane.heartbeat(registration.instanceId(), current.fencingToken(), actualHash,
                    actualVersion, Instant.now());
            lease = current;
            // Instance별 version ordering과 backpressure를 위해 한 번에 한 delivery만 claim합니다.
            for (CpfRuntimeDelivery delivery : controlPlane.claim(registration.instanceId(), current.fencingToken(), 1)) {
                apply(delivery, current.fencingToken());
            }
        } catch (CpfRuntimeFenceException fenced) {
            log.warn("Runtime agent fencing 감지 후 재등록합니다. instanceId={}", registration.instanceId());
            lease = controlPlane.register(registration);
        } catch (RuntimeException failure) {
            log.error("Runtime Control Agent poll 실패. 기존 actual state를 유지합니다. instanceId={}",
                    registration.instanceId(), failure);
        }
    }

    private void apply(CpfRuntimeDelivery delivery, long fencingToken) {
        String baseType = normalize(delivery.changeType());
        CpfRuntimeChangeApplier applier = appliers.get(baseType);
        CpfRuntimeApplyResult result;

        if (applier == null) {
            result = CpfRuntimeApplyResult.failure("APPLIER_NOT_FOUND", "지원하지 않는 runtime changeType: " + baseType);
        } else if (delivery.payloadSchemaVersion() != applier.payloadSchemaVersion()) {
            result = CpfRuntimeApplyResult.failure("PAYLOAD_SCHEMA_UNSUPPORTED",
                    "지원 schema=" + applier.payloadSchemaVersion() + ", 요청 schema=" + delivery.payloadSchemaVersion());
        } else if (!CpfRuntimeCanonicalHash.sha256(delivery.payload()).equals(delivery.payloadHash())) {
            result = CpfRuntimeApplyResult.failure("PAYLOAD_HASH_MISMATCH", "Runtime payload hash 검증 실패");
        } else {
            var journal = inbox.find(delivery.deliveryId());
            if (journal.isPresent() && journal.get().state() == CpfRuntimeInstanceInboxStore.State.APPLIED) {
                result = CpfRuntimeApplyResult.success(journal.get().actualHash());
            } else if (journal.isPresent() && !applier.supportsIdempotentReplay()) {
                result = CpfRuntimeApplyResult.unknown("PREPARED_RESULT_UNKNOWN",
                        "이전 적용이 PREPARED 이후 중단되어 side effect 결과를 확인해야 합니다.");
            } else {
                inbox.prepare(delivery);
                try {
                    result = applier.apply(delivery);
                    if (result == null) {
                        result = CpfRuntimeApplyResult.failure("NULL_APPLY_RESULT",
                                "Runtime applier가 결과를 반환하지 않았습니다.");
                    }
                } catch (RuntimeException ex) {
                    // 예외는 side effect 발생 여부를 알 수 없으므로 자동 재시도 가능한 일반 failure로 축소하지 않습니다.
                    result = CpfRuntimeApplyResult.unknown(ex.getClass().getSimpleName(), safe(ex.getMessage()));
                }
            }
        }

        String ackState;
        String ackHash = null;
        if (result.applied()) {
            if (result.actualHash() == null || result.actualHash().isBlank()) {
                result = CpfRuntimeApplyResult.unknown("ACTUAL_HASH_MISSING",
                        "적용 성공 결과에는 actualHash가 필요합니다.");
                ackState = "UNKNOWN_RESULT";
            } else {
                inbox.markApplied(delivery, result.actualHash());
                actualVersion = delivery.desiredVersion();
                actualHash = result.actualHash();
                ackHash = actualHash;
                ackState = "SUCCESS";
            }
        } else if (result.restartRequired()) {
            // stage side effect는 발생했으므로 PREPARED를 유지합니다. 재기동 후 동일 delivery를 다시 검증합니다.
            ackHash = result.actualHash();
            ackState = "RESTART_REQUIRED";
        } else if (result.unknownResult()) {
            ackState = "UNKNOWN_RESULT";
        } else {
            // failure()는 side effect 미발생을 보장하는 계약입니다.
            inbox.clearPrepared(delivery.deliveryId());
            ackState = "FAILED";
        }

        controlPlane.acknowledge(new CpfRuntimeAck(delivery.deliveryId(), delivery.changeId(), delivery.instanceId(),
                fencingToken, "SUCCESS".equals(ackState) ? delivery.desiredVersion() : actualVersion,
                ackHash, ackState, result.errorCode(), safe(result.message()), Instant.now()));
    }

    private String normalize(String value) {
        String result = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return result.startsWith("ROLLBACK:") ? result.substring("ROLLBACK:".length()) : result;
    }

    private String safe(String value) {
        if (value == null) return "";
        String masked = KEY_VALUE_SECRET.matcher(value).replaceAll("$1=***");
        masked = EMAIL.matcher(masked).replaceAll("***@***");
        masked = PHONE.matcher(masked).replaceAll("***-****-****");
        masked = RRN.matcher(masked).replaceAll("******-*******");
        return masked.length() > 900 ? masked.substring(0, 900) : masked;
    }
}
