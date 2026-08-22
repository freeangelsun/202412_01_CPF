package external.sample.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.cpf.core.api.base.CpfRequest;

/** Optimistic Version과 멱등키를 포함하는 논리 삭제 입력 계약입니다. */
public record DeleteSampleRequest(
        @NotBlank @Size(max=180) String idempotencyKey,
        @Min(0) long expectedVersion) implements CpfRequest { }
