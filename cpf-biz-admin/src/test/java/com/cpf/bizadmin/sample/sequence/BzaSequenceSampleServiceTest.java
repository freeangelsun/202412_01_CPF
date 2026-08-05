package com.cpf.bizadmin.sample.sequence;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BzaSequenceSampleServiceTest {
    private final BzaSequenceSampleService service = new BzaSequenceSampleService();
    private final BzaSequenceSampleService.SequenceRule rule =
            new BzaSequenceSampleService.SequenceRule("ORDER", "ORD-", 4, 9999);

    @Test
    void generatesAuditedSequenceAndReplaysSameOperation() {
        var request = request("op-1", 0, "업무 주문번호 생성");
        var first = service.next(rule, BzaSequenceSampleService.SequenceState.empty(), request, Instant.EPOCH);
        var replay = service.next(rule, first.state(), request, Instant.EPOCH.plusSeconds(1));
        assertThat(first.value()).isEqualTo("ORD-202608050001");
        assertThat(first.audit().approvalId()).isEqualTo(1001L);
        assertThat(replay.replay()).isTrue();
        assertThat(replay.value()).isEqualTo(first.value());
        assertThat(service.reconcile(first.state(), first)).isTrue();
    }

    @Test
    void rejectsVersionConflictAndDivergentOperationReplay() {
        var first = service.next(rule, BzaSequenceSampleService.SequenceState.empty(),
                request("op-1", 0, "최초 생성"), Instant.EPOCH);
        assertThatThrownBy(() -> service.next(rule, first.state(), request("op-2", 0, "추가 생성"), Instant.EPOCH))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("version 충돌");
        assertThatThrownBy(() -> service.next(rule, first.state(), request("op-1", 1, "다른 요청"), Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("다른 채번 요청");
    }

    @Test
    void resetsDailyAndRequiresApprovalReason() {
        var prior = new BzaSequenceSampleService.SequenceState(LocalDate.of(2026, 8, 4), 77, 3,
                null, null, null, null);
        var result = service.next(rule, prior, new BzaSequenceSampleService.SequenceRequest(
                "ORDER", LocalDate.of(2026, 8, 5), 3, 2002, "op-2", "operator", "영업일 변경"), Instant.EPOCH);
        assertThat(result.value()).isEqualTo("ORD-202608050001");
        assertThatThrownBy(() -> service.next(rule, result.state(), new BzaSequenceSampleService.SequenceRequest(
                "ORDER", LocalDate.of(2026, 8, 5), 4, 0, "op-3", "operator", ""), Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static BzaSequenceSampleService.SequenceRequest request(String operationId, long version, String reason) {
        return new BzaSequenceSampleService.SequenceRequest("ORDER", LocalDate.of(2026, 8, 5), version,
                1001, operationId, "operator", reason);
    }
}
