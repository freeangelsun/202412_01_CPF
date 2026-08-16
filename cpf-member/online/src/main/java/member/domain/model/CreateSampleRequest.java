package member.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sample Create 입력 계약입니다. */
public record CreateSampleRequest(
        @NotBlank @Size(max=100) String sampleKey,
        @NotBlank @Size(max=200) String itemName,
        @NotBlank @Size(max=180) String idempotencyKey) { }
