package fps.mbr.common.logging;

import fps.pfw.common.logging.TransactionIdGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PFW 글로벌 거래ID 생성 규칙을 확인하는 테스트입니다.
 *
 * <p>거래ID는 모든 주제영역 로그를 하나로 묶는 가장 중요한 키입니다.
 * 따라서 생성 시각, 생성 주제영역, WAS ID, 일련번호가 정해진 순서로 조합되는지 확인합니다.</p>
 */
class TransactionIdGeneratorTest {

    /**
     * 고정 시각을 사용해 거래ID 포맷과 일련번호 증가를 검증합니다.
     */
    @Test
    void generatesTransactionIdWithTimestampModuleWasAndSequence() {
        // 테스트가 실행되는 실제 시간에 흔들리지 않도록 Clock을 고정합니다.
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-11T05:12:34.567Z"),
                ZoneId.of("Asia/Seoul"));
        // ACC 주제영역, accAP01 WAS, 7자리 일련번호 기준의 생성기를 만듭니다.
        TransactionIdGenerator generator = new TransactionIdGenerator("ACC", "accAP01", 7, clock);

        // 첫 번째 생성 값은 일련번호 0000001로 끝나야 합니다.
        assertThat(generator.generate()).isEqualTo("20260611141234567ACCaccAP010000001");
        // 같은 밀리초 안에서 한 번 더 생성하면 일련번호만 0000002로 증가해야 합니다.
        assertThat(generator.generate()).isEqualTo("20260611141234567ACCaccAP010000002");
    }

    /**
     * 외부에서 들어온 거래ID를 재사용할지 새로 만들지 판단하는 규칙을 검증합니다.
     */
    @Test
    void reusesOnlyValidIncomingTransactionId() {
        // 비정상 거래ID가 들어왔을 때 새 거래ID 생성 값을 예측할 수 있도록 시간을 고정합니다.
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-11T05:12:34.567Z"),
                ZoneId.of("Asia/Seoul"));
        // MBR 주제영역 기준 생성기를 만들어 비정상 입력 시 MBR 기준 거래ID가 생성되는지 확인합니다.
        TransactionIdGenerator generator = new TransactionIdGenerator("MBR", "mbrAP01", 7, clock);

        // 표준 포맷의 거래ID는 상위 채널이나 상위 주제영역에서 온 값이므로 그대로 이어받습니다.
        assertThat(generator.generateOrUse("20260611141234567ACCaccAP010000001"))
                .isEqualTo("20260611141234567ACCaccAP010000001");
        // 비표준 거래ID는 추적 품질을 해치므로 현재 주제영역 기준의 새 거래ID로 교체합니다.
        assertThat(generator.generateOrUse("TRX-TEST-001"))
                .isEqualTo("20260611141234567MBRmbrAP010000001");
    }
}
