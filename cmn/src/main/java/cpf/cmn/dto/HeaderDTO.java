package cpf.cmn.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ?ㅻ뜑 ?곸뿭 DTO.
 * 紐⑤뱺 嫄곕옒?먯꽌 怨듯넻?곸쑝濡??꾩슂???ㅻ뜑 ?뺣낫瑜??뺤쓽?⑸땲??
 */
@Data // Lombok???ъ슜??Getter/Setter ?먮룞 ?앹꽦
public class HeaderDTO {
    @NotEmpty(message = "Transaction ID???꾩닔 媛믪엯?덈떎.")
    private String transactionId;

    @NotEmpty(message = "Initial Channel Code???꾩닔 媛믪엯?덈떎.")
    private String initialChannelCode;

    @NotEmpty(message = "Channel Code???꾩닔 媛믪엯?덈떎.")
    private String channelCode;

    @NotNull(message = "Timestamp???꾩닔 媛믪엯?덈떎.")
    private Long timestamp;
}

