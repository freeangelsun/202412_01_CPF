package com.cpf.notification.dispatch;

import com.cpf.notification.api.CpfNotificationRequest;
import com.cpf.notification.api.CpfNotificationResult;
import com.cpf.notification.spi.CpfNotificationProvider;
import com.cpf.notification.spi.CpfNotificationReconciler;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
public final class CpfNotificationWorker {
    private final JdbcCpfNotificationOutbox outbox;
    private final Map<String, CpfNotificationProvider> providers;
    private final CpfNotificationPreferencePolicy preferences;
    private final CpfNotificationProperties properties;
    private final Clock clock;
    private final CpfNotificationContextCodec contextCodec;

    public CpfNotificationWorker(
            JdbcCpfNotificationOutbox outbox,
            List<CpfNotificationProvider> providers,
            CpfNotificationPreferencePolicy preferences,
            CpfNotificationProperties properties,
            Clock clock,
            CpfNotificationContextCodec contextCodec) {
        this.outbox = Objects.requireNonNull(outbox, "outbox");
        this.preferences = Objects.requireNonNull(preferences, "preferences");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.contextCodec = Objects.requireNonNull(contextCodec, "contextCodec");
        if (providers == null || providers.isEmpty()) {
            throw new IllegalStateException("notification capability requires exactly one or more named providers");
        }
        Map<String, CpfNotificationProvider> indexed = new HashMap<>();
        for (CpfNotificationProvider provider : providers) {
            Objects.requireNonNull(provider, "notification provider");
            String channel = provider.channel();
            if (channel == null || channel.isBlank()) {
                throw new IllegalStateException("notification provider channel is required");
            }
            channel = channel.trim().toUpperCase(java.util.Locale.ROOT);
            if (indexed.putIfAbsent(channel, provider) != null) {
                throw new IllegalStateException("duplicate notification provider: " + channel);
            }
        }
        this.providers = Map.copyOf(indexed);
    }


    public RunResult runOnce(String workerId, int limit) {
        int completed = 0;
        int retried = 0;
        int unknown = 0;
        Instant now = Instant.now(clock);
        for (JdbcCpfNotificationOutbox.ClaimedNotification claimed : outbox.claimWithContext(workerId, limit, now, properties.leaseDuration())) {
            CpfNotificationRequest request=claimed.request();
            CpfNotificationPreferencePolicy.Decision decision = preferences.evaluate(request, clock);
            if (!decision.allowed()) {
                outbox.retry(request.notificationId(), decision.reason(), decision.resumeAt());
                retried++;
                continue;
            }
            CpfNotificationProvider provider = providers.get(request.channel().trim().toUpperCase(java.util.Locale.ROOT));
            if (provider == null) {
                outbox.retry(request.notificationId(), "MISSING_PROVIDER", now.plus(properties.retryDelay()));
                retried++;
                continue;
            }
            try (AutoCloseable ignoredContext=contextCodec.bind(
                    claimed.contextLineage(),request,claimed.attemptCount()+1,false,provider.channel())) {
                CpfNotificationResult result = provider.send(request);
                if (result == null) {
                    result = CpfNotificationResult.unknown(
                            request.notificationId(), provider.channel(), "provider returned null");
                }
                if (isUnknown(result)) {
                    outbox.markUnknown(result, now.plus(properties.unknownReconcileDelay()));
                    unknown++;
                } else if (isSuccess(result)) {
                    outbox.complete(result);
                    completed++;
                } else {
                    outbox.retry(request.notificationId(), safe(result.detail()), now.plus(properties.retryDelay()));
                    retried++;
                }
            } catch (RuntimeException exception) {
                // The transport may have accepted the request before failing locally.
                // Preserve UNKNOWN and reconcile before any re-send to avoid duplicates.
                outbox.markUnknown(
                        CpfNotificationResult.unknown(
                                request.notificationId(), provider.channel(), safe(exception)),
                        now.plus(properties.unknownReconcileDelay()));
                unknown++;
            }
        }
        return new RunResult(completed, retried, unknown);
    }

    public RunResult reconcileUnknown(String workerId, int limit) {
        int completed = 0;
        int retried = 0;
        int unknown = 0;
        Instant now = Instant.now(clock);
        for (JdbcCpfNotificationOutbox.ClaimedNotification claimed : outbox.claimUnknownWithContext(
                workerId, limit, now, properties.leaseDuration())) {
            CpfNotificationRequest request=claimed.request();
            CpfNotificationProvider provider = providers.get(request.channel().trim().toUpperCase(java.util.Locale.ROOT));
            if (!(provider instanceof CpfNotificationReconciler reconciler)) {
                outbox.markUnknown(
                        CpfNotificationResult.unknown(request.notificationId(),
                                provider == null ? "MISSING_PROVIDER" : provider.channel(),
                                "provider does not support reconcile"),
                        now.plus(properties.unknownReconcileDelay()));
                unknown++;
                continue;
            }
            try (AutoCloseable ignoredContext=contextCodec.bind(
                    claimed.contextLineage(),request,claimed.attemptCount()+1,true,provider.channel())) {
                CpfNotificationResult previous = outbox.currentResult(request.notificationId());
                CpfNotificationResult result = reconciler.reconcile(request, previous);
                if (result == null || isUnknown(result)) {
                    outbox.markUnknown(
                            result == null
                                    ? CpfNotificationResult.unknown(request.notificationId(), provider.channel(), "reconcile returned null")
                                    : result,
                            now.plus(properties.unknownReconcileDelay()));
                    unknown++;
                } else if (isSuccess(result)) {
                    outbox.completeReconcile(result);
                    completed++;
                } else {
                    outbox.retry(request.notificationId(), safe(result.detail()), now.plus(properties.retryDelay()));
                    retried++;
                }
            } catch (RuntimeException exception) {
                outbox.markUnknown(
                        CpfNotificationResult.unknown(request.notificationId(), provider.channel(), safe(exception)),
                        now.plus(properties.unknownReconcileDelay()));
                unknown++;
            }
        }
        return new RunResult(completed, retried, unknown);
    }

    private static boolean isUnknown(CpfNotificationResult result) {
        return "UNKNOWN".equals(result.status()) || "UNKNOWN_RESULT".equals(result.status());
    }

    private static boolean isSuccess(CpfNotificationResult result) {
        return "SENT".equals(result.status()) || "DELIVERED".equals(result.status());
    }

    private static String safe(Exception exception) {
        String message = exception.getMessage();
        return safe(message == null ? exception.getClass().getSimpleName() : message);
    }

    private static String safe(String value) {
        if (value == null) return "UNKNOWN_FAILURE";
        return value.substring(0, Math.min(1000, value.length()));
    }

    public record RunResult(int completed, int retried, int unknown) {
        public RunResult(int completed, int retried) {
            this(completed, retried, 0);
        }
    }
}
