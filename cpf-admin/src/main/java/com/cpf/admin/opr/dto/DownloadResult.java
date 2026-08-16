package com.cpf.admin.opr.dto;

/**
 * ADM 공통 다운로드 생성 결과입니다.
 *
 * @param downloadId 다운로드 감사 로그 ID입니다.
 * @param fileName 다운로드 파일명입니다.
 * @param contentType 응답 Content-Type입니다.
 * @param content 다운로드 파일 바이트입니다.
 * @param rowCount 다운로드 행 수입니다.
 * @param maskedYn 마스킹 적용 여부입니다.
 */
public record DownloadResult(
        Long downloadId,
        String fileName,
        String contentType,
        byte[] content,
        int rowCount,
        String maskedYn
) {
}
