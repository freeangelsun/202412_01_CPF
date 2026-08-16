package com.cpf.education.web.crud.dto;
/**
 * EDU CRUD 교육 항목 응답 DTO입니다.
 *
 * @param educationItemId 교육 항목 ID
 * @param title 항목명
 * @param status 상태 코드
 * @param description 설명
 * @param createdAt 생성 일시
 * @param categoryCode 분류 코드
 * @param ownerEducation 다른 Domain을 직접 조인하지 않는 중립 소유 참조값
 */
public record EducationCrudEducationResponse(
        Long educationItemId,
        String title,
        String status,
        String description,
        String createdAt,
        String categoryCode,
        String ownerEducation) {
}
