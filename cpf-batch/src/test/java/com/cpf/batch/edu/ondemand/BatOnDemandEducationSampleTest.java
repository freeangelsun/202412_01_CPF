package com.cpf.batch.edu.ondemand;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatOnDemandEducationSampleTest {

    @Test
    void 신규접수는표준배치ID와멱등키를그대로서비스에전달한다() {
        BatOnDemandService service = mock(BatOnDemandService.class);
        BatOnDemandEducationSample sample = new BatOnDemandEducationSample(service);
        BatOnDemandRequest request = new BatOnDemandRequest(
                BatOnDemandJobConfig.STANDARD_BATCH_ID,
                "20260716",
                "ORDER-CLOSE-20260716",
                "영업일 마감 재처리",
                "operator01",
                Map.of("chunkSize", 100));
        BatOnDemandStatus expected = status("REQ-001", "REQUESTED");
        when(service.submit(request)).thenReturn(expected);

        BatOnDemandStatus actual = sample.submit(request);

        assertThat(actual).isEqualTo(expected);
        verify(service).submit(request);
    }

    @Test
    void restart와rerun은서로다른운영의도로분리한다() {
        BatOnDemandService service = mock(BatOnDemandService.class);
        BatOnDemandEducationSample sample = new BatOnDemandEducationSample(service);
        when(service.restart("REQ-001", "operator01", "checkpoint 재시작"))
                .thenReturn(status("REQ-001", "RESTARTED"));
        when(service.rerun("REQ-001", "operator01", "신규 인스턴스 재수행"))
                .thenReturn(status("REQ-001", "RERUN_REQUESTED"));

        BatOnDemandStatus restarted = sample.restart("REQ-001", "operator01", "checkpoint 재시작");
        BatOnDemandStatus rerun = sample.rerun("REQ-001", "operator01", "신규 인스턴스 재수행");

        assertThat(restarted.requestStatus()).isEqualTo("RESTARTED");
        assertThat(rerun.requestStatus()).isEqualTo("RERUN_REQUESTED");
        verify(service).restart("REQ-001", "operator01", "checkpoint 재시작");
        verify(service).rerun("REQ-001", "operator01", "신규 인스턴스 재수행");
    }

    private BatOnDemandStatus status(String requestId, String status) {
        return new BatOnDemandStatus(
                requestId,
                BatOnDemandJobConfig.STANDARD_BATCH_ID,
                "ORDER-CLOSE-20260716",
                "20260716120000000BATbatloca0000001",
                "20260716",
                status,
                10L,
                20L,
                Map.of(),
                null,
                null,
                Instant.parse("2026-07-16T03:00:00Z"),
                Instant.parse("2026-07-16T03:01:00Z"));
    }
}
