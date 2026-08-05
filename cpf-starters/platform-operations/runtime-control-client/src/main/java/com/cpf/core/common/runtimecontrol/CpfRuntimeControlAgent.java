package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeAck;
import com.cpf.core.api.runtimecontrol.CpfRuntimeActualState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeAckState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeAgentPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException;
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
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final CpfRuntimeApplyGuard applyGuard;
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private volatile CpfRuntimeInstanceLease lease;
    private volatile boolean inboxReconciliationPending;
    private volatile String actualHash;
    private volatile long actualVersion;
    private volatile PendingAck pendingAck;

    public CpfRuntimeControlAgent(CpfRuntimeAgentPort controlPlane,
                                  CpfRuntimeInstanceRegistration registration,
                                  List<CpfRuntimeChangeApplier> appliers,
                                  CpfRuntimeInstanceInboxStore inbox) {
        this(controlPlane, registration, appliers, inbox, CpfRuntimeApplyGuard.defaults());
    }

    public CpfRuntimeControlAgent(CpfRuntimeAgentPort controlPlane,
                                  CpfRuntimeInstanceRegistration registration,
                                  List<CpfRuntimeChangeApplier> appliers,
                                  CpfRuntimeInstanceInboxStore inbox,
                                  CpfRuntimeApplyGuard applyGuard) {
        this.controlPlane = Objects.requireNonNull(controlPlane, "controlPlane");
        this.registration = Objects.requireNonNull(registration, "registration");
        this.inbox = Objects.requireNonNull(inbox, "inbox");
        this.applyGuard = Objects.requireNonNull(applyGuard, "applyGuard");
        Objects.requireNonNull(appliers, "appliers");
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
        if (stopped.get()) {
            throw new IllegalStateException("종료된 Runtime Agent는 다시 시작할 수 없습니다.");
        }
        if (lease == null) {
            try {
                lease = registerAndReconcile();
            } catch (CpfRuntimeFenceException deferred) {
                // Crash 직후 이전 lease가 아직 살아 있으면 새 프로세스 전체를 종료하지 않습니다.
                // business runtime은 살아 둔 채 다음 scheduled poll에서 lease 만료 후 재등록합니다.
                lease = null;
                log.warn("Runtime Agent 등록이 기존 live lease로 지연되었습니다. "
                                + "다음 poll에서 재시도합니다. instanceId={}",
                        registration.instanceId(), deferred);
            }
        }
    }

    private CpfRuntimeInstanceLease registerAndReconcile() {
        CpfRuntimeInstanceLease registered = controlPlane.register(currentRegistration());
        // Register 성공 뒤 Inbox 대사만 실패하더라도 살아 있는 자기 lease를 잃지 않습니다.
        lease = registered;
        actualVersion = registered.actualVersion();
        actualHash = registered.actualHash();
        try {
            reconcileDurableInbox(registered);
        } catch (RuntimeException failure) {
            inboxReconciliationPending = true;
            log.warn("Runtime Agent 등록은 성공했지만 durable Inbox 대사가 실패했습니다. "
                            + "대사 성공 전 delivery claim을 차단합니다. instanceId={}",
                    registration.instanceId(), failure);
        }
        return registered;
    }


    private CpfRuntimeInstanceRegistration currentRegistration() {
        return new CpfRuntimeInstanceRegistration(
                registration.instanceId(), registration.serviceId(), registration.endpointCode(),
                registration.environment(), registration.zone(), registration.cell(), registration.baseUrl(),
                registration.artifactVersion(), registration.artifactCommit(), registration.runtimeRole(),
                registration.registrationSource(), registration.schemaVersion(), registration.configHash(),
                registration.capabilities(), registration.labels(), Instant.now(), registration.leaseSeconds());
    }

    private void reconcileDurableInbox(CpfRuntimeInstanceLease current) {
        List<CpfRuntimeActualState> states = inbox.latestAppliedStates();
        if (!states.isEmpty()) {
            controlPlane.reconcileActualState(registration.instanceId(), current.fencingToken(), states);
            states.stream().max(java.util.Comparator.comparingLong(CpfRuntimeActualState::actualVersion)).ifPresent(latest -> {
                actualVersion = latest.actualVersion();
                actualHash = latest.actualHash();
            });
        }
        inboxReconciliationPending = false;
    }

    @PreDestroy
    public synchronized void stop() {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }
        CpfRuntimeInstanceLease current = lease;
        lease = null;
        inboxReconciliationPending = false;
        pendingAck = null;
        // 신규 poll을 차단한 뒤 in-flight apply를 먼저 정리해 유효 fencing ACK 기회를 보존합니다.
        applyGuard.close();
        try {
            if (current != null) {
                controlPlane.deregister(registration.instanceId(), current.fencingToken(), "APPLICATION_SHUTDOWN");
            }
        } catch (RuntimeException ex) {
            log.warn("Runtime Agent graceful deregistration 실패. lease 만료로 복구됩니다. instanceId={}",
                    registration.instanceId(), ex);
        }
    }

    @Scheduled(fixedDelayString = "${cpf.runtime.control.agent.poll-millis:2000}")
    public void poll() {
        if (stopped.get()) return;
        CpfRuntimeInstanceLease current = lease;
        if (current == null) {
            start();
            current = lease;
        }
        if (stopped.get() || current == null) return;
        try {
            current = controlPlane.heartbeat(registration.instanceId(), current.fencingToken(), actualHash,
                    actualVersion, Instant.now());
            if (stopped.get()) return;
            lease = current;
            if (!flushPendingAck()) return;
            if (inboxReconciliationPending) {
                reconcileDurableInbox(current);
                if (inboxReconciliationPending || stopped.get()) return;
            }
            // Instance별 version ordering과 backpressure를 위해 한 번에 한 delivery만 claim합니다.
            for (CpfRuntimeDelivery delivery : controlPlane.claim(registration.instanceId(), current.fencingToken(), 1)) {
                if (stopped.get()) return;
                apply(delivery, current.fencingToken());
            }
        } catch (CpfRuntimeFenceException fenced) {
            if (stopped.get()) return;
            pendingAck = null;
            lease = null;
            log.warn("Runtime agent fencing 감지 후 durable Inbox 대사와 함께 재등록을 시도합니다. instanceId={}",
                    registration.instanceId(), fenced);
            // 다른 프로세스가 이미 새 lease를 획득했다면 즉시 재등록도 다시 fenced될 수 있습니다.
            // start()의 deferred-registration 계약을 재사용해 scheduler thread 예외와 busy loop를 방지합니다.
            start();
        } catch (RuntimeException failure) {
            log.error("Runtime Control Agent poll 실패. 기존 actual state를 유지합니다. instanceId={}",
                    registration.instanceId(), failure);
        }
    }

    private void apply(CpfRuntimeDelivery delivery, long fencingToken) {
        String baseType = normalize(delivery.changeType());
        CpfRuntimeChangeApplier applier = appliers.get(baseType);
        CpfRuntimeApplyResult result = null;
        java.util.Optional<CpfRuntimeInstanceInboxStore.Entry> existingJournal = java.util.Optional.empty();
        try {
            existingJournal = inbox.find(delivery);
        } catch (CpfRuntimeInstanceInboxStore.IdentityConflictException conflict) {
            result = CpfRuntimeApplyResult.unknown("INBOX_IDENTITY_CONFLICT", safe(conflict.getMessage()));
        } catch (RuntimeException journalFailure) {
            result = CpfRuntimeApplyResult.unknown("INBOX_JOURNAL_INVALID", safe(journalFailure.getMessage()));
        }
        boolean preservePreparedOnFailure = existingJournal.isPresent()
                && existingJournal.get().state() == CpfRuntimeInstanceInboxStore.State.PREPARED;

        if (result != null) {
            // journal identity/corruption은 기존 evidence를 변경하지 않고 UNKNOWN으로 ACK합니다.
        } else if (applier == null) {
            result = CpfRuntimeApplyResult.failure("APPLIER_NOT_FOUND", "지원하지 않는 runtime changeType: " + baseType);
        } else if (delivery.payloadSchemaVersion() != applier.payloadSchemaVersion()) {
            result = CpfRuntimeApplyResult.failure("PAYLOAD_SCHEMA_UNSUPPORTED",
                    "지원 schema=" + applier.payloadSchemaVersion() + ", 요청 schema=" + delivery.payloadSchemaVersion());
        } else if (!CpfRuntimeCanonicalHash.sha256(delivery.payload()).equals(delivery.payloadHash())) {
            result = CpfRuntimeApplyResult.failure("PAYLOAD_HASH_MISMATCH", "Runtime payload hash 검증 실패");
        } else {
            boolean preparedBeforeAttempt = preservePreparedOnFailure;
            if (existingJournal.isPresent()
                    && existingJournal.get().state() == CpfRuntimeInstanceInboxStore.State.APPLIED) {
                result = CpfRuntimeApplyResult.success(existingJournal.get().actualHash());
            } else if (preparedBeforeAttempt && !applier.supportsIdempotentReplay()) {
                result = CpfRuntimeApplyResult.unknown("PREPARED_RESULT_UNKNOWN",
                        "이전 적용이 PREPARED 이후 중단되어 side effect 결과를 확인해야 합니다.");
            } else {
                try {
                    Runnable clearCurrentAttempt = preparedBeforeAttempt
                            ? () -> { }
                            : () -> inbox.clearPrepared(delivery);
                    result = applyGuard.execute(
                            applier,
                            delivery,
                            () -> inbox.prepare(delivery),
                            clearCurrentAttempt);
                } catch (RuntimeException ex) {
                    // Guard 자체 실패도 side effect 발생 여부를 확정할 수 없으므로 UNKNOWN으로 보존합니다.
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
                ackState = CpfRuntimeAckState.UNKNOWN_RESULT.name();
            } else {
                String appliedHash = result.actualHash();
                try {
                    inbox.markApplied(delivery, appliedHash);
                    actualVersion = delivery.desiredVersion();
                    actualHash = appliedHash;
                    ackHash = actualHash;
                    ackState = CpfRuntimeAckState.SUCCESS.name();
                } catch (RuntimeException persistFailure) {
                    // Side effect 성공 뒤 APPLIED journal 영속화가 실패하면 성공 ACK로 확대하지 않습니다.
                    // in-memory actualHash는 evidence로 전달하되 재기동 복구 증명이 없으므로 UNKNOWN을 보존합니다.
                    result = CpfRuntimeApplyResult.unknown(
                            "INBOX_APPLIED_PERSIST_UNKNOWN",
                            safe(persistFailure.getMessage()));
                    ackHash = appliedHash;
                    ackState = CpfRuntimeAckState.UNKNOWN_RESULT.name();
                }
            }
        } else if (result.restartRequired()) {
            // stage side effect는 발생했으므로 PREPARED를 유지합니다. 재기동 후 동일 delivery를 다시 검증합니다.
            ackHash = result.actualHash();
            ackState = CpfRuntimeAckState.RESTART_REQUIRED.name();
        } else if (result.unknownResult()) {
            ackState = CpfRuntimeAckState.UNKNOWN_RESULT.name();
        } else {
            // failure()는 side effect 미발생을 보장하는 계약입니다.
            // 과거 UNKNOWN의 PREPARED journal 보존 여부는 apply 경로에서 계산합니다.
            if (!preservePreparedOnFailure) {
                inbox.clearPrepared(delivery);
            }
            ackState = CpfRuntimeAckState.FAILED.name();
        }

        CpfRuntimeAck ack = new CpfRuntimeAck(delivery.deliveryId(), delivery.changeId(), delivery.instanceId(),
                fencingToken, delivery.attempt(),
                CpfRuntimeAckState.SUCCESS.name().equals(ackState) ? delivery.desiredVersion() : actualVersion,
                ackHash, ackState, result.errorCode(), safe(result.message()), Instant.now());
        PendingAck pending = new PendingAck(ack, delivery, CpfRuntimeAckState.SUCCESS.name().equals(ackState));
        try {
            controlPlane.acknowledge(ack);
            afterAckConfirmed(pending);
        } catch (RuntimeException ackFailure) {
            // 한 번에 하나만 claim하므로 미확정 ACK를 보존하고 다음 heartbeat 뒤에 먼저 재전송합니다.
            pendingAck = pending;
            if (pending.clearApplied()) inboxReconciliationPending = true;
            throw ackFailure;
        }
    }


    private boolean flushPendingAck() {
        PendingAck pending = pendingAck;
        if (pending == null) return true;
        controlPlane.acknowledge(pending.ack());
        afterAckConfirmed(pending);
        return pendingAck == null;
    }

    private void afterAckConfirmed(PendingAck pending) {
        if (!pending.clearApplied()) {
            if (pendingAck == pending) pendingAck = null;
            return;
        }
        try {
            // ACK 응답을 받은 뒤에만 복구 journal을 제거합니다. 응답 유실 시에는 APPLIED를 보존합니다.
            inbox.clearApplied(pending.delivery());
            if (pendingAck == pending) pendingAck = null;
            inboxReconciliationPending = false;
        } catch (RuntimeException cleanupFailure) {
            // ACK는 멱등하므로 다음 poll에서 ACK 확인과 journal 정리를 함께 재시도합니다.
            pendingAck = pending;
            inboxReconciliationPending = false;
            log.warn("Runtime ACK는 확정됐지만 APPLIED Inbox 정리에 실패했습니다. "
                            + "다음 poll에서 멱등 ACK 후 정리를 재시도합니다. deliveryId={}",
                    pending.delivery().deliveryId(), cleanupFailure);
        }
    }

    private record PendingAck(CpfRuntimeAck ack, CpfRuntimeDelivery delivery, boolean clearApplied) { }

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
