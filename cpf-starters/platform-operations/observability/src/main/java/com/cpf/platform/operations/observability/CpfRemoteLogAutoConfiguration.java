package com.cpf.platform.operations.observability;

import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogArtifactPort;
import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogBundleJobPort;
import com.cpf.foundation.service.remotelog.DefaultCpfRemoteLogBundleJobManager;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Wires the bounded owner-scoped remote-log bundle manager only when an artifact adapter exists. */
@AutoConfiguration
@ConditionalOnBean(CpfRemoteLogArtifactPort.class)
public class CpfRemoteLogAutoConfiguration {
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(CpfRemoteLogBundleJobPort.class)
    DefaultCpfRemoteLogBundleJobManager cpfRemoteLogBundleJobPort(
            CpfRemoteLogArtifactPort artifactPort,
            ObjectProvider<Clock> clockProvider,
            Environment environment) {
        Clock clock = clockProvider.getIfUnique(Clock::systemUTC);
        return new DefaultCpfRemoteLogBundleJobManager(
                artifactPort,
                clock,
                settings(environment));
    }

    static DefaultCpfRemoteLogBundleJobManager.Settings settings(Environment environment) {
        int workerCount = bounded(environment.getProperty(
                "cpf.remote-log.bundle-job.worker-count", Integer.class, 1), 1, 8, "worker-count");
        int queueCapacity = bounded(environment.getProperty(
                "cpf.remote-log.bundle-job.queue-capacity", Integer.class, 100),
                1, 10_000, "queue-capacity");
        int maxActiveJobs = bounded(environment.getProperty(
                "cpf.remote-log.bundle-job.max-active-jobs", Integer.class, 4),
                1, 1_000, "max-active-jobs");
        int maxJobs = bounded(environment.getProperty(
                "cpf.remote-log.bundle-job.max-retained-jobs", Integer.class, 1_000),
                maxActiveJobs, 100_000, "max-retained-jobs");
        int maxRequestsPerMinute = bounded(environment.getProperty(
                "cpf.remote-log.bundle-job.max-requests-per-minute", Integer.class, 10),
                1, 10_000, "max-requests-per-minute");
        int maxArtifacts = bounded(environment.getProperty(
                "cpf.remote-log.bundle-job.max-artifacts-per-job", Integer.class, 100),
                1, 1_000, "max-artifacts-per-job");
        long jobTtlSeconds = bounded(environment.getProperty(
                "cpf.remote-log.bundle-job.job-ttl-seconds", Long.class, 900L),
                60L, Duration.ofHours(24).toSeconds(), "job-ttl-seconds");
        long grantTtlSeconds = bounded(environment.getProperty(
                "cpf.remote-log.bundle-job.download-token-ttl-seconds", Long.class, 300L),
                10L, Duration.ofMinutes(30).toSeconds(), "download-token-ttl-seconds");
        return new DefaultCpfRemoteLogBundleJobManager.Settings(
                workerCount,
                queueCapacity,
                maxJobs,
                maxActiveJobs,
                maxRequestsPerMinute,
                maxArtifacts,
                Duration.ofSeconds(jobTtlSeconds),
                Duration.ofSeconds(grantTtlSeconds));
    }

    private static int bounded(int value, int minimum, int maximum, String property) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException("cpf.remote-log.bundle-job." + property
                    + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    private static long bounded(long value, long minimum, long maximum, String property) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException("cpf.remote-log.bundle-job." + property
                    + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }
}
