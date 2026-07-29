package com.cpf.batch.worker;

import com.cpf.batch.api.JobPackManifest;
import com.cpf.batch.runtime.JobPackCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobPackReporterTest {
    @Test
    void registrationFailureAbortsStartupInsteadOfReportingRunning() {
        JobPackCatalog catalog = mock(JobPackCatalog.class);
        JobPackManifest manifest = mock(JobPackManifest.class);
        when(manifest.jobPackId()).thenReturn("pack-a");
        when(catalog.manifests()).thenReturn(List.of(manifest));
        JobPackReporter reporter = new JobPackReporter(
                catalog,
                ignored -> {
                    throw new IllegalStateException("control server unavailable");
                });

        assertThatThrownBy(reporter::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("worker is not ready")
                .hasMessageContaining("pack-a");
        assertThat(reporter.isRunning()).isFalse();
    }
}
