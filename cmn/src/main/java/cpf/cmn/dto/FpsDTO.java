package cpf.cmn.dto;

import jakarta.validation.Valid;
import lombok.Data;

/**
 * ?대옒?? FpsDTO
 *
 * ⑤뱺 嫄곕옒 ?붿껌??怨듯넻 援ъ“瑜??뺤쓽?⑸땲??
 * @param <T> ?곗씠?????(?? ACC ⑤뱢??DTO)
 */
@Data
public class FpsDTO<T> {

    @Valid
    private HeaderDTO header; // 怨듯넻 ?ㅻ뜑 ?뺣낫

    @Valid
    private DataDTO<T> data; // ?곗씠???뺣낫 (?쒕꽕由????
}

