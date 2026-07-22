package com.cpf.core.common.logging.segment;

import com.cpf.core.common.logging.TransactionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TransactionSegmentServiceTest {

    private TransactionSegmentPersistenceService persistenceService;
    private TransactionSegmentService service;

    @BeforeEach
    void setUp() {
        persistenceService = mock(TransactionSegmentPersistenceService.class);
        service = new TransactionSegmentService(persistenceService);
        TransactionContext.initialize(
                "20260722120000000CPFlocal010000001",
                "trace-segment-test",
                null);
    }

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void recordsStartAndSuccessfulEndOnlyOnce() {
        TransactionSegmentScope scope = service.start(
                TransactionSegmentRole.SUB,
                TransactionSegmentDirection.OUTBOUND,
                "ref",
                "ref",
                "acc",
                "/acc/api/shared/accounts/summary",
                "계좌 요약 조회");

        scope.success();
        scope.close();

        ArgumentCaptor<TransactionSegmentRecord> startCaptor =
                ArgumentCaptor.forClass(TransactionSegmentRecord.class);
        verify(persistenceService).insert(startCaptor.capture());
        verify(persistenceService, times(1)).updateEnd(startCaptor.getValue());

        TransactionSegmentRecord record = startCaptor.getValue();
        assertThat(record.getTransactionGlobalId()).isEqualTo("20260722120000000CPFlocal010000001");
        assertThat(record.getTransactionSegmentId()).contains("-SEG-0001-");
        assertThat(record.getModuleCode()).isEqualTo("REF");
        assertThat(record.getSourceModuleCode()).isEqualTo("REF");
        assertThat(record.getTargetModuleCode()).isEqualTo("ACC");
        assertThat(record.getStatus()).isEqualTo(TransactionSegmentStatus.SUCCESS.name());
        assertThat(record.getFailureYn()).isEqualTo("N");
        assertThat(record.getEndedAt()).isNotNull();
    }

    @Test
    void recordsFailureAndPropagatesOriginalException() {
        assertThatThrownBy(() -> service.around(
                TransactionSegmentRole.SUB,
                TransactionSegmentDirection.OUTBOUND,
                "REF",
                "REF",
                "EXS",
                "/api/reference/external-simulator/executions",
                "대외 시뮬레이터 호출",
                () -> {
                    throw new IllegalStateException("password=exposed-value");
                }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exposed-value");

        ArgumentCaptor<TransactionSegmentRecord> endCaptor =
                ArgumentCaptor.forClass(TransactionSegmentRecord.class);
        verify(persistenceService).updateEnd(endCaptor.capture());

        TransactionSegmentRecord record = endCaptor.getValue();
        assertThat(record.getStatus()).isEqualTo(TransactionSegmentStatus.FAILED.name());
        assertThat(record.getFailureYn()).isEqualTo("Y");
        assertThat(record.getFailureCode()).isEqualTo("IllegalStateException");
        assertThat(record.getFailureMessageMasked())
                .doesNotContain("exposed-value")
                .contains("***");
    }
}
