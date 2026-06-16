package cpf.xyz.edu.dto;

/**
 * XYZ 援먯쑁???섑뵆 ?묐떟 DTO?낅땲??
 *
 * @param sampleId    ?섑뵆 ID
 * @param title       ?섑뵆 ?쒕ぉ
 * @param status      泥섎━ ?곹깭
 * @param description ?섑뵆 ?ㅻ챸
 * @param createdAt   ?앹꽦 ?쇱떆
 */
public record XyzSampleResponse(
        Long sampleId,
        String title,
        String status,
        String description,
        String createdAt) {
}

