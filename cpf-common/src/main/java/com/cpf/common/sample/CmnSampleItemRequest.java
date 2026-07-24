package com.cpf.common.sample;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * CMN Sample 항목 등록·수정 요청입니다.
 */
public record CmnSampleItemRequest(
        @NotBlank @Size(max = 100)
        String sampleKey,
        @NotBlank @Size(max = 200)
        String itemName,
        @Size(max = 30)
        String categoryCode,
        @Pattern(regexp = "ACTIVE|INACTIVE")
        String statusCode,
        @Size(max = 500)
        String searchableText,
        @Size(max = 100)
        String ownerReference,
        long sortOrder,
        @Size(max = 100)
        String requestUser) {
}
