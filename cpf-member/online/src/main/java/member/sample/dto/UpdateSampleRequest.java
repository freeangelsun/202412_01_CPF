package member.sample.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.cpf.core.api.base.CpfRequest;

/** Optimistic Version을 포함하는 Sample Update 입력 계약입니다. */
public record UpdateSampleRequest(
        @NotBlank @Size(max=200) String itemName,
        @NotBlank @Size(max=30) String statusCode,
        @NotBlank @Size(max=180) String idempotencyKey,
        @Min(0) long expectedVersion) implements CpfRequest { }
