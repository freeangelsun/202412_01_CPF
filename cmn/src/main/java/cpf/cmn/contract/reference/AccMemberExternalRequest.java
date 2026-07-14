package cpf.cmn.contract.reference;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * MBR이 ACC reference domain에 회원 기반 외부연계를 요청하는 공개 계약입니다.
 */
public record AccMemberExternalRequest(
        @NotNull @Positive Integer memberId,
        @Size(max = 50) String institutionCode,
        @Size(max = 120) String externalKey) {
}
