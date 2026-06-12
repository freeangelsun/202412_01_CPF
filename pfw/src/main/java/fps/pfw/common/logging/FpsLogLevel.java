package fps.pfw.common.logging;

/**
 * 특정 거래 진단 로그에 사용할 표준 로그 레벨입니다.
 *
 * <p>운영 중 전체 패키지 로그 레벨을 DEBUG로 올리면 로그량이 급증합니다.
 * 이 enum은 특정 글로벌 거래ID 또는 업무 거래ID에 대해서만 PFW 진단 로그를
 * 선택적으로 남기기 위한 기준값입니다.</p>
 */
public enum FpsLogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF
}
