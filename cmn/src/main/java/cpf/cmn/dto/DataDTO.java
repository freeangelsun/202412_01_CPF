package cpf.cmn.dto;

import jakarta.validation.Valid;
import lombok.Data;

/**
 * ?대옒?? DataDTO
 *
 * ?곗씠???붿껌??怨듯넻 援ъ“瑜??뺤쓽?⑸땲??
 * @param <T> ?곗씠?????(?? ACC 紐⑤뱢??DTO)
 */
@Data
public class DataDTO<T> {
    @Valid
    private T body; // ?곗씠??蹂몃Ц (?쒕꽕由????
}

