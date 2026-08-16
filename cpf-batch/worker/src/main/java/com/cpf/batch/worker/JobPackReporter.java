package com.cpf.batch.worker;

import com.cpf.batch.api.JobPackManifest;
import com.cpf.batch.runtime.JobPackCatalog;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicBoolean;

public class JobPackReporter implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(JobPackReporter.class);

    private final JobPackCatalog catalog;
    private final RegistrationClient registrationClient;
    private final AtomicBoolean running = new AtomicBoolean();

    public JobPackReporter(JobPackCatalog catalog, RestClient.Builder builder, String controlBaseUrl) {
        this(catalog, registrationClient(builder, controlBaseUrl));
    }

    JobPackReporter(JobPackCatalog catalog, RegistrationClient registrationClient) {
        this.catalog = catalog;
        this.registrationClient = registrationClient;
    }

    @Override
    public void start() {
        running.set(false);
        for (JobPackManifest manifest : catalog.manifests()) {
            try {
                registrationClient.register(manifest);
            } catch (RuntimeException failure) {
                log.error(
                        "Job Pack registration failed; worker startup is aborted. jobPackId={}, cause={}",
                        SensitiveTextSanitizer.sanitize(manifest.jobPackId()),
                        SensitiveTextSanitizer.sanitize(failure.getMessage()));
                throw new IllegalStateException(
                        "Job Pack registration failed; worker is not ready. jobPackId="
                                + SensitiveTextSanitizer.sanitize(manifest.jobPackId()),
                        failure);
            }
        }
        running.set(true);
    }

    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private static RegistrationClient registrationClient(
            RestClient.Builder builder,
            String controlBaseUrl) {
        RestClient client = builder.baseUrl(controlBaseUrl).build();
        return manifest -> client
                .post()
                .uri("/api/v1/batch/job-packs/registrations")
                .body(manifest)
                .retrieve()
                .toBodilessEntity();
    }

    @FunctionalInterface
    interface RegistrationClient {
        void register(JobPackManifest manifest);
    }
}
