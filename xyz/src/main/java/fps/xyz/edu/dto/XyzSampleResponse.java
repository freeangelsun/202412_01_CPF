package fps.xyz.edu.dto;

/**
 * XYZ 교육용 샘플 응답 DTO입니다.
 *
 * @param sampleId    샘플 ID
 * @param title       샘플 제목
 * @param status      처리 상태
 * @param description 샘플 설명
 * @param createdAt   생성 일시
 */
public record XyzSampleResponse(
        Long sampleId,
        String title,
        String status,
        String description,
        String createdAt) {
}
