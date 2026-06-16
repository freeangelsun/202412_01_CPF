package cpf.acc.bse.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * ?대옒?? AccMemberDTO
 *
 * ACC 紐⑤뱢??硫ㅻ쾭 ?곗씠?곕? ?뺤쓽?⑸땲??
 */
@Data
public class AccMemberDTO {
    @NotEmpty(message = "Member ID???꾩닔 媛믪엯?덈떎.")
    private String memberId; // 硫ㅻ쾭 ID

    @NotEmpty(message = "Member Name? ?꾩닔 媛믪엯?덈떎.")
    private String memberName; // 硫ㅻ쾭 ?대쫫

    @NotEmpty(message = "Member Status???꾩닔 媛믪엯?덈떎.")
    private String memberStatus; // 硫ㅻ쾭 ?곹깭
}

