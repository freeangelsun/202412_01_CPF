package cpf.xyz.centercut.dto;

import java.util.List;
import java.util.Map;

/**
 * XYZ center-cut EDU 실행 결과 응답입니다.
 *
 * <p>개발자가 ADM 관제나 smoke 결과에서 확인해야 하는 요약값과 DB result snapshot을 함께 보여줍니다.</p>
 */
public record XyzCenterCutExecutionResponse(
        String centerCutJobId,
        int requestedCount,
        int successCount,
        int failedCount,
        int skippedCount,
        Map<String, Long> resultStatusCounts,
        List<Map<String, Object>> results) {
}
